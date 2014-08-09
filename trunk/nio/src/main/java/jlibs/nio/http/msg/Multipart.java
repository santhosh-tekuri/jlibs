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

import jlibs.nio.http.msg.spec.values.ContentDisposition;
import jlibs.nio.http.msg.spec.values.MediaType;

import java.util.ArrayList;
import java.util.List;

import static jlibs.nio.http.msg.Headers.CONTENT_DISPOSITION;
import static jlibs.nio.http.msg.Headers.CONTENT_TYPE;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Multipart{
    public Object preamble;
    public Encoder preambleEncoder;

    public List<Part> parts = new ArrayList<>();

    public Object epilogue;
    public Encoder epilogueEncoder;

    public static class Part{
        public Headers headers = new Headers();
        public Object payload;
        public Encoder payloadEncoder;

        public ContentDisposition getContentDisposition(){
            return CONTENT_DISPOSITION.get(this);
        }

        public void setContentDisposition(ContentDisposition contentDisposition){
            CONTENT_DISPOSITION.set(this, contentDisposition);
        }

        public MediaType getMediaType(){
            return CONTENT_TYPE.get(this);
        }

        public void setMediaType(MediaType mediaType){
            CONTENT_TYPE.set(this, mediaType);
        }
    }
}
