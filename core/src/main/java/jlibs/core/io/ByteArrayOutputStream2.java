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

package jlibs.core.io;

import jlibs.core.lang.ByteSequence;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * This is an extension of {@link java.io.ByteArrayOutputStream}.
 * <p/>
 * You can get access to the internal byte buffer using
 * {@link #toByteSequence()}
 *
 * @author Santhosh Kumar T
 */
public class ByteArrayOutputStream2 extends ByteArrayOutputStream{
    public ByteArrayOutputStream2(){}

    public ByteArrayOutputStream2(int size){
        super(size);
    }

    public ByteArrayOutputStream2(InputStream is, int readBuffSize, boolean close) throws IOException{
        readFrom(is, readBuffSize, close);
    }

    /**
     * Returns the input data as {@link ByteSequence}.<br>
     * Note that the internal buffer is not copied.
     */
    public ByteSequence toByteSequence(){
        return new ByteSequence(buf, 0, size());
    }

    public int readFrom(InputStream is, int readBuffSize, boolean close) throws IOException{
        int oldSize = size();
        try{
            while(true){
                int bufAvailable = buf.length-size();
                int readAvailable = is.available();
                if(readAvailable==0)
                    readAvailable = Math.max(readBuffSize, bufAvailable);

                if(bufAvailable<readAvailable){
                    buf = Arrays.copyOf(buf, size()+readAvailable);
                    bufAvailable = readAvailable;
                }
                int read = is.read(buf, size(), bufAvailable);
                if(read==-1)
                    return size()-oldSize;
                else
                    count += read;
            }
        }finally{
            if(close)
                is.close();
        }
    }
}
