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

import jlibs.core.io.IOUtil;
import jlibs.core.lang.ImpossibleException;
import jlibs.core.lang.NotImplementedException;
import jlibs.nio.*;
import jlibs.nio.async.IgnorableEOFException;
import jlibs.nio.async.LineOverflowException;
import jlibs.nio.async.WriteBuffer;
import jlibs.nio.channels.filters.ReadTrackingInputFilter;
import jlibs.nio.channels.filters.TrackingInputFilter;
import jlibs.nio.http.msg.*;
import jlibs.nio.http.msg.spec.HTTPDate;
import jlibs.nio.http.msg.spec.values.Expect;
import jlibs.nio.http.msg.spec.values.MediaType;
import jlibs.nio.http.encoders.ThrowableEncoder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 * @author Santhosh Kumar Tekuri
 */
public class HTTPServer extends HTTPService implements Server.Listener{
    static final byte CONTINUE_100[];
    static{
        Response response = new Response();
        response.setStatus(Status.CONTINUE);
        CONTINUE_100 = response.toString().getBytes(IOUtil.US_ASCII);
    }

    private Consumer<Task> listener;

    public AccessLog accessLog;
    public boolean supportsProxyConnectionHeader;
    public long ioTimeout;
    public int requestLineLimit;
    public int requestHeaderLimit;
    public int requestHeadersLimit;
    public boolean showStackTrace;
    public boolean setDateHeader;

    public HTTPServer(Consumer<Task> listener){
        this.listener = listener;
        Defaults.apply(this);
    }

    private ServerEndpoint serverEndpoint;
    public void start(ServerEndpoint serverEndpoint) throws IOException{
        this.serverEndpoint = serverEndpoint;
        serverEndpoint.start(this);
    }

    public void stop() throws IOException{
        serverEndpoint.stop();
    }

    @Override
    public void accepted(Server server, Client client){
        ServerEndpoint endpoint;
        if(server.attachment() instanceof ServerEndpoint)
            endpoint = (ServerEndpoint)server.attachment();
        else{
            endpoint = new ServerEndpoint((InetSocketAddress)server.boundTo());
            if(server.attachment()==null)
                server.attach(endpoint);
        }
        new Task(endpoint, client, accessLog).resume();
    }

    public class Task extends AbstractHTTPTask<Task>{
        private final ServerEndpoint serverEndpoint;
        private boolean requestHasPayload;
        protected Task(ServerEndpoint serverEndpoint, Client client, AccessLog accessLog){
            super(requestLineLimit, requestHeaderLimit, requestHeadersLimit,
                    accessLog, supportsProxyConnectionHeader,
                    requestFilters, responseFilters, errorFilters);
            this.serverEndpoint = serverEndpoint;
            this.client = client;
            client.setTimeout(ioTimeout);
        }

        public ServerEndpoint getServerEndpoint(){
            return serverEndpoint;
        }

        @Override
        public ClientEndpoint getClientEndpoint(){
            return new ClientEndpoint((InetSocketAddress)client.connectedTo());
        }

        /*-------------------------------------------------[ Set Response ]---------------------------------------------------*/

        public void setResponse(Response response){
            this.response = response;
        }

        private void populateErrorResponse(){
            response = new Response();
            response.setStatus(getErrorCode(), getErrorPhrase());
            if(thr!=null && showStackTrace){
                try{
                    response.setPayload(new EncodablePayload<>(MediaType.TEXT_PLAIN.toString(), thr, ThrowableEncoder.INSTANCE), true);
                }catch(IOException ex){
                    throw new ImpossibleException(ex);
                }
            }
        }

        /*-------------------------------------------------[ Flow ]---------------------------------------------------*/

        @Override
        protected void prepareRequest(){
            connectionStatus = ConnectionStatus.OPEN;
            sending100Continue = false;
            request = new Request();
            response = null;
            beginTime = System.currentTimeMillis();
            endTime = -1;
            readMessage(request);
        }

        @Override
        protected void addTrackingFilters(){
            if(Version.HTTP_1_1.compareTo(request.version)>=0){
                Expect expectation = request.getExpectation();
                if(expectation==Expect.CONTINUE_100){
                    try{
                        client.inPipeline.push(new ReadTrackingInputFilter(this::send100Continue));
                    }catch(IOException ex){
                        throw new ImpossibleException();
                    }
                }else if(expectation!=null)
                    errorCode = Status.EXPECTATION_FAILED;
            }
        }

        private boolean sending100Continue;
        private void send100Continue(TrackingInputFilter trackingFilter){
            client.makeActive();
            sending100Continue = true;
            new WriteBuffer(ByteBuffer.wrap(CONTINUE_100)).start(client.out(), this::sent100Continue);
        }

        private void sent100Continue(Throwable thr, boolean timeout){
            if(thr!=null || timeout){
                connectionStatus = ConnectionStatus.ABORTED;
                client.closeForcefully();
                notifyUser(thr, timeout ? Status.RESPONSE_TIMEOUT : -1);
            }else{
                sending100Continue = false;
                if(finishListener!=null)
                    finish(finishListener);
            }
        }

        @Override
        protected void readMessageCompleted(Throwable thr, boolean timeout){
            if(thr!=null || timeout){
                if(thr==IgnorableEOFException.INSTANCE){
                    connectionStatus = ConnectionStatus.CLOSED;
                    client.close();
                    return;
                }
                if(thr instanceof SocketException){
                    connectionStatus = ConnectionStatus.ABORTED;
                    client.closeForcefully();
                    finished();
                }

                keepAlive = false;
                if(timeout)
                    resume(Status.REQUEST_TIMEOUT);
                else if(thr instanceof LineOverflowException){
                    LineOverflowException lofe = (LineOverflowException)thr;
                    resume(lofe.line() == 0 ? Status.REQUEST_URI_TOO_LONG : Status.REQUEST_HEADER_FIELDS_TOO_LARGE);
                }else if(thr instanceof NotImplementedException)
                    resume(Status.NOT_IMPLEMENTED, thr.getMessage());
                else
                    resume(thr);
            }else{
                requestHasPayload = request.getPayload().getContentLength()!=0;
                _resume();
            }
        }

        @Override
        protected void prepareResponse(){
            try{
                listener.accept(this);
            }catch(Throwable thr){
                Reactor.current().handleException(thr);
                if(isOpen())
                    resume(thr);
            }
        }

        @Override
        public void finish(FinishListener<? super Task> finishListener){
            if(!isOpen())
                throw new AssertionError("client is already closed");
            client.makeActive();
            if(response==null && isSuccess())
                throw new AssertionError("response must be set before calling finish(...)");
            this.finishListener = finishListener;
            if(sending100Continue)
                return;
            _resume();
        }

        protected void deliverResponse(){
            if(response==null){
                assert !isSuccess();
                populateErrorResponse();
            }
            response.version = requestVersion;

            boolean drain = false;
            if(keepAlive && requestHasPayload){
                if(client.in().isEOF())
                    drain = true;
                else
                    keepAlive = false;
            }
            response.setKeepAlive(keepAlive);

            if(setDateHeader && response.headers.get(Headers.DATE.name)==null)
                response.headers.set(Headers.DATE.name, HTTPDate.currentDate());

            if(drain)
                drainInputFilters(null, false);
            else
                writeMessage(response);
        }

        @Override
        protected void drainedInputFilters(Throwable thr, boolean timeout){
            if(thr!=null || timeout){
                connectionStatus = ConnectionStatus.ABORTED;
                client.closeForcefully();
                notifyUser(thr, timeout ? Status.REQUEST_TIMEOUT : -1);
            }else
                writeMessage(response);
        }

        @Override
        protected void writeMessageCompleted(Throwable thr, boolean timeout){
            if(thr!=null || timeout){
                connectionStatus = ConnectionStatus.ABORTED;
                client.closeForcefully();
            }
            if(isOpen() && !keepAlive)
                connectionStatus = ConnectionStatus.CLOSED;
            notifyUser(thr, timeout ? Status.RESPONSE_TIMEOUT : -1);
            if(isOpen()){ // not closed && not stolen
                if(keepAlive){
                    client.taskCompleted();
                    client.invokeLater(this::restart);
                }else
                    client.close();
            }
        }

        @Override
        protected void notifyUser(Throwable thr, int timeoutCode){
            finished();
            super.notifyUser(thr, timeoutCode);
        }

        private void finished(){
            endTime = System.currentTimeMillis();
            if(requestHasPayload)
                requestPayloadSize = client.in().stopInputMetric();
            if(accessLog!=null){
                accessLogRecord.taskFinished(this);
                accessLogRecord.publish();
            }
        }

        @Override
        public String toString(){
            return "ServerTask["+getServerEndpoint()+"]";
        }
    }

    public static class Defaults{
        public static boolean SUPPORTS_PROXY_CONNECTION_HEADER = false;

        public static int REQUEST_LINE_LIMIT = 7*1024;
        public static int REQUEST_HEADER_LIMIT = 10*1024;
        public static int REQUEST_HEADERS_LIMIT = 25*1024;
        public static boolean SHOW_STACK_TRACE = true;
        public static boolean SET_DATE_HEADER = true;

        private Defaults(){}

        public static void apply(HTTPServer service){
            service.supportsProxyConnectionHeader = SUPPORTS_PROXY_CONNECTION_HEADER;
            service.ioTimeout = Client.Defaults.getTimeout();
            service.requestLineLimit = REQUEST_LINE_LIMIT;
            service.requestHeaderLimit = REQUEST_HEADER_LIMIT;
            service.requestHeadersLimit = REQUEST_HEADERS_LIMIT;
            service.showStackTrace = SHOW_STACK_TRACE;
            service.setDateHeader = SET_DATE_HEADER;
        }
    }
}
