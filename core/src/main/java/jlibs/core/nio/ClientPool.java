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

import jlibs.core.util.NonNullIterator;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class ClientPool extends AttachmentSupport implements Iterable<ClientPool.Entry>{
    private final NIOSelector selector;
    private final Map<SocketAddress, Entry> map = new HashMap<SocketAddress, Entry>();

    protected ClientPool(NIOSelector selector){
        this.selector = selector;
    }

    public String toString(){
        return "ClientPool@"+selector.id;
    }

    protected void add(ClientChannel client, SocketAddress address, long timeout){
        if(timeout<=0)
            throw new IllegalArgumentException("invalid timeout: "+timeout);
        if(client.poolFlag>=0)
            throw new IllegalArgumentException(client+" is already pooled");
        if(!client.realChannel().isConnected())
            throw new IllegalArgumentException(client+" is not connected yet");
        if(client.interests()!=0)
            throw new IllegalArgumentException(client+".interests() should be zero");

        client.poolFlag = timeout;
        if(client.key.interestOps()==0)
            track(client);
        addToList(client, address);
    }

    protected void track(ClientChannel client){
        long oldTimeout = client.getTimeout();
        client.setTimeout(client.poolFlag);
        client.nioSelector.timeoutTracker.track(client);
        client.setTimeout(oldTimeout);
        client.poolFlag = 0;
    }

    public Entry entry(SocketAddress remote){
        if(size==0)
            return null;
        return map.get(remote);
    }

    public ClientChannel remove(SocketAddress remote){
        if(size==0)
            return null;
        Entry list = map.get(remote);
        if(list==null)
            return null;
        return list.remove();
    }

    protected boolean remove(ClientChannel client, SocketAddress address){
        if(client.poolFlag<0)
            return false;
        Entry list = map.get(address);
        list.remove(client);
        return true;
    }

    /*-------------------------------------------------[ LinkedList ]---------------------------------------------------*/

    public class Entry{
        private SocketAddress address;
        private ClientChannel head = new ClientChannel();
        private int size;

        protected Entry(SocketAddress address){
            this.address = address;
        }

        public SocketAddress address(){
            return address;
        }

        public int size(){
            return size;
        }

        protected void add(ClientChannel channel){
            assert channel.prev==null && channel.next==null;

            channel.prev = head.prev;
            channel.next = head;
            channel.prev.next = channel;
            channel.next.prev = channel;
            size++;
            ClientPool.this.size++;
        }

        protected void remove(ClientChannel channel){
            if(channel.poolFlag==0)
                channel.nioSelector.timeoutTracker.untrack(channel);
            channel.poolFlag = -1;

            channel.prev.next = channel.next;
            channel.next.prev = channel.prev;
            channel.prev = channel.next = null;
            size--;
            ClientPool.this.size--;

            if(size()==0)
                map.remove(address);
        }

        public ClientChannel remove(){
            if(size==0)
                return null;
            ClientChannel client = head.next;
            remove(client);
            return client;
        }

        @Override
        public String toString(){
            return address+"="+size;
        }
    }

    @Override
    public Iterator<Entry> iterator(){
        final Iterator<Entry> entries = map.values().iterator();
        return new NonNullIterator<Entry>(){
            @Override
            protected Entry findNext(){
                return entries.hasNext() ? entries.next() : null;
            }
        };
    }

    private int size;
    public int size(){
        return size;
    }

    public int size(SocketAddress remote){
        Entry list = map.get(remote);
        return list==null ? 0 : list.size();
    }

    private void addToList(ClientChannel channel, SocketAddress address){
        Entry list = map.get(address);
        if(list==null)
            map.put(address, list=new Entry(address));
        list.add(channel);
    }
}
