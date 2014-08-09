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

import jlibs.nio.*;
import jlibs.nio.async.InputChannelException;
import jlibs.nio.async.OutputChannelException;
import jlibs.nio.async.Pump;
import jlibs.nio.http.filters.CheckBasicAuthentication;
import jlibs.nio.http.filters.CheckDigestAuthentication;
import jlibs.nio.http.msg.*;
import jlibs.nio.http.util.HTTPURL;
import jlibs.nio.util.NIOUtil;

import java.io.IOException;

import static jlibs.nio.http.msg.Method.CONNECT;
import static jlibs.nio.http.msg.Status.*;
import static jlibs.nio.http.msg.Status.RESPONSE_TIMEOUT;

/**
 * @author Santhosh Kumar Tekuri
 */
public class HTTPProxyServer{
    public final HTTPServer httpServer = new HTTPServer(this::accept);
    public final HTTPClient httpClient = new HTTPClient();
    public AccessLog accessLog;
    public boolean addXForwardedFor = true;

    public HTTPProxyServer(){
        httpClient.keepAliveTimeout = +60000L;
    }

    public void start(ServerEndpoint serverEndpoint) throws IOException{
        httpServer.accessLog = accessLog;
        httpServer.supportsProxyConnectionHeader = true;
        httpServer.start(serverEndpoint);
    }

    public void stop() throws IOException{
        httpServer.stop();
    }

    private void accept(HTTPServer.Task task){
        if(task.isSuccess()){
            Request request = task.getRequest();
            if(request.method==CONNECT)
                new ConnectTask(task).start();
            else
                processNonConnect(task);
        }else if(task.isOpen())
            task.finish(this::finished);
    }

    private void connectFailed(HTTPServer.Task serverTask, Throwable thr, boolean timeout){
        String reason = thr==null ? "Connection Timeout" : "Connection Failure";
        serverTask.resume(BAD_GATEWAY, reason, thr);
    }

    private void processNonConnect(HTTPServer.Task serverTask){
        Request request = serverTask.getRequest();
        HTTPURL url;
        try{
            url = new HTTPURL(request.uri);
        }catch(Exception ex){
            serverTask.resume(BAD_REQUEST, ex);
            return;
        }
        request.uri = url.path;

        if(addXForwardedFor){
            String clientIP = serverTask.getClientEndpoint().host;
            Header header = request.headers.get(Headers.X_FORWARDED_FOR);
            if(header==null)
                request.headers.set(Headers.X_FORWARDED_FOR, clientIP);
            else{
                while(header.sameNext()!=null)
                    header = header.sameNext();
                header.setValue(header.getValue()+", "+clientIP);
            }
        }

        try{
            HTTPClient.Task clientTask = httpClient.newTask(url.createClientEndpoint(), request);
            clientTask.attach(serverTask);
            clientTask.setAccessLog(serverTask);
            clientTask.finish(this::sendResponse);
        }catch(Throwable thr){
            connectFailed(serverTask, thr, false);
        }
    }

    private void sendResponse(HTTPClient.Task clientTask){
        HTTPServer.Task serverTask = clientTask.attachment();
        clientTask.attach(null);

        if(clientTask.isSuccess()){
            if(serverTask.isOpen()){ // might have closed if failed to send 100-continue response
                Response response = clientTask.getResponse();
                serverTask.setResponse(response);
                serverTask.finish(this::finished);
            }
        }else{
            Throwable error = clientTask.getError();
            if(NIOUtil.isConnectionFailure(error))
                connectFailed(serverTask, error, false);
            else if(error instanceof InputChannelException){
                // error while reading request payload
                Reactor.current().handleException(error);
                assert !clientTask.isOpen();
                serverTask.close();
            }else{
                if(error instanceof OutputChannelException)
                    error = error.getCause();
                int errorCode = clientTask.getErrorCode();
                if(errorCode%100==6)
                    errorCode = errorCode==RESPONSE_TIMEOUT ? GATEWAY_TIMEOUT : BAD_GATEWAY;
                serverTask.resume(errorCode, clientTask.getErrorPhrase(), error);
            }
        }
    }

    private void finished(HTTPServer.Task serverTask){
        if(!serverTask.isSuccess()){
            Reactor.current().handleException(serverTask.getError());
            assert !serverTask.isOpen();
        }
    }

    private class ConnectTask{
        private HTTPServer.Task serverTask;
        private ConnectTask(HTTPServer.Task serverTask){
            this.serverTask = serverTask;
        }

        private Client buddy;
        public void start(){
            Request request = serverTask.getRequest();
            ClientEndpoint endpoint;
            try{
                endpoint = new ClientEndpoint(request.uri);
            }catch(Throwable thr){
                throw HTTPException.valueOf(BAD_REQUEST, "bad url address", thr);
            }
            endpoint.borrowNew(this::connectFinished, httpClient.connectTimeout);
        }

        private void connectFinished(Result<Client> result){
            try{
                buddy = result.get();
            }catch(Throwable thr){
                connectFailed(serverTask, thr, false);
                return;
            }

            Response response = new Response();
            response.statusCode = OK;
            response.reasonPhrase = "Connection Established";
            serverTask.setResponse(response);
            serverTask.finish(this::startTunnel);
        }

        private void startTunnel(HTTPServer.Task serverTask){
            buddy.makeActive();
            if(serverTask.isSuccess()){
                Client client = serverTask.stealClient();
                if(Debugger.HTTP)
                    Debugger.println("Tunnel("+client+" -> "+buddy+").start()");
                Pump.startTunnel(client, buddy);
            }else
                Reactor.current().handleException(serverTask.getError());
        }
    }

    public static void main(String[] args) throws Exception{
        Reactors.start(1);
        Reactors.enableJMX();
        HTTPProxyServer proxyServer = new HTTPProxyServer();
        proxyServer.addXForwardedFor = false;
        AccessLog accessLog = new AccessLog();
        String headers = "\n%<(request.headers)\n\n%>(request.headers)\n\n%<(response.headers)\n\n%>(response.headers)";
        accessLog.setFormat(true, "%>(execution.id) %Dms "+
                "SI(%<(request.head.size)+%<(request.payload.size)) "+
                "CO(%>(request.head.size)+%>(request.payload.size)) "+
                "CI(%<s:%<(response.head.size)+%<(response.payload.size))%<X "+
                "SO(%>s:%>(response.head.size)+%>(response.payload.size))%>X "+
                "Transferred:%S "+
                "\"%r\"");
        proxyServer.accessLog = accessLog;

//        proxyServer.httpServer.requestFilters.add(new CheckBasicAuthentication(true, "JLibs Proxy", basicCredentials -> {
//            return "admin".equals(basicCredentials.user) && "passwd".equals(basicCredentials.password);
//        }));
        proxyServer.httpServer.requestFilters.add(new CheckDigestAuthentication(true, "JLibs Proxy", user -> "passwd"));
        proxyServer.start(new ServerEndpoint(args[0]));
    }
}
