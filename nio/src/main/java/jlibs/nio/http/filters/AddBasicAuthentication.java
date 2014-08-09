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

import jlibs.nio.http.HTTPClient;
import jlibs.nio.http.HTTPTask;
import jlibs.nio.http.msg.spec.values.BasicCredentials;

/**
 * @author Santhosh Kumar Tekuri
 */
public class AddBasicAuthentication implements HTTPTask.RequestFilter<HTTPClient.Task>{
    private boolean proxy;
    private BasicCredentials credentials;

    public AddBasicAuthentication(boolean proxy, BasicCredentials credentials){
        this.proxy = proxy;
        this.credentials = credentials;
    }

    public AddBasicAuthentication(BasicCredentials credentials){
        this(false, credentials);
    }

    @Override
    public void filter(HTTPClient.Task task) throws Exception{
        task.getRequest().setCredentials(proxy, credentials);
        task.resume();
    }
}
