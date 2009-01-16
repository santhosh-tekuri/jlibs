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

package jlibs.xml.sax.sniff.events;

/**
 * @author Santhosh Kumar T
 */
public class Comment extends Event{
    @Override
    public int type(){
        return COMMENT;
    }

    public char[] ch;
    public int start;
    public int length;

    public void setData(char[] ch, int start, int length){
        this.ch = ch;
        this.start = start;
        this.length = length;
        setResultWrapper(this);
    }

    @Override
    public String toString(){
        return new String(ch, start, length);
    }
}
