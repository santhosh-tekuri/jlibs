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

import jlibs.core.io.IOUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * @author Santhosh Kumar Tekuri
 */
public class StringPayload extends EncodablePayload{
    public final String content;
    public final Charset charset;

    public StringPayload(String content, String contentType, Charset charset){
        super(contentType);
        this.content = content;
        this.charset = charset;
    }

    public StringPayload(String content, String contentType){
        this(content, contentType, IOUtil.UTF_8);
    }

    public StringPayload(String content){
        this(content, "text/plain; charset=utf-8", IOUtil.UTF_8);
    }

    @Override
    public void writeTo(OutputStream out) throws IOException{
        out.write(content.getBytes(charset));
    }
}
