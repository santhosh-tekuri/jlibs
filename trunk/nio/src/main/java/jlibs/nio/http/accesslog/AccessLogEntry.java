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

package jlibs.nio.http.accesslog;

import jlibs.nio.http.Exchange;
import jlibs.nio.http.msg.Message;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class AccessLogEntry{
    public final Class<? extends Exchange> exchangeType;
    public final Class<? extends Message> messageType;
    public AccessLogEntry(Class<? extends Exchange> exchangeType, Class<? extends Message> messageType){
        this.exchangeType = exchangeType;
        this.messageType = messageType;
    }

    public abstract String getValue(Exchange exchange);

    public boolean matches(Exchange exchange, Message msg){
        return (exchangeType==null || exchangeType==exchange.getClass()) &&
                (messageType==null || messageType==msg.getClass());
    }
}
