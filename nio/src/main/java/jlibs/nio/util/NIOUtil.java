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
import java.net.ConnectException;
import java.net.PortUnreachableException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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
        int srcRemaining = src.remaining();
        int dstRemaining = dst.remaining();
        if(srcRemaining<=dstRemaining){
            dst.put(src);
            return srcRemaining;
        }
        int srcLimit = src.limit();
        src.limit(src.position()+dstRemaining);
        dst.put(src);
        src.limit(srcLimit);
        return dstRemaining;
    }

    public static int copy(ByteBuffer src, ByteBuffer dsts[], int offset, int length){
        int read = 0;
        while(src.hasRemaining() && length>0){
            read += copy(src, dsts[offset]);
            ++offset;
            --length;
        }
        return read;
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

    public static int transfer(ByteBuffer src, FileChannel target, long position, long count) throws IOException{
        int wrote;
        if(src.remaining()<=count){
            wrote = target.write(src, position);
        }else{
            int srcLimit = src.limit();
            src.limit(src.position()+(int)count);
            try{
                wrote = target.write(src, position);
            }finally{
                src.limit(srcLimit);
            }
        }
        return wrote;
    }
}
