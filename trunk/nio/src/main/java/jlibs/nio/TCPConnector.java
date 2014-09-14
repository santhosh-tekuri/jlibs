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
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import static java.nio.channels.SelectionKey.OP_CONNECT;
import static jlibs.nio.Debugger.DEBUG;
import static jlibs.nio.Debugger.println;

/**
 * @author Santhosh Kumar Tekuri
 */
public class TCPConnector extends NBChannel<SocketChannel>{
    public final long id;
    private final SelectionKey selectionKey;
    public TCPConnector() throws IOException{
        super(SocketChannel.open());
        selectionKey = selectable.register(reactor.selector, 0, this);
        id = ++reactor.lastConnectID;
        ++reactor.connectionPending;
        uniqueID = "c"+id;
    }

    private Listener listener;
    public static interface Listener{
        public void process(TCPConnector connector);
    }

    private SocketAddress address;
    public void connect(SocketAddress address, Listener listener){
        this.address = address;
        this.listener = listener;
        if(DEBUG)
            println(this+".connect()");
        boolean notify;
        try{
            notify = selectable.connect(address);
            if(DEBUG && notify)
                println("connected");
        }catch(IOException ex){
            if(DEBUG)
                println("connectFailed: "+ex);
            connectError = ex;
            notify = true;
        }
        if(notify){
            try{
                listener.process(this);
            }catch(Throwable thr){
                reactor.handleException(thr);
            }
        }else{
            selectionKey.interestOps(OP_CONNECT);
            if(SO_TIMEOUT>0)
                reactor.startTimer(this, SO_TIMEOUT);
        }
    }

    private IOException connectError;
    public TCPConnection getTCPConnection() throws IOException{
        if(connectError!=null)
            throw connectError;
        return new TCPConnection(id, selectable, selectionKey);
    }

    @Override
    protected void process(boolean timeout){
        boolean notify;
        try{
            if(timeout)
                throw new ConnectException("timed out");
            notify = selectable.finishConnect();
            if(DEBUG)
                println("finishConnect = "+notify);
            if(notify)
                selectionKey.interestOps(0);
        }catch(IOException ex){
            if(DEBUG)
                println("connectFailed: "+ex);
            connectError = ex;
            notify = true;
        }
        if(notify){
            try{
                listener.process(this);
            }catch(Throwable thr){
                reactor.handleException(thr);
            }
        }
    }

    @Override
    public void shutdown(){
        if(isOpen()){
            --reactor.connectionPending;
            super.shutdown();
        }
    }

    @Override
    public String toString(){
        StringBuilder buf = new StringBuilder();
        buf.append("TCPConnector").append(id).append("[");
        InetSocketAddress address = (InetSocketAddress)this.address;
        if(selectable.isConnected())
            buf.append("c:");
        else
            buf.append("?:");
        if(address==null)
            buf.append("?");
        else
            buf.append(address.getHostString()).append(":").append(address.getPort());
        buf.append("]");
        return buf.toString();
    }

    /*-------------------------------------------------[ Options ]---------------------------------------------------*/

    public static long SO_TIMEOUT;
}
