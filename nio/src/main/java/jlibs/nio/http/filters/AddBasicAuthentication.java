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

import jlibs.nio.http.ClientExchange;
import jlibs.nio.http.ClientFilter;
import jlibs.nio.http.FilterType;
import jlibs.nio.http.util.BasicCredentials;

/**
 * @author Santhosh Kumar Tekuri
 *
 * Client Request Filter
 */
public class AddBasicAuthentication implements ClientFilter{
    private BasicCredentials credentials;
    private boolean proxy;

    public AddBasicAuthentication(BasicCredentials credentials){
        this(credentials, false);
    }

    public AddBasicAuthentication(BasicCredentials credentials, boolean proxy){
        this.credentials = credentials;
        this.proxy = proxy;
    }

    @Override
    public boolean filter(ClientExchange exchange, FilterType type) throws Exception{
        assert type==FilterType.REQUEST;
        exchange.getRequest().setCredentials(credentials, proxy);
        return true;
    }
}
