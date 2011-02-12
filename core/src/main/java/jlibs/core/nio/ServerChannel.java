/**
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

package jlibs.core.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Santhosh Kumar T
 */
public class ServerChannel extends NIOChannel {
    private static AtomicLong ID_GENERATOR = new AtomicLong();

    protected final ServerSocketChannel channel;
    public ServerChannel() throws IOException{
        super(ID_GENERATOR.incrementAndGet(), ServerSocketChannel.open());
        this.channel = (ServerSocketChannel)super.channel;
    }

    @Override
    public ServerSocketChannel realChannel(){
        return channel;
    }

    public void bind(InetSocketAddress endpoint) throws IOException{
        realChannel().socket().bind(endpoint);
    }

    public void bind(SocketAddress endpoint, int backlog) throws IOException{
        realChannel().socket().bind(endpoint, backlog);
    }

    public ClientChannel accept(NIOSelector nioSelector) throws IOException{
        SocketChannel socketChannel = channel.accept();
        if(socketChannel==null)
            return null;
        return new ClientChannel(nioSelector, socketChannel, realChannel().socket().getLocalSocketAddress());
    }

    public void register(NIOSelector nioSelector) throws IOException{
        if(channel.keyFor(nioSelector.selector)==null){
            nioSelector.validate();
            channel.register(nioSelector.selector, SelectionKey.OP_ACCEPT, this);
            nioSelector.servers.add(this);
        }
    }

    public void unregister(NIOSelector nioSelector) throws ClosedChannelException{
        SelectionKey key = channel.keyFor(nioSelector.selector);
        if(key!=null){
            key.cancel();
            nioSelector.servers.remove(this);
        }
    }

    @Override
    public String toString(){
        return "ServerChannel@"+id;
    }
}
