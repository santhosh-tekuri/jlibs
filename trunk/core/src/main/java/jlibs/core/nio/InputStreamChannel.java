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

package jlibs.core.nio;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * @author Santhosh Kumar T
 */
public final class InputStreamChannel implements ReadableByteChannel{
    private InputStream is;
    public InputStreamChannel(InputStream is){
        this.is = is;
    }

    private boolean eof;
    public boolean isEOF(){
        return eof;
    }

    @Override
    public int read(ByteBuffer buffer) throws IOException{
        int pos = buffer.position();
        int read = is.read(buffer.array(), buffer.arrayOffset()+pos, buffer.remaining());
        if(read>0)
            buffer.position(pos+read);
        else if(read==-1)
            eof = true;
        return read;
    }

    @Override
    public boolean isOpen(){
        return is!=null;
    }

    @Override
    public void close() throws IOException{
        is.close();
        is = null;
    }
}
