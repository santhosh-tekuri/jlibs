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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ClientPool{
    private final Reactor reactor;
    public long timeout = 60*1000;

    Map<String, Entry> entries = new HashMap<>();
    private int count;
    ClientPool(Reactor reactor){
        this.reactor = reactor;
    }

    public int count(){
        return count;
    }

    public void add(String key, Client client){
        add(key, client, timeout);
    }

    public void add(String key, Client client, long timeout){
        assert client.in().getInputListener()==null;
        assert client.out().getOutputListener()==null;
        if(timeout<=0){
            timeout = this.timeout;
            if(timeout<=0)
                timeout = 60*1000;
        }

        Entry entry = entries.get(key);
        if(entry==null)
            entries.put(key, entry=new Entry(key));
        entry.add(client, timeout);
    }

    public Client remove(String key){
        Entry entry = entries.get(key);
        return entry==null ? null : entry.remove();
    }

    public void remove(Client client){
        if(client.poolPrev!=null && client.poolNext!=null)
            entries.get(client.poolKey).remove(client);
    }

    public class Entry{
        public final String key;
        int count;

        public Entry(String key){
            this.key = key;
        }

        private Client head;

        public void add(Client client, long timeout){
            if(client.poolPrev==null && client.poolNext==null){
                if(Debugger.DEBUG)
                    Debugger.println(reactor+".clientPool.add("+client+", "+timeout+"){");
                client.poolKey = key;
                if(client.socketIO.interestOps()==0){
                    client.addingToPool();
                    if(head==null){
                        client.poolPrev = client;
                        client.poolNext = client;
                    }else{
                        Client tail = head.poolPrev;
                        client.poolNext = head;
                        head.poolPrev = client;
                        client.poolPrev = tail;
                        tail.poolNext = client;
                    }
                    head = client;
                    client.poolTimeout = client.getTimeout();
                    client.setTimeout(timeout);
                    reactor.timeoutTracker.track(client);
                    ++count;
                    ++ClientPool.this.count;
                }else{
                    if(Debugger.DEBUG)
                        Debugger.println("non-idle client");
                    client.poolTimeout = timeout;
                }
                if(Debugger.DEBUG)
                    Debugger.println("}");
            }
        }

        public Client remove(){
            if(head==null)
                return null;

            Client client;
            client = head;
            remove(head);
            client.setTimeout(client.poolTimeout);
            client.poolTimeout = 0;
            reactor.timeoutTracker.untrack(client);
            if(Debugger.DEBUG)
                Debugger.println(reactor+".clientPool.remove("+key+") = "+client);
            client.initWorkingFor();
            return client;
        }

        void remove(Client client){
            if(client==head){
                if(head.poolNext==head && head.poolPrev==head){
                    head = null;
                }else{
                    Client tail = head.poolPrev;
                    head = head.poolNext;
                    head.poolPrev = tail;
                    tail.poolNext = head;
                }
            }else{
                Client before = client.poolPrev;
                Client after = client.poolNext;
                before.poolNext = after;
                after.poolPrev = before;
            }

            client.poolPrev = null;
            client.poolNext = null;
            --count;
            --ClientPool.this.count;
        }
    }
}
