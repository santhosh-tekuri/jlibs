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

import jlibs.nio.channels.filters.SSLFilter;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ServerEndpoint extends Endpoint{
    public ServerEndpoint(String host, int port){
        super(host, port);
    }

    public ServerEndpoint(InetSocketAddress address){
        super(address);
    }

    public ServerEndpoint(int port){
        super("0.0.0.0", port);
    }

    public ServerEndpoint(String url) throws GeneralSecurityException, SSLException{
        super(url);
    }

    private Server server;
    public int backlog;
    public void start(Server.Listener listener) throws IOException{
        server = new Server();
        server.attach(this);
        server.setListener((s, client) -> {
            try{
                if(sslContext != null){
                    SSLEngine engine = sslContext.createSSLEngine();
                    engine.setUseClientMode(false);
                    client.pipeline.push(new SSLFilter(engine));
                }
                listener.accepted(s, client);
            }catch(Throwable thr){
                Reactor.current().handleException(thr);
                client.closeForcefully();
            }
        });
        server.bind(socketAddress(), backlog);
        Reactors.register(server);
    }

    public void stop() throws IOException{
        if(server!=null && server.isOpen()){
            server.close();
            server = null;
        }
    }
}
