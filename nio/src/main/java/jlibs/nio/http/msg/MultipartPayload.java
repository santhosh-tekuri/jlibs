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

package jlibs.nio.http.msg;

import jlibs.core.lang.NotImplementedException;
import jlibs.nio.async.ExecutionContext;
import jlibs.nio.channels.ListenerUtil;

import java.io.OutputStream;

/**
 * @author Santhosh Kumar Tekuri
 */
public class MultipartPayload extends Payload{
    public final Multipart multipart;
    public boolean retain;

    public MultipartPayload(String contentType, Multipart multipart){
        super(contentType);
        this.multipart = multipart;
    }

    public void transferTo(OutputStream out, ExecutionContext context){
        try{
            throw new NotImplementedException();
//            ListenerUtil.resume(context, null, false);
        }catch(Throwable thr){
            ListenerUtil.resume(context, thr, false);
        }
    }
}
