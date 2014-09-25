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

import jlibs.nio.Connection;
import jlibs.nio.Reactor;
import jlibs.nio.Result;
import jlibs.nio.TCPEndpoint;
import jlibs.nio.filters.CloseTrackingInput;
import jlibs.nio.filters.TrackingInput;
import jlibs.nio.http.msg.Method;
import jlibs.nio.http.msg.Request;
import jlibs.nio.http.msg.Response;
import jlibs.nio.http.msg.Status;
import jlibs.nio.http.msg.parser.ResponseParser;
import jlibs.nio.http.util.Expect;
import jlibs.nio.listeners.IOListener;
import jlibs.nio.log.LogHandler;

import java.util.Collection;
import java.util.Iterator;

import static java.nio.channels.SelectionKey.OP_WRITE;
import static jlibs.nio.Debugger.HTTP;
import static jlibs.nio.Debugger.println;
import static jlibs.nio.http.ClientExchange.State.*;
import static jlibs.nio.http.msg.Method.HEAD;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ClientExchange extends Exchange{
    private Collection<ClientFilter> requestFilters;
    private Collection<ClientFilter> responseFilters;

    private final HTTPClient client;
    private TCPEndpoint endpoint;
    private TCPEndpoint retry;
    private ResponseListener user;

    private AccessLog accessLog;
    private AccessLog.Record accessLogRecord;
    private LogHandler logHandler;

    protected ClientExchange(HTTPClient client, TCPEndpoint endpoint){
        super(client.maxResponseHeadSize, new ResponseParser(), OP_WRITE);
        this.client = client;
        this.endpoint = endpoint;
        requestFilters=  client.requestFilters;
        responseFilters = client.responseFilters;

        accessLog = client.accessLog;
        if(accessLog!=null){
            accessLogRecord = accessLog.new Record();
            logHandler = client.logHandler;
        }
    }

    enum State{
        PREPARE_REQUEST_FILTERS, FILTER_REQUEST, WRITE_REQUEST,
        READ_RESPONSE, SEND_REQUEST_PAYLOAD, PREPARE_RESPONSE_FILTERS, FILTER_RESPONSE,
        DELIVER_RESPONSE, DRAIN_RESPONSE, PREPARE_COMPLETE, COMPLETE,
        COMPLETED, CLOSED
    }

    private State state = PREPARE_REQUEST_FILTERS;
    private Method requestMethod;
    private boolean responseHasPayload;
    private boolean continue100Expected;
    private boolean trackClose = false;
    protected Iterator<ClientFilter> filters;

    @Override
    protected boolean process(int readyOp){
        while(true){
            try{
                switch(state){
                    case PREPARE_REQUEST_FILTERS:
                        if(in!=null)
                            in.setInputListener(null);
                        filters = requestFilters.iterator();
                        state = FILTER_REQUEST;
                        if(HTTP)
                            println("state = "+state);
                    case FILTER_REQUEST:
                        while(filters.hasNext()){
                            if(!filters.next().filter(this, FilterType.REQUEST))
                                return false;
                        }
                        if(in==null){
                            endpoint.getConnection(this::connectCompleted, client.proxy);
                            return false;
                        }
                        state = WRITE_REQUEST;
                        if(HTTP)
                            println("state = "+state);
                    case WRITE_REQUEST:
                        in.setInputListener(listener);
                        if(client.keepAliveTimeout<0)
                            keepAlive = request.isKeepAlive();
                        else
                            request.setKeepAlive(keepAlive=client.keepAliveTimeout!=0);
                        requestMethod = request.method;
                        String hostPort = endpoint.port==80 || endpoint.port==443 ? endpoint.host : endpoint.toString();
                        request.headers.set(Request.HOST, hostPort);
                        if(client.userAgent !=null)
                            request.setUserAgent(client.userAgent);
                        continue100Expected = false;
                        if(request.version.expectSupported && request.getPayload().getContentLength()!=0){
                            if(request.getExpectation()==Expect.CONTINUE_100)
                                continue100Expected = true;
                        }
                        writeMessage.reset(request, null, !continue100Expected);
                        if(accessLog!=null)
                            accessLogRecord.process(this, request);
                        setChild(writeMessage);
                        return true;
                    case READ_RESPONSE:
                        response = new Response();
                        readMessage.reset(response, requestMethod==HEAD);
                        setChild(readMessage);
                        return true;
                    case PREPARE_RESPONSE_FILTERS:
                        in.setInputListener(null);
                        filters = responseFilters.iterator();
                        state = FILTER_RESPONSE;
                        if(HTTP)
                            println("state = "+state);
                    case FILTER_RESPONSE:
                        while(filters.hasNext()){
                            if(!filters.next().filter(this, FilterType.RESPONSE))
                                return false;
                            if(retry!=null)
                                break;
                        }
                        state = DELIVER_RESPONSE;
                        if(HTTP)
                            println("state = "+state);
                    case DELIVER_RESPONSE:
                        if(retry==null)
                            user.process(this, error);
                        state = COMPLETE;
                        if(in!=null){
                            if(responseHasPayload){
                                if(retry==null){
                                    if(in.isOpen()){
                                        trackClose = true;
                                        return false;
                                    }else if(!in.eof())
                                        keepAlive = false;
                                }else if(keepAlive && !retry.equals(endpoint))
                                    keepAlive = false;
                            }
                            in = ((jlibs.nio.Readable)in.channel()).in();
                            in.setInputListener(listener);
                        }else
                            assert !keepAlive;
                        if(HTTP)
                            println("state = "+state);
                    case COMPLETE:
                        if(keepAlive && responseHasPayload){
                            if(!drainInputs())
                                return false;
                        }
                        if(keepAlive){
                            if(retry==null)
                                Reactor.current().connectionPool.add(endpoint.toString(), (Connection)in.channel(), Math.abs(client.keepAliveTimeout));
                        }else
                            close();
                        if(retry==null)
                            notifyCallback();
                        if(retry!=null){
                            if(retry.equals(endpoint) && in!=null)
                                Reactor.current().connectionPool.remove((Connection)in.channel());
                            else{
                                in = null;
                                out = null;
                            }
                            endpoint = retry;
                            retry = null;
                            state = PREPARE_REQUEST_FILTERS;
                            requestMethod = null;
                            responseHasPayload = false;
                            continue100Expected = false;
                            trackClose = false;
                            error = null;
                            response = null;
                            if(HTTP)
                                println("state = "+state);
                            break;
                        }else
                            return true;
                    case CLOSED:
                        return true;
                    case SEND_REQUEST_PAYLOAD:
                        setChild(writeMessage);
                        return true;
                }
            }catch(Throwable thr){
                setError(thr);
            }
        }
    }

    public void setAccessLog(ServerExchange exchange){
        accessLog = exchange.accessLog;
        accessLogRecord = exchange.accessLogRecord;
        logHandler = exchange.logHandler;
    }

    public void execute(ResponseListener listener){
        if(HTTP)
            println(this+".execute{");
        user = listener;
        assert state==PREPARE_REQUEST_FILTERS;
        process(OP_WRITE);
        if(HTTP)
            println("}");
    }

    private void connectCompleted(Result<Connection> result){
        try{
            Connection con = result.get();
            connectionStatus = ConnectionStatus.OPEN;
            state = WRITE_REQUEST;
            if(HTTP)
                println("state = "+state);
            new IOListener().start(this, con);
        }catch(Throwable thr){
            setError(thr);
            process(0);
        }
    }

    @Override
    protected void writeMessageFinished(Throwable thr){
        if(thr==null){
            state = READ_RESPONSE;
            if(HTTP)
                println("state = "+state);
        }else
            setError(thr);
    }

    @Override
    protected void readMessageFinished(Throwable thr){
        if(accessLog!=null)
            accessLogRecord.process(this, response);
        if(thr==null){
            if(continue100Expected && Status.CONTINUE.equals(response.status))
                state = SEND_REQUEST_PAYLOAD;
            else{
                responseHasPayload = response.getPayload().getContentLength()!=0;
                if(responseHasPayload){
                    SocketPayload socketPayload = (SocketPayload)response.getPayload();
                    in = socketPayload.in = new CloseTrackingInput(in, this::responsePayloadClosed);
                }
                if(keepAlive)
                    keepAlive = readMessage.keepAlive();
                state = PREPARE_RESPONSE_FILTERS;
            }
            if(HTTP)
                println("state = "+state);
        }else
            setError(thr);
    }

    private void responsePayloadClosed(TrackingInput trackingInput){
        if(trackClose){
            assert retry==null;
            if(!in.eof())
                keepAlive = false;
            in = ((jlibs.nio.Readable)in.channel()).in();
            if(callback==null)
                listener.process(in);
            else{
                in.setInputListener(listener);
                in.wakeupReader();
            }
        }
    }

    @Override
    protected void reset(){
        super.reset();
    }

    public void setRequest(Request request){
        this.request = request;
    }

    protected void setError(Throwable thr){
        error = thr;
        if(HTTP)
            println("error = "+error);
        keepAlive = false;
        retry = null;
        if(state.ordinal()<DELIVER_RESPONSE.ordinal())
            state = DELIVER_RESPONSE;
        else
            state = COMPLETED ;
        if(HTTP)
            println("state = "+state);
    }

    protected ClientCallback callback;
    public void setCallback(ClientCallback callback){
        this.callback = callback;
    }

    private void notifyCallback(){
        try{
            if(accessLog!=null){
                accessLogRecord.finished(this);
                if(accessLogRecord.getOwner()==ClientExchange.class){
                    logHandler.publish(accessLogRecord);
                    accessLogRecord.reset();
                }
            }
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
    }

    @Override
    public TCPEndpoint getEndpoint(){
        return endpoint;
    }

    @Override
    public Connection stealConnection(){
        if(in==null)
            return null;
        if(HTTP)
            println("stealConnection()");
        Connection con = (Connection)in.channel();
        Reactor.current().connectionPool.remove(con);
        in = null;
        out = null;
        return con;
    }

    public void retry(){
        retry = endpoint;
        if(HTTP)
            println("retry()");
    }

    public void retry(TCPEndpoint endpoint){
        retry = endpoint;
        if(HTTP)
            println("retry("+retry+")");
    }

    @Override
    public String toString(){
        String str = super.toString();
        return str.substring(0, str.length()-1)+":"+state+"]";
    }
}
