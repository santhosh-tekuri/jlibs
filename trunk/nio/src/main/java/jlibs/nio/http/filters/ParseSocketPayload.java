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

import jlibs.nio.http.*;
import jlibs.nio.http.msg.Message;
import jlibs.nio.http.util.MediaType;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class ParseSocketPayload implements ServerFilter, ClientFilter{
    @Override
    public boolean filter(ServerExchange exchange, FilterType type) throws Exception{
        return doFilter(exchange, type);
    }

    @Override
    public boolean filter(ClientExchange exchange, FilterType type) throws Exception{
        return doFilter(exchange, type);
    }

    private boolean doFilter(Exchange exchange, FilterType type) throws Exception{
        Message msg;
        if(type==FilterType.REQUEST)
            msg = exchange.getRequest();
        else if(type==FilterType.RESPONSE)
            msg = exchange.getResponse();
        else
            return true;

        if(!(msg.getPayload() instanceof SocketPayload))
            return true;

        SocketPayload payload = (SocketPayload)msg.getPayload();
        MediaType mt = payload.getMediaType();
        if(mt==null || !isCompatible(mt))
            return true;
        parse(exchange, msg, payload, mt);
        return false;
    }

    protected abstract boolean isCompatible(MediaType mt);
    protected abstract boolean parse(Exchange exchange, Message msg, SocketPayload payload, MediaType mt) throws Exception;
}

