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

import jlibs.core.io.NullOutputStream;
import jlibs.nio.*;
import jlibs.nio.http.msg.Method;
import jlibs.nio.http.msg.Request;
import jlibs.nio.http.msg.Response;
import jlibs.nio.listeners.IOListener;
import jlibs.nio.listeners.WriteToOutputStream;

import javax.net.ssl.SSLEngine;
import java.util.function.Consumer;

/**
 * @author Santhosh Kumar Tekuri
 */
public class HTTPProxy extends Proxy{
    public static final String TYPE = "http";
    public HTTPProxy(TCPEndpoint endpoint){
        super(TYPE, endpoint);
    }

    @Override
    public void getConnection(TCPEndpoint endpoint, Consumer<Result<Connection>> listener){
        HTTPClient client = new HTTPClient();
        client.proxy = null;
        ClientExchange exchange = client.newExchange(this.endpoint);
        Request request = new Request();
        request.method = Method.CONNECT;
        request.uri = endpoint.toString();
        exchange.setRequest(request);
        exchange.execute(new Listener(endpoint, listener));
    }

    private class Listener implements ResponseListener, ClientCallback{
        private TCPEndpoint endpoint;
        private Consumer<Result<Connection>> listener;
        private Listener(TCPEndpoint endpoint, Consumer<Result<Connection>> listener){
            this.endpoint = endpoint;
            this.listener = listener;
        }

        @Override
        public void process(ClientExchange exchange, Throwable thr) throws Exception{
            if(thr==null){
                Response response = exchange.getResponse();
                if(response.status.isSuccessful()){
                    exchange.setCallback(this);
                    if(response.getPayload().getContentLength()!=0){
                        SocketPayload socketPayload = (SocketPayload)response.getPayload();
                        new IOListener().start(new WriteToOutputStream(NullOutputStream.INSTANCE, null), socketPayload.in, null);
                    }
                }else{
                    if(response.getPayload().getContentLength()!=0){
                        SocketPayload socketPayload = (SocketPayload)response.getPayload();
                        socketPayload.in.close();
                    }
                    listener.accept(new Result<>(response.status));
                }
            }else
                listener.accept(new Result<>(thr));
        }

        @Override
        public void completed(ClientExchange exchange, Throwable thr){
            if(thr==null){
                Connection con = exchange.stealConnection();
                try{
                    if(endpoint.sslContext!=null){
                        SSLEngine engine = endpoint.sslContext.createSSLEngine();
                        engine.setUseClientMode(true);
                        new SSLSocket(con.in(), con.out(), engine);
                    }
                }catch(Throwable thr1){
                    con.close();
                    listener.accept(new Result<>(thr1));
                    return;
                }
                listener.accept(new Result<>(con));
            }else
                listener.accept(new Result<>(thr));
        }
    }
}
