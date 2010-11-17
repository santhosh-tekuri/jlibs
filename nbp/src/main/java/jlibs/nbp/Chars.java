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

/**
 * @author Santhosh Kumar T
 */
public final class Chars implements CharSequence{
    private char buff[];
    private int offset;
    private int length;

    public Chars(char[] buff, int offset, int length){
        set(buff, offset, length);
    }

    void set(char[] buff, int offset, int length){
        this.buff = buff;
        this.offset = offset;
        this.length = length;
    }

    public char[] array(){
        return buff;
    }

    public int offset(){
        return offset;
    }

    public int length(){
        return length;
    }

    @Override
    public char charAt(int index){
        if(index<0 || index>=length)
            throw new IndexOutOfBoundsException(index+" is not in range [0, "+length+")");
        else
            return buff[offset+index];
    }

    @Override
    public CharSequence subSequence(int start, int end){
        if(start<0)
            throw new IndexOutOfBoundsException("CharArray index out of range: "+start);
        if(end>length)
            throw new IndexOutOfBoundsException("CharArray index out of range: "+end);
        if(start>end)
            throw new IndexOutOfBoundsException("CharArray index out of range: "+(end-start));
        return (start==0 && end==length) ? this : new Chars(buff, this.offset+start, end-start);
    }

    @Override
    public String toString(){
        return new String(buff, offset, length);
    }
}