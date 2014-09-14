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

import javax.management.MBeanServer;
import javax.management.MXBean;
import javax.management.ObjectName;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Map;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Management{
    public static MBeanServer MBEAN_SERVER = ManagementFactory.getPlatformMBeanServer();

    @MXBean
    public static interface ReactorMXBean{
        public int getServersCount();
        public int getAccepted();
        public int getConnectionPending();
        public int getConnected();
        public int getPooled();
        public Map<String, Integer> getPool();
    }

    @MXBean
    public static interface ServerMXBean{
        public String getType();
        public int getAccepted();
        public boolean isOpen();
        public void close() throws IOException;
    }

    static ObjectName register(Object mbean, String name){
        try{
            ObjectName objName = new ObjectName(name);
            if(!MBEAN_SERVER.isRegistered(objName))
                MBEAN_SERVER.registerMBean(mbean, objName);
            return objName;
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    static void unregister(ObjectName name){
        try{
            if(name!=null && !MBEAN_SERVER.isRegistered(name))
                MBEAN_SERVER.unregisterMBean(name);
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
