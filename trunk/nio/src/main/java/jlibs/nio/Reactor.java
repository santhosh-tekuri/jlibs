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

import jlibs.core.lang.ImpossibleException;
import jlibs.core.lang.Waiter;
import jlibs.nio.channels.impl.SelectableChannel;
import jlibs.nio.channels.impl.SelectableInputChannel;
import jlibs.nio.channels.impl.SelectableOutputChannel;

import javax.management.MBeanServer;
import javax.management.MXBean;
import javax.management.ObjectName;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.nio.channels.SelectionKey.*;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Reactor{
    static final AtomicInteger ID_GENERATOR = new AtomicInteger(-1);
    long lastAcceptedClientID;
    long lastConnectableClientID;

    public final int id;
    Selector selector;

    public Reactor() throws IOException{
        this.id = ID_GENERATOR.incrementAndGet();
        executionID = "R"+id;
        selector = Selector.open();
        toString = "Reactor"+id;
        Reactors.reactors.add(this);
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
        if(Debugger.DEBUG){
            Debugger.println(this+".handleException("+thr+"){");
            System.setErr(new PrintStream(System.out){
                @Override
                public void println(String line){
                    Debugger.println(line, System.out);
                }
            });
            Debugger.println("}");
        }
        try{
            if(exceptionHandler!=null)
                exceptionHandler.accept(thr);
            else{
                System.err.println("Unexpected error during "+getExecutionID()+":");
                thr.printStackTrace();
            }
        }catch(Throwable thr1){
            thr1.printStackTrace();
        }
        if(Debugger.DEBUG){
            Debugger.println("}");
            System.setErr(System.out);
        }
    }

    /*-------------------------------------------------[ Tasks ]---------------------------------------------------*/

//    private volatile List<Runnable> tasks = new LinkedList<>();
    private volatile Deque<Runnable> tasks = new ArrayDeque<>();
    private volatile Deque<Runnable> tempTasks = new ArrayDeque<>();

    public synchronized void invokeLater(Runnable task){
        tasks.push(task);
        selector.wakeup();
    }

    public void invokeAndWait(Runnable task) throws InterruptedException{
        if(Reactor.current()==this)
            task.run();
        else{
            task = new Waiter(task);
            synchronized(task){
                invokeLater(task);
                task.wait();
            }
        }
    }

    void runTasks(){
        while(!tasks.isEmpty()){
            synchronized(this){
                Deque<Runnable> temp = tasks;
                tasks = tempTasks;
                tempTasks = temp;
            }
            while(!tempTasks.isEmpty()){
                try{
                    activeClient = null;
                    tempTasks.pop().run();
                }catch(Throwable thr){
                    handleException(thr);
                }
            }
        }
    }

    /*-------------------------------------------------[ Servers ]---------------------------------------------------*/

    final List<Server> servers = new ArrayList<Server>();

    public int serversCount(){
        return servers.size();
    }

    public boolean isRegistered(Server server){
        return servers.contains(server);
    }

    public void register(final Server server) throws IOException{
        validate();
        if(server.channel.keyFor(selector)==null){
            server.channel.register(selector, OP_ACCEPT, server);
            servers.add(server);
            if(objectName!=null)
                server.enableJMX();
        }
    }

    public void unregister(Server server){
        servers.remove(server);
        SelectionKey key = server.channel.keyFor(selector);
        if(key!=null && key.isValid())
            key.cancel();
    }

    /*-------------------------------------------------[ Clients ]---------------------------------------------------*/

    public Client newClient() throws IOException{
        validate();
        SocketChannel channel = SocketChannel.open();
        try{
            Client client = new Client(this, channel, null);
            return client;
        }catch(IOException ex){
            try{
                channel.close();
            }catch(IOException ex1){
                handleException(ex1);
            }
            throw ex;
        }
    }

    int acceptedClients;
    int connectionPendingClients;
    int connectedClients;

    public int acceptedClientsCount(){
        return acceptedClients;
    }

    public int connectionPendingClientsCount(){
        return connectionPendingClients;
    }

    public int connectedClientsCount(){
        return connectedClients;
    }

    public final ClientPool clientPool = new ClientPool(this);
    public final BufferPool bufferPool = new BufferPool();
    final TimeoutTracker timeoutTracker = new TimeoutTracker();

    /*-------------------------------------------------[ ReadyList ]---------------------------------------------------*/

    private Client readyListHead;

    void addToReadyList(Client client){
        if(client.readyPrev==null && client.readyNext==null){
            if(Debugger.IO)
                Debugger.println(client+".addToReadyList()");
            if(readyListHead==null){
                client.readyPrev = client;
                client.readyNext = client;
            }else{
                client.readyNext = readyListHead;
                client.readyPrev = readyListHead.poolPrev;
                readyListHead.poolPrev = client;
            }
            readyListHead = client;
        }
    }

    void processReadyList(){
        while(readyListHead!=null){
            Client removed = readyListHead;
            if(readyListHead.readyNext==readyListHead && readyListHead.readyPrev==readyListHead){
                readyListHead = null;
            }else{
                readyListHead = readyListHead.readyNext;
                readyListHead.readyPrev = removed.readyPrev;
                readyListHead.readyPrev.readyNext = readyListHead;
            }
            removed.readyPrev = null;
            removed.readyNext = null;

            activeClient = removed;
            removed.process();
        }
    }

    /*-------------------------------------------------[ Thread ]---------------------------------------------------*/

    public void start(){
        new ReactorThread().start();
    }

    public static Reactor current(){
        Thread thread = Thread.currentThread();
        if(thread instanceof ReactorThread)
            return ((ReactorThread)thread).reactor();
        else
            return null;
    }

    private class ReactorThread extends Thread implements Thread.UncaughtExceptionHandler{
        ReactorThread(){
            super(Reactor.this.toString);
            setUncaughtExceptionHandler(this);
        }
        Reactor reactor(){
            return Reactor.this;
        }

        @Override
        public void run(){
            Client client;
            while(true){
                try{
                    if(readyListHead!=null)
                        processReadyList();
                    runTasks();

                    if(initiateShutdown){
                        shutdownInProgress = true;
                        initiateShutdown = false;
                        if(Debugger.IO)
                            Debugger.println(Reactor.this+".shutdownInitialized: servers="+serversCount()+
                                    " connectedClients="+connectedClients+
                                    " connectionPendingClients="+connectionPendingClients+
                                    " acceptedClients="+acceptedClients);
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
                            connectedClients = connectionPendingClients = acceptedClients = 0;
                        }
                    }
                    if(isShutdown()){
                        selector.close();
                        if(Debugger.IO)
                            Debugger.println(Reactor.this+".shutdown");
                        synchronized(shutdownLock){
                            shutdownLock.notifyAll();
                        }
                        Reactors.reactors.remove(Reactor.this);
                        return;
                    }

                    long selectTimeout = timeoutTracker.isTracking() ? 1000 : 0;
                    if(Debugger.IO)
                        Debugger.println(Reactor.this+".select("+selectTimeout+"){");

                    int selected = selector.select(selectTimeout);
                    if(timeoutTracker.isTracking())
                        timeoutTracker.time = System.currentTimeMillis();
                    if(selected>0){
                        Iterator<SelectionKey> ready = selector.selectedKeys().iterator();
                        while(ready.hasNext()){
                            activeClient = null;
                            SelectionKey key = ready.next();
                            ready.remove();
                            if(key.attachment() instanceof Client){
                                client = (Client)key.attachment();
                                if(client.heapIndex!=-1){
                                    assert client.poolPrev==null && client.poolNext==null;
                                    timeoutTracker.untrack(client);
                                }
                                if(key.isValid()){
                                    if(Debugger.IO)
                                        Debugger.println(client+".process{");
                                    activeClient = client;
                                    client.process();
                                    if(Debugger.IO)
                                        Debugger.println("}");
                                }
                            }else{
                                Server server = (Server)key.attachment();
                                if(key.isValid())
                                    server.process(Reactor.this);
                            }
                        }
                    }
                    if(Debugger.IO)
                        Debugger.println("}");

                    while((client=timeoutTracker.next())!=null){
                        if(client.poolPrev!=null && client.poolNext!=null){
                            clientPool.remove(client);
                            client.close();
                        }else
                            client.processTimeout();
                    }
                }catch(Throwable ex){
                    handleException(ex);
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

    /*-------------------------------------------------[ Shutdown ]---------------------------------------------------*/

    private volatile boolean force;
    private volatile boolean initiateShutdown;
    private boolean shutdownInProgress;

    public void shutdown(){
        shutdown(false);
    }

    public void kill(){
        shutdown(true);
    }

    public void shutdown(boolean force){
        if(isShutdownPending() || isShutdown())
            return;
        this.force = force;
        initiateShutdown = true;
        selector.wakeup();
        if(Debugger.IO)
            Debugger.println(this+".shutdownRequested");
    }

    public boolean isShutdownPending(){
        return (initiateShutdown || shutdownInProgress) &&
                (serversCount()!=0 || connectedClients!=0 || connectionPendingClients!=0 || acceptedClients!=0);
    }

    public boolean isShutdown(){
        return shutdownInProgress && serversCount()==0
                && connectedClients==0 && connectionPendingClients==0 && acceptedClients==0;
    }

    protected void validate() throws IOException{
        if(isShutdownPending())
            throw new IOException("shutdown in progress");
        if(isShutdown())
            throw new IOException("already shutdown");
    }

    /*-------------------------------------------------[ ShutdownHook ]---------------------------------------------------*/

    public void shutdownOnExit(final boolean force){
        if(!isShutdown()){
            Runtime.getRuntime().addShutdownHook(new Thread(){
                @Override
                public void run(){
                    try{
                        shutdownAndWait(force);
                    }catch (InterruptedException ex){
                        handleException(ex);
                    }
                }
            });
        }
    }

    private final Object shutdownLock = new Object();
    public void waitForShutdown() throws InterruptedException{
        synchronized(shutdownLock){
            if(!isShutdown())
                shutdownLock.wait();
        }
    }

    public void shutdownAndWait(boolean force) throws InterruptedException{
        synchronized(shutdownLock){
            shutdown(force);
            if(!isShutdown())
                shutdownLock.wait();
        }
    }

    /*-------------------------------------------------[ JMX ]---------------------------------------------------*/

    private ObjectName objectName;
    public void enableJMX(){
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        try{
            objectName = new ObjectName("jlibs.nio:type=Reactor,id="+id);
            if(!mbeanServer.isRegistered(objectName))
                mbeanServer.registerMBean(createMBean(), objectName);
        }catch(Exception ex){
            throw new ImpossibleException(ex);
        }

        for(Server server: servers)
            server.enableJMX();
    }

    public ReactorMXBean createMBean(){
        return new ReactorMXBean(){
            @Override
            public int getServersCount(){
                return serversCount();
            }

            @Override
            public int getAcceptedClientsCount(){
                return acceptedClients;
            }

            @Override
            public int getConnectionPendingClientsCount(){
                return connectionPendingClients;
            }

            @Override
            public int getConnectedClientsCount(){
                return connectedClients;
            }

            @Override
            public int getPooledClientsCount(){
                return clientPool.count();
            }

            @Override
            @SuppressWarnings("unchecked")
            public Map<String, Integer> getPool(){
                Map<String, Integer> map[] = new Map[1];
                try{
                    invokeAndWait(()->{
                        map[0] = clientPool.entries.values().stream()
                                .filter(t -> t.count>0)
                                .collect(Collectors.toMap(t -> t.key, t -> t.count));
                    });
                }catch(InterruptedException ex){
                    throw new RuntimeException(ex);
                }
                return map[0];
            }
        };
    }

    @MXBean
    public static interface ReactorMXBean{
        public int getServersCount();
        public int getAcceptedClientsCount();
        public int getConnectionPendingClientsCount();
        public int getConnectedClientsCount();
        public int getPooledClientsCount();
        public Map<String, Integer> getPool();
    }

    @MXBean
    public static interface ServerMXBean{
        public String getType();
        public int getConnectedClientsCount();
        public boolean isOpen();
        public void close() throws IOException;
    }

    /*-------------------------------------------------[ Execution-Tracker ]---------------------------------------------------*/

    Client activeClient;
    public Client getActiveClient(){
        return activeClient;
    }

    final String executionID;
    public String getExecutionID(){
        return activeClient==null ? executionID : activeClient.getExecutionID();
    }

    /*-------------------------------------------------[ Internal ]---------------------------------------------------*/

    Internal internal = new Internal();
    public class Internal{
        public void closing(Client client){
            Server server = client.acceptedFrom;
            if(server!=null){
                acceptedClients--;
                int connectedClients = server.connectedClients.decrementAndGet();
                if(connectedClients==0 && !server.isOpen())
                    server.disableJMX();
            }else if(client.isConnected())
                connectedClients--;
            else if(client.isConnectionPending())
                connectionPendingClients--;
        }

        public void track(Client client){
            timeoutTracker.track(client);
        }

        public void addToReadyList(SelectableChannel channel){
            Client client = channel.getClient();
            int interests = channel.interestOps();
            int readyOps = channel.selfReadyOps();
            if((readyOps&OP_READ)!=0 && (interests&OP_READ)!=0)
                client.readyInputChannel = (SelectableInputChannel)channel;
            if((readyOps&OP_WRITE)!=0 && (interests&OP_WRITE)!=0)
                client.readyOutputChannel = (SelectableOutputChannel)channel;

            Reactor.this.addToReadyList(client);
        }
    }
}
