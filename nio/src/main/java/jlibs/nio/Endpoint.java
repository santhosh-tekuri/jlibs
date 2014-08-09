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

import jlibs.core.net.Protocol;
import jlibs.core.net.SSLUtil;
import jlibs.nio.http.HTTPException;
import jlibs.nio.http.msg.Status;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class Endpoint{
    public final String host;
    public final int port;
    private final String toString;

    protected Endpoint(String host, int port){
        this.host = requireNonNull(host, "host==null");
        this.port = port;
        toString = host+':'+port;
    }

    protected Endpoint(InetSocketAddress address){
        this(address.getHostString(), address.getPort());
    }

    protected Endpoint(String url) throws GeneralSecurityException, SSLException{
        Protocol protocol = Protocol.TCP;
        int port = -1;
        int colon = url.indexOf("://");
        if(colon!=-1){
            protocol = Protocol.valueOf(url.substring(0, colon).toUpperCase());
            url = url.substring(colon+"://".length());
        }
        colon = url.indexOf(':');
        if(colon!=-1){
            port = Integer.parseInt(url.substring(colon+1));
            url = url.substring(0, colon);
        }
        if(port==-1)
            port = protocol.port();


        this.host = url;
        this.port = port;
        toString = host+':'+port;
        if(protocol.secured())
            sslContext = SSLUtil.defaultContext();
    }

    public InetSocketAddress socketAddress(){
        return new InetSocketAddress(host, port);
    }

    public SSLContext sslContext;

    @Override
    public final String toString(){
        return toString;
    }
}
