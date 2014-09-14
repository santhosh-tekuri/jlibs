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

package jlibs.nio.servers;

import jlibs.core.net.SSLUtil;
import jlibs.nio.*;
import jlibs.nio.listeners.Pump;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.IOException;

/**
 * @author Santhosh Kumar Tekuri
 */
public class EchoServer implements TCPServer.Listener{
    public final TCPEndpoint endpoint;
    public EchoServer(TCPEndpoint endpoint){
        this.endpoint = endpoint;
    }

    private TCPServer server;
    public void start() throws IOException{
        server = endpoint.startServer(this);
    }

    public void stop(){
        server.close();
    }

    @Override
    public void accept(TCPConnection con){
        try{
            SSLContext sslContext = SSLUtil.defaultContext();
            SSLEngine sslEngine = sslContext.createSSLEngine();
            sslEngine.setUseClientMode(false);
            new SSLSocket(con.in(), con.out(), sslEngine);
        }catch(Throwable thr){
            thr.printStackTrace();
            return;
        }
        Pump.start(con);
    }

    public static void main(String[] args) throws IOException{
        Reactors.start(1);
        new EchoServer(new TCPEndpoint(8080)).start();
    }
}
