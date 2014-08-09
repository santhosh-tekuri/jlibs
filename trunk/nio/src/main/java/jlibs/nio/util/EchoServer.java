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

package jlibs.nio.util;

import jlibs.nio.Client;
import jlibs.nio.Reactors;
import jlibs.nio.Server;
import jlibs.nio.ServerEndpoint;
import jlibs.nio.async.ExecutionContext;
import jlibs.nio.async.Pump;

import java.io.IOException;

/**
 * @author Santhosh Kumar Tekuri
 */
public class EchoServer implements Server.Listener{
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
        new Pump(client.in(), client.out()).start(ExecutionContext.close(client));
    }

    public static void main(String[] args) throws Exception{
        Reactors.start(1);
        Reactors.enableJMX();
        new EchoServer().start(new ServerEndpoint(args[0]));
    }
}
