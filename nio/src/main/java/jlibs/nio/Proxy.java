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
import jlibs.nio.http.HTTPProxy;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class Proxy{
    public final String type;
    public final TCPEndpoint endpoint;
    public String user;
    public String password;

    protected Proxy(String type, TCPEndpoint endpoint){
        this.type = type;
        this.endpoint = endpoint;
    }

    public abstract void getConnection(TCPEndpoint endpoint, Consumer<Result<Connection>> listener);

    public static final Map<String, Proxy> DEFAULTS = new HashMap<>();
    static{
        try{
            String host = System.getProperty("http.proxyHost");
            if(host!=null){
                int port = Integer.parseInt(System.getProperty("http.proxyPort", "80"));
                Proxy proxy = new HTTPProxy(new TCPEndpoint(host, port));
                proxy.user = System.getProperty("http.proxyUser");
                proxy.password = System.getProperty("http.proxyPassword");
                DEFAULTS.put(proxy.type, proxy);
            }

            host = System.getProperty("https.proxyHost");
            if(host!=null){
                int port = Integer.parseInt(System.getProperty("https.proxyPort", "443"));
                Proxy proxy = new HTTPProxy(new TCPEndpoint(host, port));
                proxy.endpoint.sslContext = SSLUtil.defaultContext();
                proxy.user = System.getProperty("https.proxyUser");
                proxy.password = System.getProperty("https.proxyPassword");
                DEFAULTS.put(proxy.type, proxy);
            }

            host = System.getProperty("socksProxyHost");
            if(host!=null){
                int port = Integer.parseInt(System.getProperty("socksProxyPort", "1080"));
                Proxy proxy = new SocksProxy(5, new TCPEndpoint(host, port));
                proxy.user = System.getProperty("socksProxyUser");
                proxy.password = System.getProperty("socksProxyPassword");
                DEFAULTS.put(proxy.type, proxy);
            }
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
