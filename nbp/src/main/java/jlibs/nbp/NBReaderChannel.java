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

package jlibs.nbp;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

/**
 * @author Santhosh Kumar T
 */
public class NBReaderChannel implements ReadableCharChannel{
    private Reader reader;

    public NBReaderChannel(Reader reader){
        this.reader = reader;
    }

    @Override
    public int read(CharBuffer buffer) throws IOException{
        int pos = buffer.position();
        int read = reader.read(buffer.array(), buffer.arrayOffset()+pos, buffer.remaining());
        if(read!=-1)
            buffer.position(pos+read);
        return read;
    }

    @Override
    public boolean isOpen(){
        return reader!=null;
    }

    @Override
    public void close() throws IOException{
        reader.close();
        reader = null;
    }
}
