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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar Tekuri
 */
public class MultipartPayload extends Payload{
    public final List<Part> parts = new ArrayList<>();

    public MultipartPayload(MediaType mediaType){
        super(mediaType);
    }

    public MultipartPayload(String contentType){
        super(contentType);
    }

    public Part getPart(String name){
        for(Part part: parts){
            ContentDisposition cd = part.getContentDisposition();
            if(cd!=null && name.equals(cd.getName()))
                return part;
        }
        return null;
    }
}
