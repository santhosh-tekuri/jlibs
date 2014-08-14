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

import jlibs.core.lang.ImpossibleException;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.Channel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Santhosh Kumar Tekuri
 */
public final class Server implements Channel, Reactor.ServerMXBean{
    final ServerSocketChannel channel;

    public Server() throws IOException{
        channel = ServerSocketChannel.open();
        try{
            channel.configureBlocking(false);
            Defaults.apply(this);
        }catch(IOException ex){
            try{
                channel.close();
            }catch(IOException ex1){
                // ignore
            }
            throw ex;
        }
    }

    /*-------------------------------------------------[ Binding ]---------------------------------------------------*/

    protected SocketAddress boundTo;

    public void bind(SocketAddress address) throws IOException{
        channel.bind(address, Defaults.BACKLOG==null ? 0 : Defaults.BACKLOG);
        bindSuccessful();
    }

    public void bind(SocketAddress address, int backlog) throws IOException{
        channel.bind(address, backlog);
        bindSuccessful();
    }

    private void bindSuccessful(){
        boundTo = channel.socket().getLocalSocketAddress();
        Reactors.serverCount.incrementAndGet();
        if(objectName!=null){
            disableJMX();
            enableJMX();
        }
    }

    public SocketAddress boundTo(){
        return channel.socket().getLocalSocketAddress();
    }

    /*-------------------------------------------------[ Listener ]---------------------------------------------------*/

    private Listener listener;
    public static interface Listener{
        public void accepted(Server server, Client client);
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    public Listener getListener(){
        return listener;
    }

    /*-------------------------------------------------[ Process ]---------------------------------------------------*/

    protected AtomicInteger connectedClients = new AtomicInteger();

    @Override
    public int getConnectedClientsCount(){
        return connectedClients.get();
    }

    void process(Reactor reactor){
        try{
            SocketChannel accepted = channel.accept();
            if(accepted!=null){
                Client client = null;
                try{
                    client = new Client(reactor, accepted, this);
                    connectedClients.incrementAndGet();
                }catch(Throwable thr){
                    reactor.handleException(thr);
                    accepted.close();
                }

                if(Debugger.DEBUG)
                    Debugger.println(this+".accepted("+client+"){");
                listener.accepted(this, client);
                if(Debugger.DEBUG)
                    Debugger.println("}");
            }
        }catch(Throwable thr){
            reactor.handleException(thr);
        }
    }

    /*-------------------------------------------------[ Attachment ]---------------------------------------------------*/

    private Object attachment;

    public void attach(Object attachment){
        this.attachment = attachment;
    }

    public Object attachment(){
        return attachment;
    }

    /*-------------------------------------------------[ Closeable ]---------------------------------------------------*/

    @Override
    public boolean isOpen(){
        return channel.isOpen();
    }

    @Override
    public void close() throws IOException{
        try{
            channel.close();
        }finally{
            Reactors.serverCount.decrementAndGet();
            Reactors.unregister(this);
            if(connectedClients.get()==0)
                disableJMX();
        }
    }

    /*-------------------------------------------------[ Name ]---------------------------------------------------*/

    public String toString(){
        InetSocketAddress address = (InetSocketAddress)boundTo();
        String str = address==null ? "" : address.getHostString()+":"+address.getPort();
        String name = listener==null ? "Server" : listener.getClass().getSimpleName();
        return name+"["+str+"]";
    }

    /*-------------------------------------------------[ JMX ]---------------------------------------------------*/

    @Override
    public String getType(){
        return listener==null ? "unknown" : listener.getClass().getSimpleName();
    }

    private ObjectName objectName;
    public synchronized void enableJMX(){
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try{
            InetSocketAddress address = (InetSocketAddress)boundTo();
            String boundToStr = boundTo==null ? "none" : address.getHostString();
            int port = boundTo==null ? -1 : address.getPort();
            objectName = new ObjectName("jlibs.nio:type=Server,boundTo=\""+boundToStr+"\",port="+port);
            if(!server.isRegistered(objectName))
                server.registerMBean(this, objectName);
        }catch(Exception ex){
            throw new ImpossibleException(ex);
        }
    }

    protected synchronized void disableJMX(){
        try{
            if(objectName!=null){
                MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
                if(mbeanServer.isRegistered(objectName))
                    mbeanServer.unregisterMBean(objectName);
                objectName = null;
            }
        }catch(Exception ex){
            throw new ImpossibleException(ex);
        }
    }

    /*-------------------------------------------------[ Defaults ]---------------------------------------------------*/

    public static class Defaults{
        public static Integer BACKLOG;
        public static Boolean SO_REUSEADDR;
        public static Integer SO_RCVBUF;
        private Defaults(){}

        public static void apply(Server server) throws SocketException{
            ServerSocket socket = server.channel.socket();
            if(SO_REUSEADDR!=null)
                socket.setReuseAddress(SO_REUSEADDR);
            if(SO_RCVBUF!=null)
                socket.setReceiveBufferSize(SO_RCVBUF);
        }
    }
}
