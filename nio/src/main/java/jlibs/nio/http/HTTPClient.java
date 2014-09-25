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

package jlibs.nio.http;

import jlibs.nio.Proxy;
import jlibs.nio.SocksProxy;
import jlibs.nio.TCPEndpoint;
import jlibs.nio.http.util.HTTPURL;
import jlibs.nio.log.ConsoleLogHandler;
import jlibs.nio.log.LogHandler;

import javax.net.ssl.SSLException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Santhosh Kumar Tekuri
 */
public class HTTPClient{
    public Collection<ClientFilter> requestFilters = Collections.emptyList();
    public Collection<ClientFilter> responseFilters = Collections.emptyList();

    public ClientExchange newExchange(TCPEndpoint endpoint){
        return new ClientExchange(this, endpoint);
    }

    public ClientExchange newExchange(String url) throws GeneralSecurityException, SSLException{
        HTTPURL httpURL = new HTTPURL(url);
        ClientExchange exchange = new ClientExchange(this, httpURL.createEndpoint());
        exchange.setRequest(httpURL.createRequest());
        return exchange;
    }

    public Proxy proxy;
    public long maxResponseHeadSize = Defaults.MAX_RESPONSE_HEAD_SIZE;
    public String userAgent = Defaults.USER_AGENT;
    public long keepAliveTimeout = Defaults.KEEP_ALIVE_TIMEOUT;

    public AccessLog accessLog;
    public LogHandler logHandler = ConsoleLogHandler.INSTANCE;

    public HTTPClient(){
        proxy = Proxy.DEFAULTS.get(HTTPProxy.TYPE);
        if(proxy==null)
            proxy = Proxy.DEFAULTS.get(SocksProxy.TYPE);
    }

    public static class Defaults{
        public static long MAX_RESPONSE_HEAD_SIZE = 0;
        public static String USER_AGENT = null;

        // 0=turn off, +ve=turn on, -ve=respect what is there in request
        public static long KEEP_ALIVE_TIMEOUT = -60000L;
    }
}
