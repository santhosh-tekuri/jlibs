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

import jlibs.core.util.Heap;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Santhosh Kumar T
 */
class TimeoutTracker extends Debuggable implements Iterator<NIOChannel>{
    private Heap<ClientChannel> heap = new Heap<ClientChannel>(1000){
        @Override
        protected void setIndex(ClientChannel client, int index){
            client.heapIndex = index;
        }

        @Override
        protected int compare(ClientChannel client1, ClientChannel client2){
            return client1.timeoutAt<client2.timeoutAt ? +1 : (client1.timeoutAt==client2.timeoutAt?0:-1);
        }
    };

    protected long time;
    public TimeoutTracker reset(){
        if(isTracking())
            time = System.currentTimeMillis();
        return this;
    }

    public boolean isTracking(){
        return heap.size()>0;
    }

    public void track(ClientChannel channel){
        untrack(channel);
        if(channel.getTimeout()>0){
            channel.timeoutAt = System.currentTimeMillis() + channel.getTimeout();
            heap.add(channel);
        }
    }

    public void untrack(ClientChannel channel){
        if(channel.heapIndex!=-1){
            ClientChannel removed = heap.removeAt(channel.heapIndex);
            assert removed==channel;
            assert channel.heapIndex==-1;
        }
        channel.timeoutAt = Long.MAX_VALUE;
    }

    @Override
    public boolean hasNext(){
        ClientChannel root = heap.root();
        return root!=null && root.timeoutAt<time;
    }

    @Override
    public NIOChannel next(){
        ClientChannel root = heap.root();
        if(root!=null && root.timeoutAt<time){
            assert root.heapIndex==0;
            heap.removeAt(0);
            if(DEBUG)
                println("channel@"+root.id+".timeout");
            return root;
        }else
            throw new NoSuchElementException();
    }

    @Override
    public void remove(){
        throw new UnsupportedOperationException();
    }
}
