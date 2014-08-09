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

import jlibs.nio.ClientEndpoint;
import jlibs.nio.http.HTTPClient;
import jlibs.nio.http.HTTPTask;
import jlibs.nio.http.msg.Method;
import jlibs.nio.http.msg.Request;
import jlibs.nio.http.msg.Response;
import jlibs.nio.http.msg.Status;
import jlibs.nio.http.util.HTTPURL;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author Santhosh Kumar Tekuri
 */
public class FollowRedirects implements HTTPTask.ResponseFilter<HTTPClient.Task>{
    private static ThreadLocal<Map<HTTPClient.Task, Integer>> map = ThreadLocal.withInitial(WeakHashMap::new);
    private int maxRedirects;

    public FollowRedirects(){
        this(20);
    }

    public FollowRedirects(int maxRedirects){
        this.maxRedirects = maxRedirects;
    }

    protected String getRedirectLocation(HTTPTask task){
        Request request = task.getRequest();
        if(request.method==Method.GET || request.method==Method.HEAD){
            Response response = task.getResponse();
            if(Status.isRedirection(response.statusCode))
                return response.getLocation();
        }
        return null;
    }

    @Override
    public void filter(HTTPClient.Task task) throws Exception{
        Integer count = map.get().remove(task);
        if(count==null)
            count = 0;
        if(count>=maxRedirects){
            task.resume();
            return;
        }

        String location = getRedirectLocation(task);
        if(location==null || location.isEmpty())
            task.resume();
        else{
            if(location.startsWith("/")){
                task.getRequest().uri = location;
                map.get().put(task, ++count);
                task.retry();
            }else if(location.startsWith("http://") || location.startsWith("https://")){
                HTTPURL url;
                try{
                    url = new HTTPURL(location);
                }catch(IllegalArgumentException ex){
                    task.resume(Status.BAD_RESPONSE, "illegal url: "+location, ex);
                    return;
                }
                task.getRequest().uri = url.path;
                map.get().put(task, ++count);
                ClientEndpoint clientEndpoint;
                try{
                    clientEndpoint = url.createClientEndpoint();
                }catch(Throwable thr){
                    task.resume(thr);
                    return;
                }
                task.retry(clientEndpoint);
            }else
                task.resume(); // @todo what abt relative location not starting with '/'
        }
    }
}
