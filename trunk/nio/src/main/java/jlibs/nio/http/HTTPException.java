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

import jlibs.nio.http.msg.Status;

/**
 * @author Santhosh Kumar Tekuri
 */
public class HTTPException extends RuntimeException{
    public final Status status;

    public HTTPException(Throwable cause, Status status){
        super(cause);
        this.status = status;
    }

    public HTTPException(String message, Status status){
        super(message);
        this.status = status;
    }

    public HTTPException(String message, Throwable cause, Status status){
        super(message, cause);
        this.status = status;
    }
}
