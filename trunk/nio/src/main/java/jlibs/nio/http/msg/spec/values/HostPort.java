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

package jlibs.nio.http.msg.spec.values;

import java.net.InetSocketAddress;

/**
 * @author Santhosh Kumar Tekuri
 */
public class HostPort{
    public final String host;
    public final int port;
    public final boolean portSpecified;

    public HostPort(String host, int port){
        this.host = host;
        this.port = port;
        portSpecified = true;
    }

    public HostPort(String host){
        this.host = host;
        port = -1;
        portSpecified = false;
    }

    public HostPort(InetSocketAddress address){
        this(address.getHostString(), address.getPort());
    }

    public int getPort(int defaultPort){
        return portSpecified ? port : defaultPort;
    }

    private String toString;

    @Override
    public String toString(){
        if(toString==null)
            toString = portSpecified ? host+':'+port : host;
        return toString;
    }
}
