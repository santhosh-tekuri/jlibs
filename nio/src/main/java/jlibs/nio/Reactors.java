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

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Reactors{
    static final List<Reactor> reactors = Collections.synchronizedList(new ArrayList<>());
    static AtomicInteger serverCount = new AtomicInteger();

    public static void start() throws IOException{
        start(Runtime.getRuntime().availableProcessors());
    }

    public static void start(int noOfReactors) throws IOException{
        for(int i=0; i<noOfReactors; i++)
            new Reactor().start();
    }

    public static Reactor reactor(){
        synchronized(reactors){
            return reactors.isEmpty() ? null : reactors.get(0);
        }
    }

    public static void setExceptionHandler(Consumer<Throwable> exceptionHandler){
        reactors.forEach(reactor -> reactor.invokeLater(() -> reactor.setExceptionHandler(exceptionHandler)));
    }

    public static void forEach(Consumer<Reactor> consumer){
        reactors.forEach(consumer);
    }

    public static void register(Server server){
        reactors.forEach(reactor -> {
            reactor.invokeLater(() -> {
                try{
                    reactor.register(server);
                }catch(Throwable thr){
                    reactor.handleException(thr);
                }
            });
        });
    }

    public static void unregister(Server server){
        reactors.forEach(reactor -> {
            reactor.invokeLater(() -> {
                try{
                    reactor.unregister(server);
                }catch(Throwable thr){
                    reactor.handleException(thr);
                }
            });
        });
    }

    public static int size(){
        return reactors.size();
    }

    public static int lastReactorID(){
        return Reactor.ID_GENERATOR.get();
    }


    public static void enableJMX(){
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try{
            ObjectName objectName = new ObjectName("jlibs.nio:type=Reactors");
            if(!server.isRegistered(objectName))
                server.registerMBean(createMBean(), objectName);
        }catch(Exception ex){
            throw new ImpossibleException();
        }

        reactors.forEach(Reactor::enableJMX);
    }

    public static Reactor.ReactorMXBean createMBean(){
        return new Reactor.ReactorMXBean(){
            @Override
            public int getServersCount(){
                return serverCount.get();
            }

            private int getCount(ToIntFunction<Reactor> function){
                int count[] = { 0 };
                reactors.forEach(reactor -> {
                    count[0] += function.applyAsInt(reactor);
                });
                return count[0];
            }

            @Override
            public int getAcceptedClientsCount(){
                return getCount(Reactor::acceptedClientsCount);
            }

            @Override
            public int getConnectionPendingClientsCount(){
                return getCount(Reactor::connectionPendingClientsCount);
            }

            @Override
            public int getConnectedClientsCount(){
                return getCount(Reactor::connectedClientsCount);
            }

            @Override
            public int getPooledClientsCount(){
                return getCount(reactor -> reactor.clientPool.count());
            }

            @Override
            public Map<String, Integer> getPool(){
                Map<String, Integer> map = new HashMap<>();
                reactors.forEach(reactor -> {
                    Map<String, Integer> pool = reactor.createMBean().getPool();
                    for(Map.Entry<String, Integer> entry: pool.entrySet())
                        map.compute(entry.getKey(), (s, count) -> count+entry.getValue());
                });
                return map;
            }
        };
    }
}
