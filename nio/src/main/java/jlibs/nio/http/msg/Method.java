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
import jlibs.nio.http.util.USAscii;
import jlibs.nio.util.Buffers;

import java.nio.ByteBuffer;

import static jlibs.nio.http.util.USAscii.SP;

/**
 * @author Santhosh Kumar Tekuri
 */
public final class Method{
    public final String name;
    public final boolean requestPayloadAllowed;
    public final boolean responsePayloadAllowed;

    private final byte bytes[];

    private Method(String name, boolean requestPayloadAllowed, boolean responsePayloadAllowed, boolean createBytes){
        this.name = name;
        this.requestPayloadAllowed = requestPayloadAllowed;
        this.responsePayloadAllowed = responsePayloadAllowed;
        if(createBytes)
            bytes = (name+' ').getBytes(IOUtil.US_ASCII);
        else
            bytes = null;
    }

    public void putInto(ByteBuffer buffer){
        if(bytes==null){
            USAscii.append(buffer, name);
            buffer.put(SP);
        }else
            buffer.put(bytes);
    }

    public void writeTo(Buffers buffers){
        if(bytes==null){
            buffers.write(name);
            buffers.write(SP);
        }else
            buffers.write(bytes);
    }

    @Override
    public int hashCode(){
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj){
        return obj==this || (obj instanceof Method && this.name.equalsIgnoreCase(((Method)obj).name));
    }

    @Override
    public String toString(){
        return name;
    }

    public static final Method GET     = new Method("GET", false, true, true);
    public static final Method POST    = new Method("POST", true, true, true);
    public static final Method DELETE  = new Method("DELETE", true, true, true);
    public static final Method HEAD    = new Method("HEAD", false, false, true);
    public static final Method PUT     = new Method("PUT", true, true, true);
    public static final Method CONNECT = new Method("CONNECT", false, true, true);
    public static final Method PATCH   = new Method("PATCH", true, true, true);
    public static final Method TRACE   = new Method("TRACE", false, true, true);
    public static final Method OPTIONS = new Method("OPTIONS", true, true, true);

    private static final Method methods[] = { GET, POST, DELETE, HEAD, PUT, CONNECT, PATCH, TRACE, OPTIONS };
    public static Method valueOf(CharSequence seq){
        for(Method method: methods){
            if(method.name.length()==seq.length()){
                if(USAscii.equalIgnoreCase(method.name, seq))
                    return method;
            }
        }
        return new Method(seq.toString(), true, true, false);
    }
}
