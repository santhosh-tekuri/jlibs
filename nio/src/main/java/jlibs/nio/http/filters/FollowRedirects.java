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

import jlibs.nio.http.Key;
import jlibs.nio.http.ClientExchange;
import jlibs.nio.http.ClientFilter;
import jlibs.nio.http.FilterType;
import jlibs.nio.http.msg.Method;
import jlibs.nio.http.msg.Request;
import jlibs.nio.http.msg.Response;
import jlibs.nio.http.msg.Status;
import jlibs.nio.http.util.HTTPURL;

/**
 * @author Santhosh Kumar Tekuri
 *
 * Client Response Filter
 */
public class FollowRedirects implements ClientFilter{
    private static final Key<Integer> REDIRECT_COUNT = new Key<>("RedirectCount", 0);

    private int maxRedirects;
    public FollowRedirects(int maxRedirects){
        this.maxRedirects = maxRedirects;
    }

    public FollowRedirects(){
        this(20);
    }

    @Override
    public boolean filter(ClientExchange exchange, FilterType type) throws Exception{
        assert type==FilterType.RESPONSE;
        String location = getRedirectLocation(exchange);
        if(location==null || location.isEmpty())
            return true;

        int count = exchange.attachment(REDIRECT_COUNT);
        if(count>=maxRedirects)
            return true;

        if(location.startsWith("/")){
            exchange.getRequest().uri = location;
            exchange.attach(REDIRECT_COUNT, ++count);
            exchange.retry();
        }else if(location.startsWith("http://") || location.startsWith("https://")){
            HTTPURL url;
            try{
                url = new HTTPURL(location);
            }catch(IllegalArgumentException ex){
                throw Status.BAD_GATEWAY.with(ex);
            }
            exchange.getRequest().uri = url.path;
            exchange.attach(REDIRECT_COUNT, ++count);
            exchange.retry(url.createEndpoint());
        }
        // @todo what abt relative location not starting with '/'
        return true;
    }

    protected String getRedirectLocation(ClientExchange exchange){
        Request request = exchange.getRequest();
        if(request.method== Method.GET || request.method==Method.HEAD){
            Response response = exchange.getResponse();
            if(response.status.isRedirection())
                return response.getLocation();
        }
        return null;
    }
}
