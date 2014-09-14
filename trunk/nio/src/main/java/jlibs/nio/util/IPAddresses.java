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

package jlibs.nio.util;

import jlibs.core.util.AbstractIterator;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Santhosh Kumar Tekuri
 */
public class IPAddresses extends AbstractIterator<InetAddress>{
    private List<Enumeration<NetworkInterface>> interfaces = new ArrayList<>();
    private Enumeration<InetAddress> addresses = Collections.emptyEnumeration();

    public IPAddresses() throws SocketException{
        interfaces.add(NetworkInterface.getNetworkInterfaces());
    }

    @Override
    protected Object computeNext(){
        while(true){
            if(addresses.hasMoreElements())
                return addresses.nextElement();
            if(interfaces.isEmpty())
                return NO_MORE_ELEMENTS;
            while(!interfaces.isEmpty()){
                Enumeration<NetworkInterface> enumer = interfaces.get(interfaces.size()-1);
                if(enumer.hasMoreElements()){
                    NetworkInterface ni = enumer.nextElement();
                    interfaces.add(ni.getSubInterfaces());
                    addresses = ni.getInetAddresses();
                    break;
                }else
                    interfaces.remove(interfaces.size()-1);
            }
        }
    }

    public static Stream<InetAddress> stream() throws IOException{
        int characteristics = Spliterator.NONNULL;
        Spliterator<InetAddress> spliterator = Spliterators.spliteratorUnknownSize(new IPAddresses(), characteristics);
        return StreamSupport.stream(spliterator, false);
    }

    public static void main(String[] args) throws IOException{
        IPAddresses.stream()
                .filter(inetAddress -> inetAddress instanceof Inet4Address)
                .forEach(inetAddress -> System.out.println(inetAddress.getHostAddress()));
    }
}
