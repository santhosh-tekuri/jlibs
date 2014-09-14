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

import javax.management.ObjectName;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static jlibs.nio.Debugger.DEBUG;
import static jlibs.nio.Debugger.println;

/**
 * @author Santhosh Kumar Tekuri
 */
public class TCPServer extends NBChannel<ServerSocketChannel>{
    private static final AtomicInteger COUNTER = new AtomicInteger();

    public final long id = COUNTER.incrementAndGet();
    public TCPServer(Listener listener) throws IOException{
        super(ServerSocketChannel.open());

        for(Reactor reactor: Reactors.get()){
            reactor.invokeLater(() -> {
                try{
                    reactor.register(this);
                }catch(IOException ex){
                    reactor.handleException(ex);
                }
            });
        }
        this.listener = listener;
        uniqueID = "S"+id;
    }

    private ObjectName objName;
    public TCPServer bind(SocketAddress local) throws IOException{
        selectable.bind(local, BACKLOG);
        String boundToStr = ((InetSocketAddress)local).getHostString();
        int port = ((InetSocketAddress)local).getPort();
        objName = Management.register(new Management.ServerMXBean(){
            @Override
            public String getType(){
                String name = listener.getClass().getSimpleName();
                if(name.length()==0)
                    name = listener.toString();
                return name;
            }

            @Override
            public int getAccepted(){
                return accepted.get();
            }

            @Override
            public boolean isOpen(){
                return TCPServer.this.isOpen();
            }

            @Override
            public void close() throws IOException{
                TCPServer.this.close();
            }
        }, "jlibs.nio:type=Server,boundTo=\""+boundToStr+"\",port="+port);
        return this;
    }

    public SocketAddress boundTo(){
        return selectable.socket().getLocalSocketAddress();
    }

    private Listener listener;
    public static interface Listener{
        public void accept(TCPConnection con);
    }

    final AtomicInteger accepted = new AtomicInteger();
    public int getAccepted(){
        return accepted.get();
    }

    @Override
    protected void process(boolean timeout){
        try{
            SocketChannel socket = selectable.accept();
            if(socket==null)
                return;
            TCPConnection connection;
            try{
                connection = new TCPConnection(this, socket);
                connection.workingFor = connection;
                if(DEBUG)
                    println("accepted = "+connection);
            }catch(IOException ex){
                socket.close();
                throw ex;
            }
            listener.accept(connection);
        }catch(IOException ex){
            Reactor.current().handleException(ex);
        }
    }

    @Override
    public void close(){
        List<Reactor> reactors = Reactors.get();
        CountDownLatch latch = new CountDownLatch(reactors.size());
        for(Reactor reactor: reactors){
            reactor.invokeLater(() -> {
                try{
                    reactor.unregister(this);
                }finally{
                    latch.countDown();
                }
            });
        }
        try{
            latch.await();
        }catch(InterruptedException ex){
            // ignore
        }
        super.close();
        Management.unregister(objName);
    }

    @Override
    public String getExecutionID(){
        return Reactor.current().executionID+'/'+uniqueID;
    }

    public String toString(){
        InetSocketAddress address = (InetSocketAddress)boundTo();
        String str = address==null ? "" : address.getHostString()+":"+address.getPort();
        String name = listener.getClass().getSimpleName();
        if(name.length()==0)
            name = listener.toString();
        return name+"["+str+"]";
    }

    /*-------------------------------------------------[ Options ]---------------------------------------------------*/

    public static int BACKLOG = 0;
}
