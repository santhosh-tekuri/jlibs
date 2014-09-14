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
import jlibs.nio.http.msg.Method;
import jlibs.nio.http.msg.Request;
import jlibs.nio.http.msg.Response;
import jlibs.nio.http.msg.Status;
import jlibs.nio.http.util.HTTPURL;
import jlibs.nio.listeners.InputException;
import jlibs.nio.listeners.OutputException;
import jlibs.nio.listeners.Pump;
import jlibs.nio.util.BufferAllocator;

import javax.net.ssl.SSLException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.SocketTimeoutException;
import java.nio.BufferOverflowException;
import java.security.GeneralSecurityException;
import java.util.function.Consumer;

/**
 * @author Santhosh Kumar Tekuri
 */
public class HTTPProxyServer{
    public final HTTPServer server;
    public final HTTPClient client;

    public HTTPProxyServer(TCPEndpoint endpoint){
        server = new HTTPServer(endpoint);
        client = new HTTPClient();
        server.supportsProxyConnectionHeader = true;
        server.listener = new Listener();
    }

    public void start() throws IOException{
        server.start();
    }

    public void stop(){
        server.stop();
    }

    private class Listener implements RequestListener, ResponseListener{
        @Override
        public boolean process(ServerExchange exchange) throws Exception{
            Request request = exchange.getRequest();
            if(request.method==Method.CONNECT)
                new ConnectHandler(exchange).start(client.proxy);
            else{
                HTTPURL url;
                try{
                    url = new HTTPURL(request.uri);
                }catch(Throwable thr){
                    throw Status.BAD_REQUEST.with("Bad URL", thr);
                }
                request.uri = url.path;
                ClientExchange clientExchange = client.newExchange(url.createEndpoint());
                clientExchange.attach(exchange);
                clientExchange.setRequest(request);
                clientExchange.execute(this);
            }
            return false;
        }

        @Override
        public void process(ClientExchange exchange, Throwable thr) throws Exception{
            ServerExchange serverExchange = exchange.attachment();
            if(thr==null){
                serverExchange.setResponse(exchange.getResponse());
                serverExchange.resume();
            }else{
                if(thr instanceof BufferOverflowException){
                    Request request = exchange.getRequest();
                    int lineSize = request.method.name.length()+1+request.uri.length()+1+request.version.text.length()+2;
                    if(lineSize>BufferAllocator.Defaults.CHUNK_SIZE)
                        thr = Status.REQUEST_URI_TOO_LONG;
                }else if(thr instanceof InputException)
                    thr = thr.getCause();
                else if(thr instanceof OutputException){
                    thr = thr.getCause();
                    if(thr instanceof SocketTimeoutException)
                        thr = Status.GATEWAY_TIMEOUT;
                    else
                        thr = Status.BAD_GATEWAY.with(thr);
                }else if(exchange.getConnectionStatus()==null)
                    thr = Status.BAD_GATEWAY.with(thr);
                serverExchange.resume(thr);
            }
        }
    }

    private static class ConnectHandler implements Consumer<Result<Connection>>, ServerCallback{
        private ServerExchange exchange;
        private Connection con;
        private ConnectHandler(ServerExchange exchange){
            this.exchange = exchange;
        }

        public boolean start(Proxy proxy) throws GeneralSecurityException, SSLException{
            Request request = exchange.getRequest();
            TCPEndpoint endpoint = new TCPEndpoint(request.uri);
            if(endpoint.port==-1)
                throw Status.BAD_REQUEST.with("Port Missing");
            endpoint.newConnection(this, proxy);
            return false;
        }

        @Override
        public void accept(Result<Connection> result){
            try{
                con = result.get();
            }catch(Throwable thr){
                exchange.resume(Status.BAD_GATEWAY.with(thr));
                return;
            }
            exchange.setResponse(new Response());
            exchange.setCallback(this);
            exchange.resume();
        }

        @Override
        public void completed(ServerExchange exchange, Throwable thr){
            if(thr==null)
                Pump.startTunnel(exchange.stealConnection(), con);
            else
                con.close();
        }
    }

    public static void main(String[] args) throws Exception{
        System.setOut(new PrintStream(new FileOutputStream("/Users/santhosh/Desktop/proxy.log")));
        Reactors.start(1);
        HTTPProxyServer proxyServer = new HTTPProxyServer(new TCPEndpoint(args[0]));
        proxyServer.client.proxy = null;
        proxyServer.start();
    }
}
