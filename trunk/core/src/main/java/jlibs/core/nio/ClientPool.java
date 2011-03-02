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

import java.net.SocketAddress;

/**
 * @author Santhosh Kumar T
 */
public class ClientPool extends AttachmentSupport{
    private final SocketAddress remote;

    public ClientPool(SocketAddress remote){
        this.remote = remote;
    }

    public SocketAddress remoteAddress(){
        return remote;
    }

    public String toString(){
        return "ClientPool["+remote+"]";
    }

    // should be called in client's thread
    public synchronized void add(ClientChannel client, long timeout){
        if(client.pool!=null)
            throw new IllegalArgumentException("client is already pooled");
        if(client.interests()!=0)
            throw new IllegalArgumentException("client.interests() should be zero");
        if(client.sslEnabled() && client.plainTransport().interests()!=0){
            client.futurePool = this;
            client.futurePoolTimeout = timeout;
            addToList(client);
        }else
            _add(client, timeout);
    }

    protected synchronized void _add(ClientChannel client, long timeout){
        client.pool = this;
        long oldTimeout = client.getTimeout();
        client.setTimeout(timeout);
        client.nioSelector.timeoutTracker.track(client);
        client.setTimeout(oldTimeout);

        if(client.futurePool!=null)
            client.futurePool = null;
        else
            addToList(client);
    }

    public synchronized ClientChannel remove(){
        final ClientChannel client = size==0 ? null : head.next;
        if(client!=null){
            removeFromList(client);
            client.nioSelector.invokeLater(new Runnable(){
                @Override
                public void run(){
                    remove(client);
                }
            });
        }
        return client;
    }

    // should be called in client's thread
    public synchronized boolean remove(ClientChannel client){
        if(client.pool!=this)
            throw new IllegalArgumentException("This client doesn't belong to this pool");
        client.pool = client.futurePool = null;
        return removeFromList(client);
    }

    /*-------------------------------------------------[ LinkedList ]---------------------------------------------------*/

    private ClientChannel head = new ClientChannel();
    private int size;

    public synchronized int size(){
        return size;
    }

    private void addToList(ClientChannel channel){
        assert channel.prev==null && channel.next==null;

        channel.prev = head.prev;
        channel.next = head;
        channel.prev.next = channel;
        channel.next.prev = channel;
        size++;
    }

    private boolean removeFromList(ClientChannel channel){
        if(channel.prev!=null){ // present in list
            channel.prev.next = channel.next;
            channel.next.prev = channel.prev;
            channel.prev = channel.next = null;
            size--;
            return true;
        }else
            return false;
    }
}
