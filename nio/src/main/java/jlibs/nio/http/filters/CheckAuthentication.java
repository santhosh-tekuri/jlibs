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

import jlibs.nio.http.ServerExchange;
import jlibs.nio.http.ServerFilter;
import jlibs.nio.http.msg.Response;
import jlibs.nio.http.msg.Status;
import jlibs.nio.http.util.Challenge;
import jlibs.nio.http.util.Credentials;

/**
 * @author Santhosh Kumar Tekuri
 *
 * Server Request Filter
 */
public abstract class CheckAuthentication implements ServerFilter{
    protected final Authenticator authenticator;
    protected final boolean proxy;
    protected CheckAuthentication(Authenticator authenticator, boolean proxy){
        this.authenticator = authenticator;
        this.proxy = proxy;
    }

    protected Credentials getCredentials(ServerExchange exchange){
        try{
            return exchange.getRequest().getCredentials(proxy);
        }catch(Exception ex){
            throw Status.BAD_REQUEST.with(ex);
        }
    }

    protected void authorized(ServerExchange exchange, String user){
        if(proxy)
            exchange.getRequest().setCredentials(null);
        authenticator.authorized(exchange, user);
    }

    protected Status unauthorized(ServerExchange exchange, Challenge challenge){
        Response response = new Response();
        if(proxy){
            response.status = Status.PROXY_AUTHENTICATION_REQUIRED;
            response.setProxyChallenge(challenge);
        }else{
            response.status = Status.UNAUTHORIZED;
            response.setChallenge(challenge);
        }
        exchange.setResponse(response);
        return response.status;
    }
}
