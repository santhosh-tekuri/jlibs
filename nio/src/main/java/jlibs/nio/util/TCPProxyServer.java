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

import jlibs.nio.*;
import jlibs.nio.async.Pump;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * @author Santhosh Kumar Tekuri
 */
public class TCPProxyServer implements Server.Listener{
    private ClientEndpoint remoteEndpoint;
    public TCPProxyServer(ClientEndpoint remoteEndpoint){
        this.remoteEndpoint = remoteEndpoint;
    }

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
        new ClientHandler(client).start();
    }

    private class ClientHandler implements Consumer<Result<Client>>{
        private Client client;
        private ClientHandler(Client client){
            this.client = client;
        }

        public void start(){
            remoteEndpoint.borrowNew(this, 0);
        }

        private Client buddy;
        @Override
        public void accept(Result<Client> result){
            try{
                buddy = result.get();
            }catch(Throwable thr){
                client.reactor.handleException(thr);
                client.close();
                return;
            }
            Pump.startTunnel(client, buddy);
        }
    }

    public static void main(String[] args) throws Exception{
        Reactors.start(1);
        Reactors.enableJMX();

        ServerEndpoint serverEndpoint = new ServerEndpoint(args[0]);
        ClientEndpoint remoteEndpoint = new ClientEndpoint(args[1]);

        new TCPProxyServer(remoteEndpoint).start(serverEndpoint);
    }
}
