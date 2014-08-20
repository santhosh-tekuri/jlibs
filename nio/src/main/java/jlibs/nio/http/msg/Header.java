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

import jlibs.nio.util.Bytes;
import jlibs.nio.util.NIOUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Header implements Encodable{
    String name;
    String value;

    public Header(String name, String value){
        this.name = name;
        this.value = value;
    }

    public String getName(){
        return name;
    }

    public String getValue(){
        return value;
    }

    public void setValue(String value){
        this.value = value;
    }

    @Override
    public String toString(){
        return name+": "+value;
    }

    Header sameNext;
    Header samePrev = this;

    public Header sameNext(){
        return sameNext;
    }

    Header next;
    Header prev = this;

    public Header next(){
        return next;
    }

    ByteBuffer encode(Bytes bytes, ByteBuffer buffer){
        buffer = bytes.append(name, buffer);
        buffer = bytes.append(": ", buffer);
        buffer = bytes.append(value, buffer);
        return bytes.append("\r\n", buffer);
    }

    @Override
    public void encodeTo(OutputStream out) throws IOException{
        NIOUtil.writeAscii(name, out);
        out.write(':');
        out.write(' ');
        NIOUtil.writeAscii(value, out);
        out.write('\r');
        out.write('\n');
    }

    public static interface Names{
        String CONNECTION = "Connection";
        String PROXY_CONNECTION = "Proxy-Connection";
        String CONTENT_LENGTH = "Content-Length";
        String CONTENT_TYPE = "Content-Type";
    }

    public static interface Values{
        String IDENTITY = "identity";
        String CHUNKED = "chunked";
        String GZIP = "gzip";
        String DEFLATE = "deflate";

        String CLOSE = "close";
        String KEEP_ALIVE = "keep-alive";
    }
}
