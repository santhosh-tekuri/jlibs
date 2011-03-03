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
public class FixedLengthOutputChannel extends FilterOutputChannel{
    private long length;

    public FixedLengthOutputChannel(OutputChannel delegate, long length){
        super(delegate);
        this.length = length;
    }

    @Override
    protected boolean activateInterest(){
        return super.activateInterest() || length>0;
    }

    @Override
    public int write(ByteBuffer dst) throws IOException{
        Status earlierStatus = status();
        if(length==0)
            throw new EOFException();
        int toBeRead = (int)Math.min(length, dst.remaining());
        int givenLimit = dst.limit();
        dst.limit(dst.position()+toBeRead);
        int wrote;
        try{
            wrote = delegate.write(dst);
        }finally{
            dst.limit(givenLimit);
        }
        if(wrote>0)
            length -= wrote;

        notifyCompleted(earlierStatus, status());
        return wrote;
    }

    @Override
    protected void doWritePending() throws IOException{}

    @Override
    protected Status selfStatus(){
        return Status.COMPLETED;
    }
}
