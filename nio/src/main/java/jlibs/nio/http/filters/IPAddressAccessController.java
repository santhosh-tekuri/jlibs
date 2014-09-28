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

package jlibs.nio.http.filters;

import jlibs.nio.http.FilterType;
import jlibs.nio.http.ServerExchange;
import jlibs.nio.http.ServerFilter;
import jlibs.nio.http.msg.Status;
import jlibs.nio.util.IPV4Pattern;
import jlibs.nio.util.IPV6Pattern;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Santhosh Kumar Tekuri
 *
 * Server Request Filter
 */
public class IPAddressAccessController implements ServerFilter{
    public Map<IPV4Pattern, Boolean> ipv4ACL = new LinkedHashMap<>();
    public Map<IPV6Pattern, Boolean> ipv6ACL = new LinkedHashMap<>();
    public boolean defaultAllow;
    public Status denyStatus = Status.FORBIDDEN;

    public IPAddressAccessController defaultAllow(boolean allow){
        defaultAllow = allow;
        return this;
    }

    public IPAddressAccessController denyStatus(Status status){
        denyStatus = status;
        return this;
    }

    public void addPattern(String pattern, boolean allow){
        if(pattern.indexOf('.')>=0)
            ipv4ACL.put(new IPV4Pattern(pattern), allow);
        else
            ipv6ACL.put(new IPV6Pattern(pattern), allow);
    }

    @Override
    public boolean filter(ServerExchange exchange, FilterType type) throws Exception{
        assert type==FilterType.REQUEST;
        InetAddress address = exchange.getClientAddress();
        boolean allow = defaultAllow;
        if(address instanceof Inet4Address){
            for(Map.Entry<IPV4Pattern, Boolean> entry: ipv4ACL.entrySet()){
                if(entry.getKey().matches(address)){
                    allow = entry.getValue();
                    break;
                }
            }
        }else if(address instanceof Inet6Address){
            for(Map.Entry<IPV6Pattern, Boolean> entry: ipv6ACL.entrySet()){
                if(entry.getKey().matches(address)){
                    allow = entry.getValue();
                    break;
                }
            }
        }
        if(allow)
            return true;
        else
            throw denyStatus;
    }
}
