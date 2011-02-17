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
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

/**
 * @author Santhosh Kumar T
 */
public class PlainTransport extends Debuggable implements Transport{
    private final ClientChannel client;

    public PlainTransport(ClientChannel client){
        this.client = client;
    }

    @Override
    public long id(){
        return client.id;
    }

    @Override
    public void id(long newID){
        client.id = newID;
    }

    @Override
    public ClientChannel client(){
        return client;
    }

    @Override
    public int interests(){
        return client.key.isValid() ? client.key.interestOps() : 0;
    }

    @Override
    public void addInterest(int operation) throws IOException{
        client.key.interestOps(interests() | operation);
        client.nioSelector.timeoutIterator.add(client);
        if(DEBUG){
            switch(operation){
                case SelectionKey.OP_CONNECT:
                    println("channel@"+id()+".connectWait");
                    return;
                case SelectionKey.OP_READ:
                    println("channel@"+id()+".readWait");
                    return;
                case SelectionKey.OP_WRITE:
                    println("channel@"+id()+".writeWait");
            }
        }
    }

    @Override
    public void removeInterest(int operation){
        client.key.interestOps(interests()&~operation);
        if(interests()==0)
            client.nioSelector.timeoutIterator.remove(client);
    }

    @Override
    public int ready(){
        return client.key.isValid() ? client.key.readyOps() : 0;
    }

    @Override
    public boolean process(){
        int ops = client.key.interestOps();
        if(client.isConnectable())
            ops &= ~SelectionKey.OP_CONNECT;
        if(client.isReadable())
            ops &= ~SelectionKey.OP_READ;
        if(client.isWritable())
            ops &= ~SelectionKey.OP_WRITE;
        client.key.interestOps(ops);
        return true;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException{
        int read = client.realChannel().read(dst);
        if(DEBUG)
            println("channel@"+id()+".read: "+read);
        return read;
    }

    @Override
    public int write(ByteBuffer src) throws IOException{
        int wrote = client.realChannel().write(src);
        if(DEBUG)
            println("channel@"+id()+".write: "+wrote);
        return wrote;
    }

    @Override
    public void shutdownOutput() throws IOException{
        if(DEBUG)
            println("channel@"+id()+".shutdownOutput");
        client.realChannel().socket().shutdownOutput();
    }

    @Override
    public boolean isOutputShutdown(){
        return client.realChannel().socket().isOutputShutdown();
    }

    @Override
    public boolean isOpen(){
        return client.realChannel().isOpen();
    }

    @Override
    public void close() throws IOException{
        if(DEBUG)
            println("channel@"+id()+".close");
        boolean wasOpen = isOpen();
        boolean wasConnected = client.isConnected();
        client.realChannel().close();
        if(wasOpen){
            if(wasConnected)
                client.nioSelector.connectedClients--;
            else
                client.nioSelector.connectionPendingClients--;
            client.nioSelector.timeoutIterator.remove(client);
        }
    }
}
