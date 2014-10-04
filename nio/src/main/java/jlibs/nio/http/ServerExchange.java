/*
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T <santhosh.tekuri@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.nio.http;

import jlibs.core.lang.NotImplementedException;
import jlibs.nio.*;
import jlibs.nio.filters.InputLimitExceeded;
import jlibs.nio.filters.ReadTrackingInput;
import jlibs.nio.filters.TrackingInput;
import jlibs.nio.http.expr.UnresolvedException;
import jlibs.nio.http.msg.*;
import jlibs.nio.http.msg.parser.RequestParser;
import jlibs.nio.http.util.Expect;
import jlibs.nio.http.util.USAscii;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static java.nio.channels.SelectionKey.OP_READ;
import static jlibs.nio.Debugger.HTTP;
import static jlibs.nio.Debugger.println;
import static jlibs.nio.http.ServerExchange.State.*;
import static jlibs.nio.http.msg.Message.CONNECTION;
import static jlibs.nio.http.msg.Message.PROXY_CONNECTION;
import static jlibs.nio.http.msg.Method.CONNECT;

/**
 * @author Santhosh Kumar Tekuri
 */
public final class ServerExchange extends Exchange{
    private static final ByteBuffer CONTINUE_100;
    static{
        Response response = new Response();
        response.status = Status.CONTINUE;
        String string = response.toString();
        CONTINUE_100 = ByteBuffer.allocateDirect(string.length());
        USAscii.append(CONTINUE_100, string);
        CONTINUE_100.flip();
    }

    private final HTTPServer server;
    private final RequestListener user;
    private Collection<ServerFilter> requestFilters;
    private Collection<ServerFilter> responseFilters;
    private Collection<ServerFilter> errorFilters;

    AccessLog accessLog;
    AccessLog.Record accessLogRecord;

    protected ServerExchange(HTTPServer server){
        super(server.maxRequestHeadSize, new RequestParser(server.maxURISize), OP_READ);
        this.server = server;
        user = server.listener;
        requestFilters = server.requestFilters;
        responseFilters = server.responseFilters;
        errorFilters = server.errorFilters;

        accessLog = server.accessLog;
        if(accessLog!=null){
            accessLogRecord = accessLog.records.allocate();
            accessLogRecord.setLogHandler(server.logHandler);
        }
        connectionStatus = ConnectionStatus.OPEN;
    }

    enum State{
        READ_REQUEST, FILTER_REQUEST,
        RESPONSE_READY, FILTER_RESPONSE, FILTER_ERROR,
        DELIVER_RESPONSE, DRAIN_REQUEST, WRITE_RESPONSE,
        CLOSED
    }

    private State state = READ_REQUEST;
    private boolean hasProxyConnectionHeader;
    private Version requestVersion;
    private boolean requestHasPayload;
    private ByteBuffer continue100Buffer;
    protected Iterator<ServerFilter> filters;

    @Override
    protected boolean process(int readyOp) throws IOException{
        if(state==CLOSED)
            return true;
        if(continue100Buffer!=null){
            try{
                if(send(continue100Buffer)){
                    continue100Buffer = null;
                    return false;
                }
            }catch(Throwable thr){
                setError(thr);
            }
        }

        while(true){
            try{
                switch(state){
                    case READ_REQUEST:
                        request = new Request();
                        readMessage.reset(request, false);
                        setChild(readMessage);
                        return true;
                    case FILTER_REQUEST:
                        while(response==null && filters.hasNext()){
                            if(!filters.next().filter(this, FilterType.REQUEST))
                                return false;
                        }
                        state = RESPONSE_READY;
                        if(HTTP)
                            println("state = "+state);
                        if(response==null && !user.process(this))
                            return false;
                    case RESPONSE_READY:
                        filters = responseFilters.iterator();
                        state = FILTER_RESPONSE;
                        if(HTTP)
                            println("state = "+state);
                    case FILTER_RESPONSE:
                    case FILTER_ERROR:
                        FilterType filterType = state==FILTER_RESPONSE ? FilterType.RESPONSE : FilterType.ERROR;
                        while(filters.hasNext()){
                            if(!filters.next().filter(this, filterType))
                                return false;
                        }
                        state = DELIVER_RESPONSE;
                        if(HTTP)
                            println("state = "+state);
                    case DELIVER_RESPONSE:
                        in = ((jlibs.nio.Readable)in.channel()).in();
                        in.setInputListener(listener);
                        out.setOutputListener(listener);
                        state = WRITE_RESPONSE;
                        if(keepAlive && requestHasPayload){
                            if(in.eof())
                                state = DRAIN_REQUEST;
                            else
                                keepAlive = false;
                        }
                        if(HTTP)
                            println("state = "+state);
                        break;
                    case DRAIN_REQUEST:
                        if(!drainInputs())
                            return false;
                        state = WRITE_RESPONSE;
                        if(HTTP)
                            println("state = "+state);
                    case WRITE_RESPONSE:
                        if(response==null){
                            if(error==null)
                                error = Status.INTERNAL_SERVER_ERROR.with("Missing Response");
                            Status errorStatus;
                            if(error instanceof Status)
                                errorStatus = (Status)error;
                            else
                                errorStatus = Status.INTERNAL_SERVER_ERROR.with(error);
                            response = new Response();
                            response.status = errorStatus;
                            if(errorStatus.getCause()!=null)
                                response.setPayload(new ErrorPayload(errorStatus.getCause()));
                        }
                        if(error!=null && Status.INTERNAL_SERVER_ERROR.equals(response.status))
                            Reactor.current().handleException(error);
                        error = null;
                        response.version = requestVersion;
                        response.setKeepAlive(keepAlive);
                        if(hasProxyConnectionHeader){
                            Header header = response.headers.remove(CONNECTION);
                            if(header!=null)
                                response.headers.set(PROXY_CONNECTION, header.getValue());
                        }
                        if(server.setDateHeader)
                            response.setDate(false);
                        if(server.serverName !=null)
                            response.setServer(server.serverName);
                        writeMessage.reset(response, continue100Buffer, true);
                        if(accessLog!=null)
                            accessLogRecord.process(this, response);
                        continue100Buffer = null;
                        setChild(writeMessage);
                        return true;
                    case CLOSED:
                        return true;
                }
            }catch(Throwable thr){
                setError(thr);
            }
        }
    }

    @Override
    protected void reset(){
        super.reset();
        in.channel().taskCompleted();
        state = READ_REQUEST;
        if(HTTP)
            println("state = "+state);
        hasProxyConnectionHeader = false;
        requestVersion = null;
        requestHasPayload = false;
        continue100Buffer = null;
        filters = null;
        callback = null;
        if(accessLog!=null){
            accessLogRecord = accessLog.records.allocate();
            accessLogRecord.setLogHandler(server.logHandler);
        }
    }

    @Override
    protected void readMessageFinished(Throwable thr){
        if(accessLog!=null){
            try{
                accessLogRecord.process(this, request);
            }catch(Throwable thr1){
                Reactor.current().handleException(thr1);
            }
        }
        if(thr!=null){
            if(thr==ReadMessage.IGNORABLE_EOF_EXCEPTION){
                if(accessLog!=null){
                    accessLogRecord.reset();
                    accessLog.records.free(accessLogRecord);
                }
                close();
                return;
            }
            if(thr instanceof Status)
                error = thr;
            else if(thr instanceof SocketException){
                error = thr;
                if(HTTP)
                    println("error = "+error);
                notifyCallback();
                close();
                return;
            }else if(thr instanceof SocketTimeoutException)
                error = Status.REQUEST_TIMEOUT;
            else if(thr instanceof NotImplementedException)
                error = Status.NOT_IMPLEMENTED.with(thr);
            else
                error = Status.INTERNAL_SERVER_ERROR.with(thr);
            if(HTTP)
                println("error = "+error);
        }

        if(server.supportsProxyConnectionHeader){
            Header header = request.headers.remove(PROXY_CONNECTION);
            if(header!=null){
                hasProxyConnectionHeader = true;
                request.headers.set(CONNECTION, header.getValue());
            }
        }
        keepAlive = error==null && (request.method==CONNECT || request.isKeepAlive());
        requestVersion = request.version;
        requestHasPayload = request.getPayload().getContentLength()!=0;

        if(error==null && requestHasPayload && requestVersion.expectSupported){
            Expect expectation = request.getExpectation();
            if(expectation==Expect.CONTINUE_100){
                SocketPayload socketPayload = (SocketPayload)request.getPayload();
                in = socketPayload.in = new ReadTrackingInput(in, this::send100Continue);
            }
            else if(expectation!=null)
                error = Status.EXPECTATION_FAILED;
        }

        in.setInputListener(null);
        out.setOutputListener(null);
        if(error==null){
            filters = requestFilters.iterator();
            state = FILTER_REQUEST;
        }else{
            filters = errorFilters.iterator();
            state = FILTER_ERROR;
        }
        if(HTTP)
            println("state = "+state);
    }

    private void send100Continue(TrackingInput tracker){
        continue100Buffer = CONTINUE_100.duplicate();
        try{
            if(send(continue100Buffer))
                continue100Buffer = null;
        }catch(Throwable thr){
            error = thr;
            close();
            notifyCallback();
        }
    }

    @Override
    protected void writeMessageFinished(Throwable thr){
        error = thr;
        if(error!=null || !keepAlive)
            close();
        notifyCallback();
        if(in!=null)
            reset();
    }

    protected void setError(Throwable thr){
        if(thr instanceof SocketTimeoutException)
            error = Status.REQUEST_TIMEOUT;
        else if(thr instanceof InputLimitExceeded)
            error = Status.REQUEST_ENTITY_TOO_LARGE;
        else if(thr instanceof NotImplementedException)
            error = Status.NOT_IMPLEMENTED.with(thr);
        else
            error = thr;
        if(HTTP)
            println("error = "+error);

        if(state.ordinal()<FILTER_ERROR.ordinal()){
            filters = errorFilters.iterator();
            state = FILTER_ERROR;
            if(HTTP)
                println("state = "+state);
        }else if(state==FILTER_ERROR){
            state = DELIVER_RESPONSE;
            if(HTTP)
                println("state = "+state);
        }else{
            notifyCallback();
            close();
        }
    }

    public void setResponse(Response response){
        this.response = response;
    }

    protected ServerCallback callback;

    public void setCallback(ServerCallback callback){
        this.callback = callback;
    }

    @SuppressWarnings("unchecked")
    @Trace(condition=HTTP)
    private void notifyCallback(){
        try{
            if(accessLog!=null)
                accessLogRecord.finished(this);
        }catch(Throwable thr){
            Reactor.current().handleException(thr);
        }
        if(callback!=null){
            try{
                callback.completed(this, error);
            }catch(Throwable unexpected){
                Reactor.current().handleException(unexpected);
            }
            callback = null;
        }else if(error!=null)
            Reactor.current().handleException(error);
    }

    @Override
    public void close(){
        super.close();
        state = CLOSED;
        if(HTTP)
            println("state = "+state);
    }

    @Override
    public TCPEndpoint getEndpoint(){
        return server.endpoint;
    }

    public InetAddress getClientAddress(){
        TCPConnection con = (TCPConnection)in.channel();
        return con.selectable.socket().getInetAddress();
    }

    @Override
    public Connection stealConnection(){
        if(HTTP)
            println("stealConnection()");
        Connection con = (Connection)in.channel();
        in = null;
        out = null;
        state = CLOSED;
        return con;
    }

    @Override
    public String toString(){
        String str = super.toString();
        return str.substring(0, str.length()-1)+":"+state+"]";
    }

    @Override
    @SuppressWarnings("StringEquality")
    public Object getField(String name) throws UnresolvedException{
        if(name=="remote_ip"){
            InetAddress address = getClientAddress();
            return address.getHostAddress();
        }else if(name=="client_ip"){
            List<String> list = getRequest().getXForwardedFor();
            if(!list.isEmpty())
                return list.get(0);
            return getClientAddress().getHostAddress();
        }else
            return super.getField(name);
    }
}
