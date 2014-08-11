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

import jlibs.nio.http.HTTPServer;
import jlibs.nio.http.HTTPTask;
import jlibs.nio.http.msg.Response;
import jlibs.nio.http.msg.Status;
import jlibs.nio.http.msg.spec.values.Challenge;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class CheckAuthentication implements HTTPTask.RequestFilter<HTTPServer.Task>{
    protected final boolean proxy;
    protected Authenticator authenticator;
    protected CheckAuthentication(boolean proxy, Authenticator authenticator){
        this.proxy = proxy;
        this.authenticator = authenticator;
    }

    protected void unauthorized(HTTPServer.Task task, Challenge challenge){
        Response response = new Response();
        if(proxy){
            response.setStatus(Status.PROXY_AUTHENTICATION_REQUIRED);
            response.setProxyChallenge(challenge);
        }else{
            response.setStatus(Status.UNAUTHORIZED);
            response.setChallenge(challenge);
        }
        task.setResponse(response);
        task.resume(response.statusCode);
    }

    protected void authorized(HTTPServer.Task task, String user){
        if(proxy)
            task.getRequest().setCredentials(null);
        authenticator.authorized(task, user);
        task.resume();
    }

    public static interface Authenticator{
        public String getPassword(String user);
        public default void authorized(HTTPServer.Task task, String user){}
    }
}
