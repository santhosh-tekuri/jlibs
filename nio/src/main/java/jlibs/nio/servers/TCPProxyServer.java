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

import jlibs.nio.*;
import jlibs.nio.listeners.Pump;

import java.io.IOException;

/**
 * @author Santhosh Kumar Tekuri
 */
public class TCPProxyServer implements TCPServer.Listener{
    public final TCPEndpoint localEndpoint;
    public final TCPEndpoint remoteEndpoint;
    public TCPProxyServer(TCPEndpoint localEndpoint, TCPEndpoint remoteEndpoint){
        this.localEndpoint = localEndpoint;
        this.remoteEndpoint = remoteEndpoint;
    }

    private TCPServer server;
    public void start() throws IOException{
        server = localEndpoint.startServer(this);
    }

    public void stop(){
        server.close();
    }

    @Override
    public void accept(TCPConnection con1){
        remoteEndpoint.newConnection(result -> {
            try{
                Connection con2 = result.get();
                Pump.startTunnel(con1, con2);
            }catch(Throwable thr){
                Reactor.current().handleException(thr);
                con1.close();
            }
        }, null);
    }

    public static void main(String[] args) throws IOException{
        Reactors.start(1);
        TCPEndpoint localEndpoint = new TCPEndpoint(8080);
        TCPEndpoint remoteEndpoint = new TCPEndpoint("www.mvnrepository.com", 80);
        new TCPProxyServer(localEndpoint, remoteEndpoint).start();
    }
}
