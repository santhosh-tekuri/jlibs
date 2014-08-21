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

import jlibs.nio.http.HTTPTask;
import jlibs.nio.http.async.ReadMultipart;
import jlibs.nio.http.msg.*;
import jlibs.nio.http.msg.spec.values.MediaType;

/**
 * @author Santhosh Kumar Tekuri
 */
public class MultipartUnmarshalling implements HTTPTask.RequestFilter<HTTPTask>, HTTPTask.ResponseFilter<HTTPTask>{
    public static final MultipartUnmarshalling INSTANCE_REQUEST = new MultipartUnmarshalling(true);
    public static final MultipartUnmarshalling INSTANCE_RESPONSE = new MultipartUnmarshalling(false);
    private boolean parseRequest;

    public MultipartUnmarshalling(boolean parseRequest){
        this.parseRequest = parseRequest;
    }

    public MultipartUnmarshalling(){
        this(true);
    }

    @Override
    public void filter(HTTPTask task) throws Exception{
        Message message = parseRequest ? task.getRequest() : task.getResponse();
        Payload payload = message.getPayload();

        if(payload.getContentLength()!=0 && payload instanceof RawPayload){
            RawPayload rawPayload = (RawPayload)payload;

            MediaType mt = null;
            if(rawPayload.channel!=null && payload.contentType!=null)
                mt = new MediaType(payload.contentType);
            if(mt==null || !mt.isMultipart()){
                task.resume();
                return;
            }

            rawPayload.removeEncodings();
            Multipart multipart = new Multipart();
            new ReadMultipart(multipart, mt.getBoundary()).start(rawPayload.channel, (thr, timeout) -> {
                if(thr!=null)
                    task.resume(parseRequest ? Status.BAD_REQUEST : Status.BAD_RESPONSE, thr);
                else if(timeout)
                    task.resume(parseRequest ? Status.REQUEST_TIMEOUT : Status.RESPONSE_TIMEOUT);
                else{
                    try{
                        message.setPayload(new MultipartPayload(payload.contentType, multipart), true);
                    }catch(Throwable ex){
                        task.resume(ex);
                        return;
                    }
                    task.resume();
                }
            });
        }else
            task.resume();
    }
}
