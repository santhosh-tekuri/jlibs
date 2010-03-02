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

package jlibs.core.lang;

import java.io.CharArrayReader;

/**
 * This is {@link CharSequence} implementation.
 *
 * <ul>
 * <li>the char array passed to the constructor is not copied.</li>
 * <li>supports to change the char array using {@link #set(char[], int, int)}
 * <li>internal state can be queried using {@link #buffer()}, {@link #offset()} and {@link #length()}
 * </ul>
 *
 * @author Santhosh Kumar T
 */
public class CharArray implements CharSequence{
    private char buff[];
    private int offset;
    private int length;

    /**
     * Allocates a new {@code CharArray} so that it represents the sequence of
     * characters currently contained in the character array argument. The
     * contents of the character array are not copied;
     *
     * @param buff value of the chararray
     */
    public CharArray(char[] buff){
        this(buff, 0, buff.length);
    }

    /**
     * Allocates a new {@code CharArray} that contains characters from a subarray
     * of the character array argument. The {@code offset} argument is the
     * index of the first character of the subarray and the {@code length}
     * argument specifies the length of the subarray. The contents of the
     * subarray are not copied;
     *
     * @param  buff     Array that is the source of characters
     * @param  offset   The initial offset
     * @param  length   The length
     *
     * @throws  IndexOutOfBoundsException
     *          If the {@code offset} and {@code length} arguments index
     *          characters outside the bounds of the {@code buff} array
     */
    public CharArray(char[] buff, int offset, int length){
        set(buff, offset, length);
    }

    /**
     * replaces the internal char buffer with the given char array.
     *
     * @param buff      Array that is the source of characters
     * @param offset    The initial offset
     * @param length    The length
     */
    public void set(char[] buff, int offset, int length){
        if(offset<0)
            throw new IndexOutOfBoundsException("CharArray index out of range: "+offset);
        if(length<0)
            throw new IndexOutOfBoundsException("CharArray index out of range: "+length);
        if(offset>buff.length-length)
            throw new StringIndexOutOfBoundsException("CharArray index out of range: "+(offset+length));

        this.buff = buff;
        this.offset = offset;
        this.length = length;
    }

    /** returns the char buffer used by this instance */
    public char[] buffer(){
        return buff;
    }

    /** returns the index of first character in char buffer*/
    public int offset(){
        return offset;
    }

    @Override
    public int length(){
        return length;
    }

    public char[] toCharArray(boolean clone){
        if(!clone){
            if(offset==0 && buff.length==length)
                return buff;
        }
        char array[] = new char[length];
        System.arraycopy(buff, offset, array, 0, length);
        return array;
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
        return (start==0 && end==length) ? this : new CharArray(buff, this.offset+start, end-start);
    }

    /** creates a new {@link java.io.CharArrayReader} to read contents of this chararray */
    public CharArrayReader asReader(){
        return new CharArrayReader(buffer(), offset(), length());
    }

    @Override
    public String toString(){
        return new String(buff, offset, length);
    }
}
