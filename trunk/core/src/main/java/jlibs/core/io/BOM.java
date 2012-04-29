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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 *
 * 
 * @author Santhosh Kumar T
 */
public enum BOM{
    UTF32_LE("UTF-32LE"),
    UTF32_BE("UTF-32BE"),
    UTF16_LE("UTF-16LE"),
    UTF16_BE("UTF-16BE"),
        UTF8("UTF-8");

    private String encoding;
    private int expected[];

    BOM(String encoding){
        this.encoding = encoding;

        byte byteBom[] = "\uFEFF".getBytes(Charset.forName(encoding));
        expected = new int[byteBom.length];
        for(int i=0; i<byteBom.length; i++)
            expected[i] = byteBom[i]&0xFF;
    }

    public String encoding(){
        return encoding;
    }

    public int[] expected(){
        return expected;
    }

    private boolean matches(ByteBuffer buffer){
        int pos = buffer.position();
        for(int i: expected){
            if(i!=(buffer.get()&0xFF)){
                buffer.position(pos);
                return false;
            }
        }
        return true;
    }

    public static BOM detect(ByteBuffer buffer){
        int available = buffer.remaining();
        for(BOM bom: values()){
            if(available>=bom.expected.length){
                if(bom.matches(buffer))
                    return bom;
            }else
                return null;
        }
        return null;
    }
}
