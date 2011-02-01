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

import jlibs.core.util.AbstractIterator;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Santhosh Kumar T
 */
public class NIOSelector extends Debuggable implements Iterable<NIOChannel>{
    private static AtomicLong ID_GENERATOR = new AtomicLong();

    protected long id = ID_GENERATOR.incrementAndGet();
    protected final Selector selector;
    protected final long timeout;
    private volatile boolean initiateShutdown;
    private boolean shutdownInProgress;

    public NIOSelector(long timeout) throws IOException{
        selector = Selector.open();
        this.timeout = timeout;
    }

    protected long lastClientID;
    public ClientChannel newClient() throws IOException{
        validate();
        return new ClientChannel(this, SocketChannel.open());
    }

    @Override
    public String toString(){
        return "NIOSelector@"+id;
    }

    /*-------------------------------------------------[ Shutdown ]---------------------------------------------------*/

    public void shutdown(){
        if(!shutdownInProgress){
            initiateShutdown = true;
            if(DEBUG)
                println(this+".shutdownRequested");
        }
    }

    public boolean isShutdownPending(){
        return (initiateShutdown || shutdownInProgress) && (serverCount()!=0 || connectedClients!=0 || connectionPendingClients!=0);
    }

    public boolean isShutdown(){
        return shutdownInProgress && serverCount()==0 && connectedClients==0 && connectionPendingClients==0;
    }

    protected void validate() throws IOException{
        if(isShutdownPending())
            throw new IOException("shutdown in progress");
        if(isShutdownPending())
            throw new IOException("already shutdown");
    }

    /*-------------------------------------------------[ Statistics ]---------------------------------------------------*/

    protected List<ServerChannel> servers = new ArrayList<ServerChannel>();
    protected long connectionPendingClients;
    protected long connectedClients;

    public long serverCount(){
        return servers.size();
    }

    public long connectionPendingClientsCount(){
        return connectionPendingClients;
    }

    public long connectedClientsCount(){
        return connectedClients;
    }

    /*-------------------------------------------------[ Iterable ]---------------------------------------------------*/

    @Override
    public Iterator<NIOChannel> iterator(){
        return iterator;
    }

    private Iterator<NIOChannel> iterator = new AbstractIterator<NIOChannel>() {
        private Iterator<NIOChannel> delegate = Collections.<NIOChannel>emptyList().iterator();
        @Override
        protected NIOChannel computeNext(){
            try{
                while(!isShutdown() && !delegate.hasNext())
                    delegate = select();
                if(delegate.hasNext())
                    return delegate.next();
                else{
                    if(DEBUG)
                        println(NIOSelector.this+".shutdown");
                    return null;
                }
            }catch(IOException ex){
                throw new RuntimeException(ex);
            }
        }
    };

    /*-------------------------------------------------[ Selection ]---------------------------------------------------*/

    protected List<NIOChannel> ready = new LinkedList<NIOChannel>();
    public Iterator<NIOChannel> select() throws IOException{
        if(ready.size()>0)
            return readyIterator.reset();
        if(initiateShutdown){
            shutdownInProgress = true;
            initiateShutdown = false;
            if(DEBUG)
                println(this+".shutdownInitialized: servers="+serverCount()+" connectedClients="+connectedClientsCount()+" connectionPendingClients="+connectionPendingClientsCount());
            while(servers.size()>0)
                servers.get(0).unregister(this);
        }

        if(selector.select(timeout)>0)
            return selectedIterator.reset();
        else
            return timeoutIterator.reset();
    }

    private ReadyIterator readyIterator = new ReadyIterator();
    private class ReadyIterator implements Iterator<NIOChannel>{
        private int count;
        public ReadyIterator reset(){
            count = ready.size();
            return this;
        }

        @Override
        public boolean hasNext(){
            return count>0;
        }

        @Override
        public NIOChannel next(){
            if(count==0)
                throw new NoSuchElementException();
            NIOChannel channel = ready.remove(0);
            count--;
            return channel;
        }

        @Override
        public void remove(){
            throw new UnsupportedOperationException();
        }
    }

    private SelectedIterator selectedIterator = new SelectedIterator();
    private class SelectedIterator extends AbstractIterator<NIOChannel>{
        private Iterator<SelectionKey> keys;

        @Override
        public SelectedIterator reset(){
            super.reset();
            keys = selector.selectedKeys().iterator();
            timeoutIterator.reset();
            return this;
        }

        @Override
        protected NIOChannel computeNext(){
            while(keys.hasNext()){
                SelectionKey key = keys.next();
                keys.remove();
                NIOChannel channel = (NIOChannel)key.attachment();
                if(key.isValid()){
                    if(channel instanceof ClientChannel){
                        ClientChannel client = (ClientChannel)channel;
                        if(client.isTimeout())
                            client.interestTime = Long.MAX_VALUE;
                        timeoutIterator.remove(client);
                    }
                    if(channel.process())
                        return channel;
                }
            }
            return timeoutIterator.hasNext() ? timeoutIterator.next() : null;
        }
    }

    protected TimeoutIterator timeoutIterator = new TimeoutIterator();
    class TimeoutIterator implements Iterator<NIOChannel>{
        private ClientChannel head = new ClientChannel();

        private boolean checkLinks(){
            ClientChannel channel = head;
            do{
                assert channel.next.prev==channel;
                assert channel.prev.next==channel;
                channel = channel.next;
            }while(channel!=head);
            return true;
        }

        protected void add(ClientChannel channel){
            if(channel.prev!=null)
                remove(channel);
            channel.prev = head.prev;
            channel.next = head;
            channel.prev.next = channel;
            channel.next.prev = channel;
            channel.interestTime = System.currentTimeMillis();
            assert checkLinks();
        }

        protected void remove(ClientChannel channel){
            assert (channel.prev!=null)==(channel.next!=null);
            if(channel.prev!=null){
                if(current==channel)
                    current = current.prev;

                channel.prev.next = channel.next;
                channel.next.prev = channel.prev;
                channel.prev = channel.next = null;
                assert checkLinks();
            }
        }

        protected long time;
        private ClientChannel current;
        public TimeoutIterator reset(){
            time = System.currentTimeMillis()-timeout;
            current = head;
            return this;
        }

        @Override
        public boolean hasNext(){
            return current.next!=head && current.next.interestTime<time;
        }

        @Override
        public NIOChannel next(){
            if(hasNext()){
                current = current.next;
                if(DEBUG)
                    println("channel@"+current.id+".timeout");
                return current;
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove(){
            throw new UnsupportedOperationException();
        }
    }
}
