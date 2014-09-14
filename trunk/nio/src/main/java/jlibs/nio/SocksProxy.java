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

package jlibs.nio;

import jlibs.nio.listeners.IOListener;
import jlibs.nio.listeners.Socks4Tunnel;
import jlibs.nio.listeners.Socks5Tunnel;
import jlibs.nio.listeners.Task;

import javax.net.ssl.SSLEngine;
import java.util.function.Consumer;

/**
 * @author Santhosh Kumar Tekuri
 */
public class SocksProxy extends Proxy{
    public static final String TYPE = "socks";

    public final int version;
    public SocksProxy(int version, TCPEndpoint endpoint){
        super(TYPE, endpoint);
        this.version = version;
    }

    @Override
    public void getConnection(TCPEndpoint endpoint, Consumer<Result<Connection>> listener){
        this.endpoint.newConnection(new Listener(endpoint, listener), null);
    }

    private class Listener implements Consumer<Result<Connection>>, IOListener.Callback<Connection>{
        private TCPEndpoint endpoint;
        private Consumer<Result<Connection>> listener;
        private Listener(TCPEndpoint endpoint, Consumer<Result<Connection>> listener){
            this.endpoint = endpoint;
            this.listener = listener;
        }

        @Override
        public void accept(Result<Connection> result){
            Connection con;
            try{
                con = result.get();
            }catch(Throwable thr){
                listener.accept(result);
                return;
            }
            try{
                Task task;
                if(version==4)
                    task = new Socks4Tunnel(user, endpoint.socketAddress());
                else
                    task = new Socks5Tunnel(user, password, endpoint.socketAddress());
                new IOListener().setCallback(this, con).start(task, con);
            }catch(Throwable thr){
                con.close();
                listener.accept(new Result<>(thr));
            }
        }

        @Override
        public void completed(Connection con, Throwable thr){
            if(thr==null){
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
