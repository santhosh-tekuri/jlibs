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

package jlibs.nio.http.msg;

import jlibs.core.lang.ImpossibleException;
import jlibs.nio.http.util.USAscii;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.TreeMap;

/**
 * @author Santhosh Kumar Tekuri
 */
public class AsciiString{
    public final String text;
    private final byte bytes[];
    private final int hashCode;
    public AsciiString(CharSequence text){
        this(text, USAscii.caseInsensitiveHashCode(text));
    }

    AsciiString(CharSequence text, int hashCode){
        Objects.requireNonNull(text, "text==null");
        this.text = text.toString();
        this.hashCode = hashCode;
        bytes = USAscii.toBytes(this.text);
    }

    public void putInto(ByteBuffer buffer){
        buffer.put(bytes, 0, bytes.length);
    }

    public int putInto(ByteBuffer buffer, int offset){
        int min = Math.min(bytes.length-offset, buffer.remaining());
        if(min>0)
            buffer.put(bytes, offset, min);
        return offset + min;
    }

    @Override
    public int hashCode(){
        return hashCode;
    }

    @Override
    public boolean equals(Object obj){
        if(obj==this)
            return true;
        if(obj instanceof AsciiString){
            AsciiString that = (AsciiString)obj;
            if(this.internID!=0 && that.internID!=0)
                return this.internID==that.internID;
            if(this.hashCode!=that.hashCode || this.bytes.length!=that.bytes.length)
                return false;
            int len = bytes.length;
            for(int i=0; i<len; i++){
                int ch1 = bytes[i];
                if(ch1>='a' && ch1<='z')
                    ch1 = ch1 & 0xDF;

                int ch2 = that.bytes[i];
                if(ch2>='a' && ch2<='z')
                    ch2 = ch2 & 0xDF;

                if(ch1!=ch2)
                    return false;
            }
            return true;
        }else
            return false;
    }

    public boolean equals(CharSequence seq){
        if(seq==null || bytes.length!=seq.length())
            return false;
        int len = bytes.length;
        for(int i=0; i<len; i++){
            int ch1 = bytes[i];
            if(ch1>='a' && ch1<='z')
                ch1 = ch1 & 0xDF;

            int ch2 = seq.charAt(i);
            if(ch2>='a' && ch2<='z')
                ch2 = ch2 & 0xDF;

            if(ch1!=ch2)
                return false;
        }
        return true;
    }

    @Override
    public String toString(){
        return text;
    }

    private int internID;
    private static final Headers INTERNED = new Headers();
    protected static void initInterned(){
        if(INTERNED.getFirst()!=null)
            return;
        TreeMap<String, AsciiString> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        try{
            Class classes[] = { Request.class, Response.class };
            for(Class clazz: classes){
                for(Field field: clazz.getFields()){
                    if(field.getType()==AsciiString.class){
                        AsciiString string = (AsciiString)field.get(null);
                        INTERNED.add(string, "");
                        map.put(string.text, string);
                    }
                }
            }
        }catch(Exception ex){
            throw new ImpossibleException(ex);
        }

        int id = 0;
        for(AsciiString string: map.values())
            string.internID = ++id;
    }

    public static AsciiString valueOf(CharSequence seq){
        int hashCode = USAscii.caseInsensitiveHashCode(seq);
        Header header = INTERNED.entry(seq, hashCode, false);
        if(header!=null)
            return header.getName();
        return new AsciiString(seq, hashCode);
    }
}
