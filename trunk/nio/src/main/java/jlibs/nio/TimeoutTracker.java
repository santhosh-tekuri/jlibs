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

import jlibs.core.util.Heap;

/**
 * @author Santhosh Kumar Tekuri
 */
final class TimeoutTracker{
    private final Heap<Client> heap = new Heap<Client>(1000){
        @Override
        protected void setIndex(Client client, int index){
            client.heapIndex = index;
        }

        @Override
        protected int compare(Client client1, Client client2){
            return client1.timeoutAt<client2.timeoutAt ? -1 : (client1.timeoutAt==client2.timeoutAt?0:+1);
        }
    };

    public boolean isTracking(){
        return heap.size()>0;
    }

    public void track(Client client){
        if(client.heapIndex!=-1)
            untrack(client);
        if(client.getTimeout()>0){
            client.timeoutAt = System.currentTimeMillis() + client.getTimeout();
            heap.add(client);
        }
    }

    public void untrack(Client client){
        assert client.heapIndex!=-1;
        Client removed = heap.removeAt(client.heapIndex);
        assert removed==client;
        assert client.heapIndex==-1;
        client.timeoutAt = Long.MAX_VALUE;
    }

    long time;
    public Client next(){
        Client root = heap.root();
        if(root!=null && root.timeoutAt<time){
            assert root.heapIndex==0;
            heap.removeAt(0);
            return root;
        }else
            return null;
    }
}