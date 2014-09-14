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

import jlibs.nio.TCPConnection;
import jlibs.nio.TCPEndpoint;
import jlibs.nio.TCPServer;
import jlibs.nio.http.accesslog.ServerAccessLog;
import jlibs.nio.listeners.IOListener;
import jlibs.nio.util.LogHandler;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Santhosh Kumar Tekuri
 */
public class HTTPServer implements TCPServer.Listener{
    public Collection<ServerFilter> requestFilters = Collections.emptyList();
    public Collection<ServerFilter> responseFilters = Collections.emptyList();
    public Collection<ServerFilter> errorFilters = Collections.emptyList();

    public final TCPEndpoint endpoint;
    public HTTPServer(TCPEndpoint endpoint){
        this.endpoint = endpoint;
    }

    private TCPServer server;
    public void start() throws IOException{
        server = endpoint.startServer(this);
    }

    public void stop(){
        server.close();
    }

    @Override
    public void accept(TCPConnection con){
        new IOListener().start(new ServerExchange(this), con);
    }

    public RequestListener listener;

    public boolean setDateHeader = Defaults.SET_DATE_HEADER;
    public long maxURISize = Defaults.MAX_URI_SIZE;
    public long maxRequestHeadSize = Defaults.MAX_REQUEST_HEAD_SIZE;
    public String serverName = Defaults.SERVER_NAME;
    public boolean supportsProxyConnectionHeader = Defaults.SUPPORTS_PROXY_CONNECTION_HEADER;

    public ServerAccessLog accessLog;
    public LogHandler logHandler;

    public static class Defaults{
        public static boolean SET_DATE_HEADER = false;
        public static long MAX_URI_SIZE = 0;
        public static long MAX_REQUEST_HEAD_SIZE = 0;
        public static String SERVER_NAME = null;
        public static boolean SUPPORTS_PROXY_CONNECTION_HEADER = false;
    }
}
