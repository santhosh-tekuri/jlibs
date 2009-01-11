/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.xml.sax.sniff.model;

import java.io.CharArrayWriter;

/**
 * @author Santhosh Kumar T
 */
public class StringContent{
    private CharArrayWriter writer = new CharArrayWriter();

    public void write(char ch[], int start, int length){
        writer.write(ch, start, length);
    }

    public void reset(){
        writer.reset();
    }

    private String str;

    public void resetCache(){
        str = null;
    }

    public boolean isEmpty(){
        return writer.size()==0;
    }
    
    @Override
    public String toString(){
        if(str==null)
            return str = writer.size()>0 ? writer.toString() : null;
        return str;
    }
}
