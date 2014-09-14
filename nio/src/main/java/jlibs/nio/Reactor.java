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

import jlibs.core.lang.Waiter;
import jlibs.core.util.Heap;
import jlibs.nio.util.BufferAllocator;
import jlibs.nio.util.PooledBufferAllocator;
import jlibs.nio.util.UnpooledBufferAllocator;

import javax.management.ObjectName;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static jlibs.nio.Debugger.*;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Reactor{
    public final int id;
    public final Selector selector;
    public final ConnectionPool connectionPool = new ConnectionPool(this);
    public final BufferAllocator allocator;

    long lastAcceptID;
    long lastConnectID;
    private final ObjectName objName;

    Reactor(int id) throws IOException{
        this.id = id;
        selector = Selector.open();
        executionID = "R"+id;
        toString = "Reactor"+id;

        if(BufferAllocator.Defaults.POOL_BUFFERS)
            allocator = new PooledBufferAllocator(BufferAllocator.Defaults.USE_DIRECT_BUFFERS);
        else
            allocator = BufferAllocator.Defaults.USE_DIRECT_BUFFERS ? UnpooledBufferAllocator.DIRECT : UnpooledBufferAllocator.HEAP;

        objName = Management.register(new Management.ReactorMXBean(){
            @Override
            public int getServersCount(){
                return servers.size();
            }

            @Override
            public int getAccepted(){
                return accepted;
            }

            @Override
            public int getConnectionPending(){
                return connectionPending;
            }

            @Override
            public int getConnected(){
                return connected;
            }

            @Override
            public int getPooled(){
                return connectionPool.count();
            }

            @Override
            public Map<String, Integer> getPool(){
                Map<String, Integer> map[] = new Map[1];
                try{
                    invokeAndWait(()->{
                        map[0] = connectionPool.entries.values().stream()
                                .filter(t -> t.count>0)
                                .collect(Collectors.toMap(t -> t.key, t -> t.count));
                    });
                }catch(InterruptedException ex){
                    throw new RuntimeException(ex);
                }
                return map[0];
            }
        }, "jlibs.nio:type=Reactor,id="+id);
    }

    private final String toString;
    public String toString(){
        return toString;
    }

    /*-------------------------------------------------[ ExceptionHandler ]---------------------------------------------------*/

    private Consumer<Throwable> exceptionHandler;

    public void setExceptionHandler(Consumer<Throwable> exceptionHandler){
        this.exceptionHandler = exceptionHandler;
    }

    public void handleException(Throwable thr){
        if(thr==null)
            return;
        if(exceptionHandler==null){
            if(DEBUG)
                printStackTrace(thr);
            else{
                System.err.println("Unexpected error during "+executionID+":");
                thr.printStackTrace();
            }
        }else
            exceptionHandler.accept(thr);
    }

    /*-------------------------------------------------[ Servers ]---------------------------------------------------*/

    final List<TCPServer> servers = new ArrayList<>();

    public int getServersCount(){
        return servers.size();
    }

    void register(TCPServer server) throws IOException{
        if(server.selectable.keyFor(selector)==null){
            server.selectable.register(selector, OP_ACCEPT, server);
            servers.add(server);
            if(DEBUG)
                println(server+".register");
        }
    }

    void unregister(TCPServer server){
        if(DEBUG)
            println(server+".unregister");
        servers.remove(server);
        SelectionKey key = server.selectable.keyFor(selector);
        if(key!=null && key.isValid())
            key.cancel();
    }

    /*-------------------------------------------------[ Connections ]---------------------------------------------------*/

    int accepted;
    int connectionPending;
    int connected;

    public int getAccepted(){ return accepted; }
    public int getConnectionPending(){ return connectionPending; }
    public int getConnected(){ return connected; }

    /*-------------------------------------------------[ Tasks ]---------------------------------------------------*/

    private volatile Deque<Runnable> tasks = new ArrayDeque<>();

    public synchronized void invokeLater(Runnable task){
        tasks.push(task);
        selector.wakeup();
    }

    public void invokeAndWait(Runnable task) throws InterruptedException{
        if(Reactor.current()==this)
            task.run();
        else{
            task = task instanceof Waiter ? (Waiter)task : new Waiter(task);
            synchronized(task){
                invokeLater(task);
                task.wait();
            }
        }
    }

    /*-------------------------------------------------[ wakeupList ]---------------------------------------------------*/

    private NBStream wakeupHead;
    void wakeup(NBStream nbStream){
        if(nbStream.wakeupNext==null){
            if(IO)
                println("wakeup("+nbStream+")");
            nbStream.wakeupNext = wakeupHead ==null ? nbStream : wakeupHead;
            wakeupHead = nbStream;
        }
    }

    /*-------------------------------------------------[ Thread ]---------------------------------------------------*/

    void start(){
        new ReactorThread().start();
    }

    public static Reactor current(){
        Thread thread = Thread.currentThread();
        return thread instanceof ReactorThread ? ((ReactorThread)thread).reactor : null;
    }

    NBChannel activeChannel;
    String executionID;
    public String getExecutionID(){
        return activeChannel==null ? executionID : activeChannel.getExecutionID();
    }
    NBChannel getExecutionOwner(){
        return activeChannel==null ? null : activeChannel.workingFor;
    }

    private class ReactorThread extends Thread implements Thread.UncaughtExceptionHandler{
        public Reactor reactor = Reactor.this;
        ReactorThread(){
            super(Reactor.this.toString);
            setUncaughtExceptionHandler(this);
        }

        public void run(){
            final Selector selector = reactor.selector;
            final TimeoutTracker timeoutTracker = reactor.timeoutTracker;
            Deque<Runnable> tempTasks = new ArrayDeque<>();
            NBChannel nbChannel;
            NBStream nbStream;

            while(true){
                while(wakeupHead!=null){
                    nbStream = wakeupHead;
                    wakeupHead = null;
                    while(nbStream!=null){
                        if(nbStream.heapIndex!=-1)
                            timeoutTracker.stopTimer(nbStream);
                        activeChannel = nbStream;
                        if(IO)
                            enter(true, nbStream+".wakeupNow");
                        try{
                            nbStream.wakeupNow();
                        }catch(Throwable thr){
                            handleException(thr);
                        }
                        if(IO)
                            exit();
                        NBStream next = nbStream.wakeupNext==nbStream ? null : nbStream.wakeupNext;
                        nbStream.wakeupNext = null;
                        nbStream = next;
                    }
                }

                // run tasks
                while(!tasks.isEmpty()){
                    synchronized(this){
                        Deque<Runnable> temp = tasks;
                        tasks = tempTasks;
                        tempTasks = temp;
                    }
                    while(!tempTasks.isEmpty()){
                        activeChannel = null;
                        if(DEBUG)
                            enter(true, "runTask");
                        try{
                            tempTasks.pop().run();
                        }catch(Throwable thr){
                            handleException(thr);
                        }
                        if(DEBUG)
                            exit();
                    }
                }

                if(shutdown && servers.size()==0 && connected==0 && connectionPending==0 && accepted==0){
                    try{
                        selector.close();
                        Management.unregister(objName);
                    }catch(Throwable thr){
                        handleException(thr);
                    }
                    return;
                }

                boolean tracking = timeoutTracker.isTracking();
                long selectTimeout = tracking ? timeoutTracker.waitTime() : 0L;

                int selected = 0;
                try{
                    if(IO)
                        enter(true, "select("+selectTimeout+")");
                    selected = selector.select(selectTimeout);
                }catch(IOException ex){
                    handleException(ex);
                }
                if(tracking)
                    timeoutTracker.time = System.currentTimeMillis();
                if(selected>0){
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    for(SelectionKey key: selectedKeys){
                        if(key.isValid()){
                            nbChannel = (NBChannel)key.attachment();
                            if(nbChannel.heapIndex!=-1)
                                timeoutTracker.stopTimer(nbChannel);
                            activeChannel = nbChannel;
                            if(IO)
                                enter(nbChannel+".process");
                            try{
                                nbChannel.process(false);
                            }catch(Throwable thr){
                                handleException(thr);
                            }
                            if(IO)
                                exit();
                        }
                    }
                    selectedKeys.clear();
                }
                if(IO)
                    exit();
                if(tracking){
                    while((nbChannel=timeoutTracker.next())!=null){
                        activeChannel = nbChannel;
                        if(nbChannel instanceof Connection && ((Connection)nbChannel).poolNext!=null){
                            if(IO)
                                println(nbChannel+".poolTimeout");
                            connectionPool.remove((Connection)nbChannel);
                            nbChannel.close();
                        }else{
                            if(IO)
                                enter(true, nbChannel+".processTimeout");
                            try{
                                nbChannel.process(true);
                            }catch(Throwable thr){
                                handleException(thr);
                            }
                            if(IO)
                                exit();
                        }
                    }
                }
            }
        }

        @Override
        public void uncaughtException(Thread thread, Throwable throwable){
            handleException(throwable);
            assert !thread.isAlive();
            new ReactorThread().start();
        }
    }

    /*-------------------------------------------------[ Timeout ]---------------------------------------------------*/

    private TimeoutTracker timeoutTracker = new TimeoutTracker();
    void startTimer(NBChannel channel, long timeout){
        if(timeout>0)
            timeoutTracker.startTimer(channel, timeout);
    }

    void stopTimer(NBChannel channel){
        timeoutTracker.stopTimer(channel);
    }

    private final class TimeoutTracker{
        private final Heap<NBChannel> heap = new Heap<NBChannel>(1000){
            @Override
            protected void setIndex(NBChannel channel, int index){
                channel.heapIndex = index;
            }

            @Override
            protected int compare(NBChannel channel1, NBChannel channel2){
                return channel1.timeoutAt<channel2.timeoutAt ? -1 : (channel1.timeoutAt==channel2.timeoutAt?0:+1);
            }
        };

        public boolean isTracking(){
            return heap.size()>0;
        }

        public void startTimer(NBChannel channel, long timeout){
            if(channel.heapIndex!=-1)
                stopTimer(channel);
            if(timeout>0){
                channel.timeoutAt = System.currentTimeMillis() + timeout;
                heap.add(channel);
            }
        }

        public void stopTimer(NBChannel channel){
            assert channel.heapIndex!=-1;
            NBChannel removed = heap.removeAt(channel.heapIndex);
            assert removed==channel;
            assert channel.heapIndex==-1;
            channel.timeoutAt = Long.MAX_VALUE;
        }

        long time;
        public NBChannel next(){
            NBChannel root = heap.root();
            if(root!=null && root.timeoutAt<time){
                assert root.heapIndex==0;
                heap.removeAt(0);
                return root;
            }else
                return null;
        }

        public long waitTime(){
            return heap.size()==0 ? 0L : Math.max(1000L, heap.root().timeoutAt-System.currentTimeMillis());
        }
    }

    /*-------------------------------------------------[ Shutdown ]---------------------------------------------------*/

    private boolean shutdown;
    void shutdown(boolean force){
        if(!shutdown){
            shutdown = true;
            if(Debugger.IO)
                Debugger.println(Reactor.this+".shutdownInitialized: servers="+getServersCount()+
                        " connected="+connected+
                        " connectionPending="+connectionPending+
                        " accepted="+accepted);
            while(servers.size()>0)
                unregister(servers.get(0));
            if(force){
                for(SelectionKey key: selector.keys()){
                    try{
                        key.channel().close();
                    }catch(IOException ex){
                        handleException(ex);
                    }
                }
                connected = connectionPending = accepted = 0;
            }
        }
    }

    /*-------------------------------------------------[ Misc ]---------------------------------------------------*/

    private StringBuilder builder = new StringBuilder(500);
    public static StringBuilder stringBuilder(){
        Reactor reactor = Reactor.current();
        if(reactor==null)
            return new StringBuilder(100);
        else{
            StringBuilder builder = reactor.builder;
            reactor.builder = null;
            builder.setLength(0);
            return builder;
        }
    }

    public static String free(StringBuilder builder){
        Reactor reactor = Reactor.current();
        if(reactor!=null)
            reactor.builder = builder;
        return builder.toString();
    }
}
