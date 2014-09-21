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

import static jlibs.nio.Debugger.DEBUG;
import static jlibs.nio.Debugger.println;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ConnectionPool{
    private final Reactor reactor;
    public long timeout = 60*1000;

    Map<String, Entry> entries = new HashMap<>();
    private int count;
    ConnectionPool(Reactor reactor){
        this.reactor = reactor;
    }

    public int count(){
        return count;
    }

    public void add(String key, Connection connection){
        add(key, connection, timeout);
    }

    public void add(String key, Connection connection, long timeout){
        // todo: transport is null for PipedConnection
        if(connection.transport.peekInInterested || connection.transport.peekOutInterested)
            throw new IllegalArgumentException("connection.interestOps!=0");
        if(timeout<=0){
            timeout = this.timeout;
            if(timeout<=0)
                timeout = 60*1000;
        }

        Entry entry = entries.get(key);
        if(entry==null)
            entries.put(key, entry=new Entry(key));
        entry.add(connection, timeout);
    }

    public Connection remove(String key){
        Entry entry = entries.get(key);
        return entry==null ? null : entry.remove();
    }

    public void remove(Connection connection){
        if(connection.poolPrev!=null && connection.poolNext!=null)
            entries.get(connection.poolKey).remove(connection);
    }

    public class Entry{
        public final String key;
        int count;

        public Entry(String key){
            this.key = key;
        }

        private Connection head;

        public void add(Connection con, long timeout){
            if(con.poolPrev==null && con.poolNext==null){
                if(DEBUG)
                    println("connectionPool.add("+con+", "+timeout+")");
                con.poolKey = key;
//                con.addingToPool();
                if(head==null){
                    con.poolPrev = con;
                    con.poolNext = con;
                }else{
                    Connection tail = head.poolPrev;
                    con.poolNext = head;
                    head.poolPrev = con;
                    con.poolPrev = tail;
                    tail.poolNext = con;
                }
                head = con;
                reactor.startTimer(con, timeout);
                ++count;
                ++ConnectionPool.this.count;
                con.workingFor = con;
                con.executionID = null;
            }
        }

        public Connection remove(){
            if(head==null)
                return null;

            Connection connection = head;
            remove(head);
            return connection;
        }

        void remove(Connection con){
            if(DEBUG)
                println("connectionPool.remove("+con+")");
            if(con.heapIndex!=-1)
                reactor.stopTimer(con);
//            con.initWorkingFor();
            if(con==head){
                if(head.poolNext==head && head.poolPrev==head){
                    head = null;
                }else{
                    Connection tail = head.poolPrev;
                    head = head.poolNext;
                    head.poolPrev = tail;
                    tail.poolNext = head;
                }
            }else{
                Connection before = con.poolPrev;
                Connection after = con.poolNext;
                before.poolNext = after;
                after.poolPrev = before;
            }

            con.poolPrev = null;
            con.poolNext = null;
            --count;
            --ConnectionPool.this.count;
            con.taskCompleted();
            con.workingFor = reactor.getExecutionOwner();
            if(con.workingFor==null)
                con.workingFor = con;
            con.makeActive();
        }
    }
}
