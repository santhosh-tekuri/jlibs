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

package jlibs.nio.util;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.PortUnreachableException;
import java.nio.ByteBuffer;
import java.nio.channels.UnresolvedAddressException;

/**
 * @author Santhosh Kumar Tekuri
 */
public class NIOUtil{
    public static boolean isConnectionFailure(Throwable thr){
        return thr instanceof UnresolvedAddressException
                || thr instanceof ConnectException
                || thr instanceof PortUnreachableException;
    }

    public static int copy(ByteBuffer src, ByteBuffer dst){
        int min = Math.min(src.remaining(), dst.remaining());
        int limit = src.limit();
        src.limit(src.position()+min);
        dst.put(src);
        src.limit(limit);
        return min;
    }

    public static void compact(ByteBuffer buffer){
        if(buffer.hasRemaining()){
            if(buffer.position()==0){
                buffer.position(buffer.remaining());
                buffer.limit(buffer.capacity());
            }else
                buffer.compact();
        }else
            buffer.clear();
    }

    public static void writeAscii(String value, OutputStream out) throws IOException{
        if(out instanceof Bytes.OutputStream)
            ((Bytes.OutputStream)out).write(value);
        else{
            byte bytes[] = new byte[value.length()];
            int len = value.length();
            for(int i=0; i<len; i++)
                bytes[i] = (byte)value.charAt(i);
            out.write(bytes);
        }
    }
}
