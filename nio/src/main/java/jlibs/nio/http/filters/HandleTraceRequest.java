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
import jlibs.nio.http.msg.EncodablePayload;
import jlibs.nio.http.msg.Method;
import jlibs.nio.http.msg.Response;

/**
 * @author Santhosh Kumar Tekuri
 */
public class HandleTraceRequest implements HTTPTask.RequestFilter<HTTPServer.Task>{
    public static final HandleTraceRequest INSTANCE = new HandleTraceRequest();

    @Override
    public void filter(HTTPServer.Task task) throws Exception{
        if(task.getRequest().method==Method.TRACE){
            Response response = task.getResponse();
            if(response==null)
                task.setResponse(response=new Response());
            response.setPayload(EncodablePayload.newInstance("message/http", task.getRequest()), true);
        }
        task.resume();
    }
}
