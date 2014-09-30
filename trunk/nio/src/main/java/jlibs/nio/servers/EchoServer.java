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

import jlibs.nio.Reactors;
import jlibs.nio.TCPConnection;
import jlibs.nio.TCPEndpoint;
import jlibs.nio.TCPServer;
import jlibs.nio.listeners.Pump;

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
        Pump.start(con);
    }

    public static void main(String[] args) throws Exception{
        Reactors.start(1);
        new EchoServer(new TCPEndpoint(args[0])).start();
    }
}
