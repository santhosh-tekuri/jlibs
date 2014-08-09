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

package jlibs.nio.channels.filters;

import jlibs.nio.channels.impl.filters.AbstractInputFilterChannel;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Santhosh Kumar Tekuri
 */
public class FixedLengthInputFilter extends AbstractInputFilterChannel{
    private long length;
    public FixedLengthInputFilter(long length){
        if(length<0)
            throw new IllegalArgumentException("negative length: "+length);
        this.length = length;
    }

    public long available(){
        return length;
    }

    @Override
    protected int _read(ByteBuffer dst) throws IOException{
        if(length==0)
            return -1;

        int toBeRead = (int)Math.min(length, dst.remaining());
        int userLimit = dst.limit();
        dst.limit(dst.position()+toBeRead);
        int read;
        try{
            read = peerInput.read(dst);
        }finally {
            dst.limit(userLimit);
        }

        if(read==-1)
            throw new EOFException(length+" more bytes expected");
        length -= read;
        return read;
    }

    @Override
    protected boolean isReadReady(){
        return length==0;
    }
}
