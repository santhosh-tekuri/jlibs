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

import jlibs.core.lang.ImpossibleException;
import jlibs.nio.*;
import jlibs.nio.channels.InputChannel;
import jlibs.nio.channels.filters.CloseTrackingInputFilter;
import jlibs.nio.channels.filters.TrackingInputFilter;
import jlibs.nio.http.msg.Request;
import jlibs.nio.http.msg.Response;
import jlibs.nio.http.msg.Status;
import jlibs.nio.http.msg.Version;

import java.io.IOException;

import static jlibs.nio.http.msg.Headers.HOST;
import static jlibs.nio.http.msg.Version.HTTP_1_1;

/**
 * @author Santhosh Kumar Tekuri
 */
public class HTTPClient extends HTTPService{
    public final ClientEndpoint endpoint;
    public Proxy proxy;
    public AccessLog accessLog;

    public long connectTimeout;
    public long ioTimeout;
    public long keepAliveTimeout;

    public int responseLineLimit;
    public int responseHeaderLimit;
    public int responseHeadersLimit;

    public HTTPClient(){
        this(null);
    }

    public HTTPClient(ClientEndpoint endpoint){
        this.endpoint = endpoint;
        proxy = Proxy.DEFAULTS.get(Proxy.Type.HTTP);
        if(proxy==null)
            proxy = Proxy.DEFAULTS.get(Proxy.Type.SOCKS);
        Defaults.apply(this);
    }

    public Task newTask(ClientEndpoint endpoint, Request request){
        if(endpoint==null)
            throw new IllegalArgumentException("endpoint==null");
        return new Task(endpoint, request, accessLog);
    }

    public Task newTask(Request request){
        return newTask(endpoint, request);
    }

    public Task newTask(){
        return newTask(new Request());
    }

    public class Task extends AbstractHTTPTask<Task>{
        private ClientEndpoint endpoint;
        private boolean responseHasPayload;

        private Task(ClientEndpoint endpoint, Request request, AccessLog accessLog){
            super(responseLineLimit, responseHeaderLimit, responseHeadersLimit,
                    accessLog, false,
                    requestFilters, responseFilters, errorFilters);
            this.endpoint = endpoint;
            this.request = request;
            resume();
        }

        @Override
        public ClientEndpoint getClientEndpoint(){
            return endpoint;
        }

        boolean ownsAccessLog = true;
        public void setAccessLog(HTTPServer.Task serverTask){
            ownsAccessLog = false;
            this.accessLog = serverTask.accessLog;
            this.accessLogRecord = serverTask.accessLogRecord;
        }

        @Override
        protected void prepareRequest(){
            connectionStatus = ConnectionStatus.OPEN;
        }

        @Override
        public void finish(FinishListener<? super Task> finishListener){
            beginTime = System.currentTimeMillis();
            this.finishListener = finishListener;
            response = null;
            retryEndpoint = null;
            _resume();
        }

        @Override
        protected void prepareResponse(){
            if(client==null)
                endpoint.borrow(this::borrowed, proxy, connectTimeout);
            else
                borrowed(new Result<>(client));
        }

        private void borrowed(Result<Client> result){
            try{
                client = result.get();
            }catch(Throwable thr){
                notifyUser(thr, -1);
                return;
            }
            connectionStatus = ConnectionStatus.OPEN;
            client.setTimeout(ioTimeout);
            if(request.version==HTTP_1_1){
                int port = endpoint.port;
                String hostPort = port==80 || port==443 ? endpoint.host : endpoint.toString();
                request.headers.set(HOST, hostPort);
            }

            if(keepAliveTimeout<0)
                requestKeepAlive = request.isKeepAlive();
            else if(request.headers.get("Upgrade")!=null)
                requestKeepAlive = true;
            else
                request.setKeepAlive(requestKeepAlive=keepAliveTimeout!=0);

            expectingContinue100 = Version.HTTP_1_1.compareTo(request.version)>=0 && request.getExpectation()!=null;
            writeMessage(request);
        }

        private boolean expectingContinue100;

        @Override
        protected void writePayload(Throwable thr, boolean timeout){
            if(thr==null && !timeout && expectingContinue100){
                response = new Response();
                socketInput = client.in();
                readMessage(response);
            }else
                super.writePayload(thr, timeout);
        }

        private InputChannel socketInput;

        @Override
        protected void writeMessageCompleted(Throwable thr, boolean timeout){
            if(thr!=null || timeout){
                connectionStatus = ConnectionStatus.ABORTED;
                client.closeForcefully();
                notifyUser(thr, timeout ? Status.REQUEST_TIMEOUT : -1);
            }else{
                response = new Response();
                socketInput = client.in();
                readMessage(response);
            }
        }

        @Override
        protected void readMessageCompleted(Throwable thr, boolean timeout){
            if(thr!=null || timeout){
                connectionStatus = ConnectionStatus.ABORTED;
                client.closeForcefully();
                notifyUser(thr, timeout ? Status.RESPONSE_TIMEOUT : -1);
            }else{
                if(expectingContinue100){
                    expectingContinue100 = false;
                    if(response.statusCode==Status.CONTINUE){
                        response = null;
                        message = request;
                        writePayload(null, false);
                        return;
                    }
                }
                responseHasPayload = response.getPayload().contentLength!=0;
                if(!responseHasPayload){
                    assert !(client.in() instanceof CloseTrackingInputFilter);
                    if(isKeepAlive())
                        endpoint.returnBack(client, Math.abs(keepAliveTimeout));
                    else
                        client.close();
                }
                _resume();

            }
        }

        @Override
        protected void deliverResponse(){
            notifyUser(null, -1);
        }

        private ClientEndpoint retryEndpoint;

        public void retry(){
            retry(finishListener);
        }

        public void retry(ClientEndpoint endpoint){
            retry(endpoint, finishListener);
        }

        public void retry(FinishListener<? super Task> finishListener){
            retry(getClientEndpoint(), finishListener);
        }

        public void retry(ClientEndpoint endpoint, FinishListener<? super Task> finishListener){
            if(Debugger.HTTP)
                Debugger.println(this+".retry("+endpoint+")");
            response = null;
            if(!isOpen()){
                this.endpoint = endpoint;
                client = null;
                restart();
                finish(finishListener);
            }else if(client.inPipeline.empty()){
                if(endpoint==this.endpoint)
                    client.reactor.clientPool.remove(client);
                else{
                    this.endpoint = endpoint;
                    client = null;
                }
                restart();
                finish(finishListener);
            }else{
                retryEndpoint = endpoint;
                this.finishListener = finishListener;
                if(client.in() instanceof CloseTrackingInputFilter){
                    client.inPipeline.pop();
                    drainInputFilters(null, false);
                }
            }
        }

        @Override
        protected void addTrackingFilters(){
            try{
                client.inPipeline.push(new CloseTrackingInputFilter(this::responsePayloadClosed));
            }catch(IOException ex){
                throw new ImpossibleException(ex);
            }
        }

        private void responsePayloadClosed(TrackingInputFilter filter){
            if(Debugger.HTTP)
                Debugger.println(client.in()+".responsePayloadClosed{");
            commitAccessLog();
            if(filter.isEOF() && isKeepAlive())
                drainInputFilters(null, false);
            else{
                connectionStatus = ConnectionStatus.CLOSED;
                client.close();
                client = null;
            }
            if(Debugger.HTTP)
                Debugger.println("}");
        }

        @Override
        protected void drainedInputFilters(Throwable thr, boolean timeout){
            if(thr!=null || timeout){
                connectionStatus = ConnectionStatus.ABORTED;
                client.closeForcefully();
                if(retryEndpoint!=null){
                    retryEndpoint = null;
                    notifyUser(thr, timeout ? Status.RESPONSE_TIMEOUT : -1);
                }
            }else{
                assert !(client.in() instanceof CloseTrackingInputFilter);
                if(retryEndpoint!=null){
                    commitAccessLog();
                    if(retryEndpoint!=endpoint){
                        endpoint.returnBack(client, Math.abs(keepAliveTimeout));
                        client = null;
                    }
                    endpoint = retryEndpoint;
                    retryEndpoint = null;
                    restart();
                    finish(finishListener);
                }else
                    endpoint.returnBack(client, Math.abs(keepAliveTimeout));
            }
        }

        @Override
        protected void notifyUser(Throwable thr, int timeoutCode){
            if(thr!=null || timeoutCode!=-1 || response.getPayload().contentLength==0)
                commitAccessLog();
            super.notifyUser(thr, timeoutCode);
        }

        private void commitAccessLog(){
            endTime = System.currentTimeMillis();
            if(responseHasPayload)
                responsePayloadSize = socketInput.getInputMetric(); // use socketInput because inputfilters may not be drained yet
            if(accessLog!=null){
                accessLogRecord.taskFinished(this);
                if(ownsAccessLog)
                    accessLogRecord.publish();
            }
        }

        @Override
        public String toString(){
            return "ClientTask["+(isOpen() ? getClientEndpoint() : "?")+"]";
        }
    }

    public static class Defaults{
        public static long CONNECT_TIMEOUT = 60000L;

        // 0=turn off, +ve=turn on, -ve=respect what is there in request
        public static long KEEP_ALIVE_TIMEOUT = -60000L;

        public static int RESPONSE_LINE_LIMIT = 2*1024;
        public static int RESPONSE_HEADER_LIMIT = 10*1024;
        public static int RESPONSE_HEADERS_LIMIT = 25*1024;

        private Defaults(){}

        public static void apply(HTTPClient client){
            client.connectTimeout = CONNECT_TIMEOUT;
            client.ioTimeout = Client.Defaults.getTimeout();
            client.keepAliveTimeout = KEEP_ALIVE_TIMEOUT;
            client.responseLineLimit = RESPONSE_LINE_LIMIT;
            client.responseHeaderLimit = RESPONSE_HEADER_LIMIT;
            client.responseHeadersLimit = RESPONSE_HEADERS_LIMIT;
        }
    }
}
