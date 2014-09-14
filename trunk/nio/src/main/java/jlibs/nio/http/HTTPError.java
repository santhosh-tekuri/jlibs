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

import jlibs.core.lang.NotImplementedException;
import jlibs.nio.filters.InputLimitExceeded;
import jlibs.nio.http.msg.Status;

import java.net.SocketTimeoutException;

/**
 * @author Santhosh Kumar Tekuri
 */
public class HTTPError{
    public Status status;
    public Throwable detail;

    public HTTPError(Throwable error){
        detail = error;
        if(error instanceof Status){
            status = (Status)error;
            detail = null;
        }else if(error instanceof HTTPException){
            status = ((HTTPException)error).status;
            status = new Status(status.code, error.getMessage());
        }else if(error instanceof SocketTimeoutException){
            status = Status.REQUEST_TIMEOUT;
            detail = null;
        }else if(error instanceof InputLimitExceeded){
            status = Status.REQUEST_ENTITY_TOO_LARGE;
            detail = null;
        }else if(error instanceof NotImplementedException){
            if(error.getMessage()==null)
                status = Status.NOT_IMPLEMENTED;
            else
                status = new Status(Status.NOT_IMPLEMENTED.code, error.getMessage());
            detail = null;
        }else{
            String message = error.getMessage()==null ? error.getClass().getSimpleName() : error.getMessage();
            status = new Status(Status.INTERNAL_SERVER_ERROR.code, message);
        }
    }
}
