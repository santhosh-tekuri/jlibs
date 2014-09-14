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

package jlibs.nio.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

/**
 * @author Santhosh Kumar Tekuri
 */
public class BytesDecoder{
    public final Appendable appendable;
    private CharsetDecoder decoder;
    private CharBuffer chars;
    public BytesDecoder(Appendable appendable, CharsetDecoder decoder, int bufferSize){
        this.appendable = appendable;
        this.decoder = decoder;
        chars = CharBuffer.allocate(bufferSize);
    }

    public void write(ByteBuffer src, boolean eof) throws IOException{
        while(true){
            CoderResult cr = decoder.decode(src, chars, eof);
            if(cr.isUnderflow())
                break;
            else if(cr.isOverflow()){
                chars.flip();
                appendable.append(chars);
                chars.clear();
            }else
                cr.throwException();
        }
        if(eof){
            chars.flip();
            appendable.append(chars);
            chars.clear();
            decoder.reset();
        }
    }
}
