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

import jlibs.core.lang.CharArray;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

/**
 * This is an extension of {@link java.io.CharArrayWriter}.
 * <p/>
 * You can get access to the internal char buffer using
 * {@link #toCharSequence()}
 * 
 * @author Santhosh Kumar T
 */
public class CharArrayWriter2 extends CharArrayWriter{
    public CharArrayWriter2(){}

    public CharArrayWriter2(int initialSize){
        super(initialSize);
    }

    public CharArrayWriter2(Reader reader, int readBuffSize, boolean close) throws IOException{
        super(readBuffSize);
        readFrom(reader, readBuffSize, close);
    }

    /**
     * Returns the input data as {@link CharArray}.<br>
     * Note that the internal buffer is not copied.
     */
    public CharArray toCharSequence(){
        return new CharArray(buf, 0, size());
    }

    public int readFrom(Reader reader, int readBuffSize, boolean close) throws IOException{
        int oldSize = size();
        try{
            while(true){
                int bufAvailable = buf.length-size();
                if(bufAvailable<readBuffSize){
                    buf = Arrays.copyOf(buf, size() + readBuffSize);
                    bufAvailable = readBuffSize;
                }
                int read = reader.read(buf, size(), bufAvailable);
                if(read==-1)
                    return size()-oldSize;
                else
                    count += read;
            }
        }finally{
            if(close)
                reader.close();
        }
    }
}