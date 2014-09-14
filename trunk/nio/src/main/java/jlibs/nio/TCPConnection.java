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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * @author Santhosh Kumar Tekuri
 */
public class TCPConnection extends Connection<SocketChannel>{
    public final long id;
    public final TCPServer server;
    TCPConnection(TCPServer server, SocketChannel selectable) throws IOException{
        super(selectable, null);
        this.server = server;
        id = ++reactor.lastAcceptID;
        ++reactor.accepted;
        server.accepted.incrementAndGet();
        init();
    }

    TCPConnection(long id, SocketChannel selectable, SelectionKey selectionKey) throws IOException{
        super(selectable, selectionKey);
        server = null;
        this.id = id;
        --reactor.connectionPending;
        ++reactor.connected;
        init();
    }

    protected void init() throws IOException{
        uniqueID = (server==null ? "C" : "A")+id;
        Socket socket = selectable.socket();
        if(TCP_NODELAY!=null)
            socket.setTcpNoDelay(TCP_NODELAY);
        if(SO_LINGER!=null)
            socket.setSoLinger(SO_LINGER<0, SO_LINGER);
        if(SO_SNDBUF!=null)
            socket.setSendBufferSize(SO_SNDBUF);
        if(SO_RCVBUF!=null)
            socket.setReceiveBufferSize(SO_RCVBUF);
    }

    public SocketAddress connectedTo(){
        return selectable.socket().getRemoteSocketAddress();
    }

    @Override
    public String toString(){
        StringBuilder buf = new StringBuilder();
        buf.append("TCPConnection").append(id).append("[");
        InetSocketAddress address;
        if(server==null){
            buf.append("c:");
            address = (InetSocketAddress)connectedTo();
        }else{
            buf.append("a:");
            address = (InetSocketAddress)server.boundTo();
        }
        buf.append(address.getHostString()).append(":").append(address.getPort());
        buf.append("]");
        return buf.toString();
    }

    @Override
    void closing(){
        if(server==null)
            --reactor.connected;
        else{
            --reactor.accepted;
            server.accepted.decrementAndGet();
        }
    }

    @Override
    public void shutdown(){
        try{
            transport.close();
        }catch(IOException ex){
            reactor.handleException(ex);
        }
    }

    /*-------------------------------------------------[ Options ]---------------------------------------------------*/

    public static Boolean TCP_NODELAY;
    public static Integer SO_LINGER;
    public static Integer SO_RCVBUF;
    public static Integer SO_SNDBUF;
}
