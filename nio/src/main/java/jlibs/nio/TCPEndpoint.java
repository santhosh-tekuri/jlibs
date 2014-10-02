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

import jlibs.core.net.Protocol;
import jlibs.core.net.SSLUtil;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static jlibs.nio.Debugger.DEBUG;
import static jlibs.nio.Debugger.println;

/**
 * @author Santhosh Kumar Tekuri
 */
public class TCPEndpoint{
    public final String host;
    public final int port;
    private final String toString;

    public TCPEndpoint(String host, int port){
        this.host = requireNonNull(host, "host==null");
        this.port = port;
        toString = host+':'+port;
    }

    public TCPEndpoint(int port){
        this("0.0.0.0", port);
    }

    public TCPEndpoint(InetSocketAddress address){
        this(address.getHostString(), address.getPort());
    }

    public TCPEndpoint(String url) throws GeneralSecurityException, SSLException{
        Protocol protocol = Protocol.TCP;
        int port = -1;
        int colon = url.indexOf("://");
        if(colon!=-1){
            protocol = Protocol.valueOf(url.substring(0, colon).toUpperCase());
            url = url.substring(colon+"://".length());
        }
        colon = url.indexOf(':');
        if(colon!=-1){
            port = Integer.parseInt(url.substring(colon+1));
            url = url.substring(0, colon);
        }
        if(port==-1)
            port = protocol.port();

        this.host = url;
        this.port = port;
        toString = host+':'+port;
        if(protocol.secured())
            sslContext = SSLUtil.defaultContext();
    }

    public InetSocketAddress socketAddress(){
        return new InetSocketAddress(host, port);
    }

    public SSLContext sslContext;

    @Override
    public final String toString(){
        return toString;
    }

    public TCPServer startServer(TCPServer.Listener listener) throws IOException{
        TCPServer server = new TCPServer(new TCPServer.Listener(){
            @Override
            public void accept(TCPConnection con){
                try{
                    if(sslContext!=null){
                        SSLEngine engine = sslContext.createSSLEngine();
                        engine.setUseClientMode(false);
                        new SSLSocket(con.in(), con.out(), engine);
                    }
                }catch(Throwable thr){
                    Reactor.current().handleException(thr);
                    con.close();
                    return;
                }
                listener.accept(con);
            }

            @Override
            public String toString(){
                return listener.getClass().getSimpleName();
            }
        });
        try{
            server.bind(socketAddress());
        }catch(Throwable thr){
            server.close();
            throw thr;
        }
        return server;
    }

    public void getConnection(Consumer<Result<Connection>> listener, Proxy proxy){
        Reactor reactor = Reactor.current();
        while(true){
            Connection con = reactor.connectionPool.remove(toString());
            if(con==null)
                break;
            ByteBuffer buffer = reactor.allocator.allocate(1);
            int read = -1;
            try{
                read = con.in().read(buffer);
            }catch(Throwable ignore){
                // ignore.printStackTrace();
            }
            reactor.allocator.free(buffer);
            assert read<=0;
            if(read==-1){
                if(DEBUG)
                    println(con+".isBroken=true");
                con.close();
            }else{
                listener.accept(new Result<>(con));
                return;
            }
        }
        newConnection(listener, proxy);
    }

    public void newConnection(Consumer<Result<Connection>> listener, Proxy proxy){
        if(proxy==null)
            new NewConnection(listener).start();
        else
            proxy.getConnection(this, listener);
    }

    private class NewConnection implements TCPConnector.Listener{
        private Consumer<Result<Connection>> listener;
        private NewConnection(Consumer<Result<Connection>> listener){
            this.listener = listener;
        }

        public void start(){
            TCPConnector connector = null;
            try{
                connector = new TCPConnector();
                connector.connect(socketAddress(), this);
            }catch(Throwable thr){
                if(connector!=null)
                    connector.close();
                listener.accept(new Result<>(thr));
            }
        }

        @Override
        public void process(TCPConnector connector){
            TCPConnection con;
            try{
                con = connector.getTCPConnection();
            }catch(Throwable thr){
                connector.close();
                listener.accept(new Result<>(thr));
                return;
            }
            try{
                if(sslContext!=null){
                    SSLEngine engine = sslContext.createSSLEngine();
                    engine.setUseClientMode(true);
                    new SSLSocket(con.in(), con.out(), engine);
                }
            }catch(Throwable thr){
                con.shutdown();
                listener.accept(new Result<>(thr));
                return;
            }
            listener.accept(new Result<>(con));
        }
    }
}
