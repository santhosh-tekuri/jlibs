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

    /**
     * Returns the input data as {@link CharArray}.<br>
     * Note that the internal buffer is not copied.
     */
    public CharArray toCharSequence(){
        return new CharArray(buf, 0, size());
    }
}