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

package jlibs.nio.http;

import jlibs.nio.http.msg.Message;
import jlibs.nio.http.msg.Request;
import jlibs.nio.http.msg.Status;

/**
 * @author Santhosh Kumar Tekuri
 */
public class HTTPException extends RuntimeException{
    public final int statusCode;
    private HTTPException(int statusCode, String message, Throwable cause){
        super(message);
        this.statusCode = statusCode;
        initCause(cause);
    }

    public static HTTPException valueOf(int statusCode, Throwable cause){
        if(cause instanceof HTTPException)
            return (HTTPException)cause;
        else
            return valueOf(statusCode, Status.message(statusCode), cause);
    }

    public static HTTPException valueOf(int statusCode, String message, Throwable cause){
        if(cause instanceof HTTPException)
            return (HTTPException)cause;
        else
            return new HTTPException(statusCode, message, cause) ;
    }

    public static HTTPException badMessage(Message message, String reasonPhrase, Throwable cause){
        int statusCode = message instanceof Request ? Status.BAD_REQUEST : Status.BAD_RESPONSE;
        return valueOf(statusCode, reasonPhrase, cause);
    }
}
