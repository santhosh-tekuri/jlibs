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

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Reactors{
    private static List<Reactor> reactors;

    public static void start() throws IOException{
        start(Runtime.getRuntime().availableProcessors());
    }

    public static void start(int count) throws IOException{
        if(reactors!=null)
            throw new UnsupportedOperationException("Reactors already started");
        Reactor reactors[] = new Reactor[count];
        for(int i=0; i<reactors.length; i++){
            reactors[i] = new Reactor(i);
            reactors[i].start();
        }
        Reactors.reactors = Collections.unmodifiableList(Arrays.asList(reactors));
        Management.register(new Management.ReactorMXBean(){
            @Override
            public int getServersCount(){
                return Arrays.stream(reactors).mapToInt(Reactor::getServersCount).max().getAsInt();
            }

            @Override
            public int getAccepted(){
                return Arrays.stream(reactors).mapToInt(Reactor::getAccepted).sum();
            }

            @Override
            public int getConnectionPending(){
                return Arrays.stream(reactors).mapToInt(Reactor::getConnectionPending).sum();
            }

            @Override
            public int getConnected(){
                return Arrays.stream(reactors).mapToInt(Reactor::getConnected).sum();
            }

            @Override
            public int getPooled(){
                return Arrays.stream(reactors).mapToInt(reactor -> reactor.connectionPool.count()).sum();
            }

            @Override
            public Map<String, Integer> getPool(){
                try{
                    Map<String, Integer> map = new HashMap<>();
                    for(Reactor reactor: reactors){
                        reactor.invokeAndWait(() -> {
                            Map<String, Integer> pool = reactor.connectionPool.entries.values().stream()
                                    .filter(t -> t.count>0)
                                    .collect(Collectors.toMap(t -> t.key, t -> t.count));
                            for(Map.Entry<String, Integer> entry : pool.entrySet()){
                                Integer count = map.getOrDefault(entry.getKey(), 0);
                                map.put(entry.getKey(), count+entry.getValue());
                            }
                        });
                    }
                    return map;
                }catch(InterruptedException ex){
                    throw new RuntimeException(ex);
                }
            }
        }, "jlibs.nio:type=Reactors");
    }

    public static List<Reactor> get(){
        return reactors;
    }

    public static void shutdown(boolean force){
        for(Reactor reactor: reactors)
            reactor.invokeLater(() -> reactor.shutdown(force));
    }
}
