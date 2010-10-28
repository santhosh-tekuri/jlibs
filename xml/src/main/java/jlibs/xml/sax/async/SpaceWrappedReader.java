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

package jlibs.xml.sax.async;

import java.io.IOException;
import java.io.Reader;

/**
 * @author Santhosh Kumar T
 */
class SpaceWrappedReader extends Reader{
    private char[] data;
    private int index = -1;

    public SpaceWrappedReader(char[] data){
        this.data = data;
    }

    @Override
    public int read(char[] buf, int off, int len) throws IOException{
        if(index==data.length+1)
            return -1;
        
        int givenOff = off;
        if(index==-1){
            buf[off] = ' ';
            off++;
            len--;
            index++;
        }
        if(len>0 && index<data.length){
            int read = Math.min(data.length-index, len);
            System.arraycopy(data, index, buf, off, read);
            off += read;
            index += read;
            len -= read;
        }
        if(len>0 && index==data.length){
            buf[off] = ' ';
            off++;
            index++;
        }
        return off-givenOff;
    }

    @Override
    public void close() throws IOException{
        index = data.length+1;
    }
}
