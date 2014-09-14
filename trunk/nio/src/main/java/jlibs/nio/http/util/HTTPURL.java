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

package jlibs.nio.http.util;

import jlibs.core.net.Protocol;
import jlibs.core.net.SSLUtil;
import jlibs.nio.TCPEndpoint;
import jlibs.nio.http.msg.Request;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;

/**
 * @author Santhosh Kumar Tekuri
 */
public class HTTPURL{
    public final String url;
    public final String protocol;
    public final String host;
    public final int port;
    public final String path;
    public final boolean secured;

    public HTTPURL(String url) throws IllegalArgumentException{
        this.url = url;
        int colon = url.indexOf("://");
        if(colon==-1)
            throw new IllegalArgumentException("Protocol Missing");

        protocol = url.substring(0, colon);
        Protocol p = Protocol.valueOf(protocol.toUpperCase());
        secured = p== Protocol.HTTPS;

        String host;
        int slash = url.indexOf('/', colon+3);
        if(slash==-1){
            host = url.substring(colon+3);
            path = "/";
        }else{
            host = url.substring(colon+3, slash);
            path = url.substring(slash);
        }

        colon = host.indexOf(':');
        if(colon==-1)
            port = p.port();
        else{
            try{
                port = Integer.parseInt(host.substring(colon+1));
            }catch(NumberFormatException ex){
                throw new IllegalArgumentException("Bad Port");
            }
            host = host.substring(0, colon);
        }
        this.host = host;
    }

    public InetSocketAddress socketAddress(){
        return new InetSocketAddress(host, port);
    }

    public TCPEndpoint createEndpoint() throws GeneralSecurityException, SSLException{
        TCPEndpoint clientEndpoint = new TCPEndpoint(host, port);
        if(secured)
            clientEndpoint.sslContext = SSLUtil.defaultContext();
        return clientEndpoint;
    }

    public Request createRequest(){
        Request request = new Request();
        request.uri = path;
        return request;
    }

    @Override
    public String toString(){
        return protocol+"://"+host+':'+port+path;
    }
}