/**
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

package jlibs.core.nio.channels;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Santhosh Kumar T
 */
public class FixedLengthInputChannel extends FilterInputChannel{
    private long length;

    public FixedLengthInputChannel(InputChannel delegate, long length){
        super(delegate);
        this.length = length;
    }

    @Override
    protected boolean activateInterest(){
        return super.activateInterest() && length>0;
    }

    @Override
    protected int doRead(ByteBuffer dst) throws IOException{
        if(length==0)
            return -1;
        int toBeRead = (int)Math.min(length, dst.remaining());
        int givenLimit = dst.limit();
        dst.limit(dst.position()+toBeRead);
        int read;
        try{
            read = delegate.read(dst);
        }finally {
            dst.limit(givenLimit);
        }
        if(read==-1)
            throw new EOFException();
        if(read>0)
            length -= read;
        return read;
    }

    @Override
    public long pending(){
        return super.pending() + length;
    }
}
