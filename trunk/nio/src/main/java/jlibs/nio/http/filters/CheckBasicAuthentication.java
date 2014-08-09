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
import jlibs.nio.http.msg.Request;
import jlibs.nio.http.msg.Response;
import jlibs.nio.http.msg.Status;
import jlibs.nio.http.msg.spec.values.BasicChallenge;
import jlibs.nio.http.msg.spec.values.BasicCredentials;
import jlibs.nio.http.msg.spec.values.Credentials;

import java.util.function.Function;

/**
 * @author Santhosh Kumar Tekuri
 */
public class CheckBasicAuthentication implements HTTPTask.RequestFilter<HTTPServer.Task>{
    private boolean proxy;
    private BasicChallenge challenge;
    private Function<BasicCredentials, Boolean> authenticator;

    public CheckBasicAuthentication(boolean proxy, String realm, Function<BasicCredentials, Boolean> authenticator){
        this.proxy = proxy;
        challenge = new BasicChallenge(realm);
        this.authenticator = authenticator;
    }

    @Override
    public void filter(HTTPServer.Task task) throws Exception{
        Request request = task.getRequest();
        Credentials credentials = request.getCredentials(proxy);
        if(credentials instanceof BasicCredentials){
            BasicCredentials basicCredentials = (BasicCredentials)credentials;
            if(authenticator.apply(basicCredentials)){
                task.resume();
                return;
            }
        }

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
}
