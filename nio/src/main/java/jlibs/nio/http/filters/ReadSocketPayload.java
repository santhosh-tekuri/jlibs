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

import jlibs.nio.Input;
import jlibs.nio.http.*;
import jlibs.nio.http.msg.Message;
import jlibs.nio.listeners.IOListener;
import jlibs.nio.listeners.ReadBuffers;
import jlibs.nio.util.Buffers;

/**
 * @author Santhosh Kumar Tekuri
 *
 * Server Request Filter or Client Response Filter
 */
public class ReadSocketPayload implements ServerFilter, ClientFilter{
    @Override
    public boolean filter(ClientExchange exchange, FilterType type) throws Exception{
        return doFilter(exchange, type);
    }

    @Override
    public boolean filter(ServerExchange exchange, FilterType type) throws Exception{
        return doFilter(exchange, type);
    }

    private boolean doFilter(Exchange exchange, FilterType type) throws Exception{
        Message msg = type==FilterType.REQUEST ? exchange.getRequest() : exchange.getResponse();
        if(msg!=null && msg.getPayload() instanceof SocketPayload){
            SocketPayload payload = (SocketPayload)msg.getPayload();
            Input in = payload.socket();
            if(in.isOpen()){
                Buffers buffers = payload.buffers;
                if(buffers==null)
                    payload.buffers = buffers = new Buffers();
                new IOListener()
                    .setCallback(Exchange::resume, exchange)
                    .start(new ReadBuffers(buffers), in, null);
                return false;
            }
        }
        return true;
    }
}
