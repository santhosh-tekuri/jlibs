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

import jlibs.core.net.SSLUtil;

import java.util.EnumMap;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Proxy{
    public enum Type{ HTTP, SOCKS }

    public final Type type;
    public final ClientEndpoint endpoint;
    public Proxy(Type type, ClientEndpoint endpoint){
        this.type = requireNonNull(type, "type==null");
        this.endpoint = requireNonNull(endpoint, "endpoint==null");
    }

    public String user;
    public String password;

    public static final EnumMap<Type, Proxy> DEFAULTS = new EnumMap<>(Type.class);

    static{
        try{
            String host = System.getProperty("http.proxyHost");
            if(host!=null){
                int port = Integer.parseInt(System.getProperty("http.proxyPort", "80"));
                Proxy proxy = new Proxy(Type.HTTP, new ClientEndpoint(host, port));
                proxy.user = System.getProperty("http.proxyUser");
                proxy.password = System.getProperty("http.proxyPassword");
                DEFAULTS.put(proxy.type, proxy);
            }

            host = System.getProperty("https.proxyHost");
            if(host!=null){
                int port = Integer.parseInt(System.getProperty("https.proxyPort", "443"));
                Proxy proxy = new Proxy(Type.HTTP, new ClientEndpoint(host, port));
                proxy.endpoint.sslContext = SSLUtil.defaultContext();
                proxy.user = System.getProperty("https.proxyUser");
                proxy.password = System.getProperty("https.proxyPassword");
                DEFAULTS.put(proxy.type, proxy);
            }

            host = System.getProperty("socksProxyHost");
            if(host!=null){
                int port = Integer.parseInt(System.getProperty("socksProxyPort", "443"));
                Proxy proxy = new Proxy(Type.SOCKS, new ClientEndpoint(host, port));
                proxy.user = System.getProperty("socksProxyUser");
                proxy.password = System.getProperty("socksProxyPassword");
                DEFAULTS.put(proxy.type, proxy);
            }
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
