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

    private ByteBuffer writeBuffer;

    @Override
    protected int onWrite(ByteBuffer src) throws IOException{
        if(length==0)
            return 0;
        int toBeWritten = (int)Math.min(length, src.remaining());
        int limit = src.position()+toBeWritten;

        writeBuffer = src.duplicate();
        writeBuffer.limit(limit);

        src.position(limit);
        length -= toBeWritten;

        return toBeWritten;
    }

    @Override
    protected void doWritePending() throws IOException{
        if(writeBuffer!=null){
            delegate.write(writeBuffer);
            if(!writeBuffer.hasRemaining())
                writeBuffer = null;
        }
    }

    @Override
    protected Status selfStatus(){
        return writeBuffer==null ? Status.COMPLETED : Status.NEEDS_OUTPUT;
    }
}
