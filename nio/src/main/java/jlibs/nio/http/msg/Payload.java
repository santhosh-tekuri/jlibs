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

import jlibs.nio.http.util.MediaType;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class Payload{
    public final String contentType;

    protected Payload(MediaType mediaType){
        this.mediaType = mediaType;
        contentType = mediaType==null ? null : mediaType.toString();
    }

    protected Payload(String contentType){
        this.contentType = contentType;
    }

    private MediaType mediaType;
    public MediaType getMediaType(){
        if(contentType==null)
            return null;
        if(mediaType==null)
            mediaType = new MediaType(contentType);
        return mediaType;
    }

    public long getContentLength(){
        return -1;
    }
}
