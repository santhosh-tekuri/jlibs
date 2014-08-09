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
import jlibs.nio.http.msg.Message;
import jlibs.nio.http.msg.Payload;
import jlibs.nio.http.msg.Status;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ReadPayload implements HTTPTask.RequestFilter<HTTPTask>, HTTPTask.ResponseFilter<HTTPTask>{
    public static final ReadPayload READ_REQUEST_PAYLOAD = new ReadPayload(true);
    public static final ReadPayload READ_RESPONSE_PAYLOAD = new ReadPayload(false);

    public final boolean readRequest;
    public ReadPayload(boolean readRequest){
        this.readRequest = readRequest;
    }

    @Override
    public void filter(HTTPTask task) throws Exception{
        Message message = readRequest ? task.getRequest() : task.getResponse();
        Payload payload = message.getPayload();
        if(payload.contentLength==0)
            task.resume();
        else{
            payload.readFromSource(-1, (thr,timeout) -> {  // @todo: how to configure limit
                if(thr!=null)
                    task.resume(readRequest ? Status.BAD_REQUEST : Status.BAD_RESPONSE, thr);
                else if(timeout)
                    task.resume(readRequest ? Status.REQUEST_TIMEOUT : Status.RESPONSE_TIMEOUT);
                else
                    task.resume();
            });
        }
    }
}
