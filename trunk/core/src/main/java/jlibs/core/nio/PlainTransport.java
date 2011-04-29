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
public class PlainTransport extends Transport{
    private final ClientChannel client;

    public PlainTransport(ClientChannel client){
        this.client = client;
    }

    @Override
    public long id(){
        return client.id;
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
        client.nioSelector.timeoutTracker.track(client);
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
        int newInterests = interests()&~operation;
        client.key.interestOps(newInterests);
        if(newInterests==0)
            client.nioSelector.timeoutTracker.untrack(client);
    }

    @Override
    public int ready(){
        return client.key.isValid() ? client.key.readyOps() : 0;
    }

    @Override
    public boolean updateReadyInterests(){
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean process(){
        int ready = client.key.readyOps();
        int interests = client.key.interestOps();
        if((ready & SelectionKey.OP_CONNECT)!=0)
            interests &= ~SelectionKey.OP_CONNECT;
        if((ready & SelectionKey.OP_READ)!=0)
            interests &= ~SelectionKey.OP_READ;
        if((ready & SelectionKey.OP_WRITE)!=0)
            interests &= ~SelectionKey.OP_WRITE;
        client.key.interestOps(interests);
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
        if(isOpen()){
            boolean wasConnected = client.isConnected();
            client.realChannel().close();
            if(wasConnected)
                client.nioSelector.connectedClients--;
            else
                client.nioSelector.connectionPendingClients--;
            client.nioSelector.timeoutTracker.untrack(client);
            if(client.poolFlag>=0)
                client.removeFromPool();
        }
    }
}
