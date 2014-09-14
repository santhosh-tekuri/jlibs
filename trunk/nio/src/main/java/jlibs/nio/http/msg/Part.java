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

import jlibs.nio.http.util.ContentDisposition;
import jlibs.nio.http.util.MediaType;

import static jlibs.nio.http.msg.Message.CONTENT_DISPOSITION;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class Part{
    public final Headers headers;
    public Part(Headers headers){
        this.headers = headers;
    }

    /*-------------------------------------------------[ Content-Disposition ]---------------------------------------------------*/

    public ContentDisposition getContentDisposition(){
        return headers.getSingleValue(CONTENT_DISPOSITION, ContentDisposition::new);
    }

    public void setContentDisposition(ContentDisposition cd){
        headers.setSingleValue(CONTENT_DISPOSITION, cd, null);
    }

    /*-------------------------------------------------[ Content-Type ]---------------------------------------------------*/

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.17
    public static final AsciiString CONTENT_TYPE = new AsciiString("Content-Type");

    public MediaType getMediaType(){
        return headers.getSingleValue(CONTENT_TYPE, MediaType::new);
    }

    public void setMediaType(MediaType mt){
        headers.setSingleValue(CONTENT_TYPE, mt, null);
    }
}
