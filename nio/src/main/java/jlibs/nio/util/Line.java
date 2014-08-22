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

import jlibs.nio.async.LineOverflowException;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author Santhosh Kumar Tekuri
 */
public final class Line{
    private char chars[];
    private int count;

    public Line(){
        this(500);
    }

    public Line(int size){
        chars = new char[size];
    }

    public int length(){
        return count;
    }

    public char charAt(int i){
        if(i<0 || i>=count)
            throw new IndexOutOfBoundsException();
        return chars[i];
    }

    public char[] array(){
        return chars;
    }

    public void reset(){
        count = 0;
    }

    public String substring(int start, int end){
        if (start<0 || start>end || end>count)
            throw new IndexOutOfBoundsException();
        return new String(chars, start, end-start);
    }

    private int maxLen = -1;
    private int maxInitialLineLen = -1;
    private int maxLineLen = -1;
    private int lineCount = 0;
    private int bytesConsumed = 0;
    private int lineBytesConsumed = 0;
    public void reset(int maxLen, int maxInitialLineLen, int maxLineLen){
        count = 0;
        this.maxLen = maxLen;
        this.maxInitialLineLen = maxInitialLineLen;
        this.maxLineLen = maxLineLen;
        lineCount = bytesConsumed = lineBytesConsumed = 0;
    }

    public int lineCount(){
        return lineCount;
    }

    public boolean parse(ByteBuffer buffer, Consumer consumer){
        while(buffer.hasRemaining()){
            int pos = buffer.position();
            boolean lineCompleted = false;
            while(buffer.hasRemaining()){
                char ch = (char)buffer.get();
                if(ch=='\n'){
                    lineCompleted = true;
                    break;
                }else if(ch!='\r' && !Character.isISOControl((int)ch)){
                    if(count==chars.length){
                        if(count==Integer.MAX_VALUE)
                            throw new OutOfMemoryError();
                        int newCapacity = count<<2;
                        if(newCapacity<count)
                            newCapacity = Integer.MAX_VALUE;
                        chars = Arrays.copyOf(chars, newCapacity);
                    }
                    chars[count++] = ch;
                }
            }

            bytesConsumed += buffer.position()-pos;
            lineBytesConsumed += buffer.position()-pos;
            if(maxLen>0 && bytesConsumed>maxLen)
                throw new LineOverflowException(-1);
            if((lineCount==0 && maxInitialLineLen>=0 && lineBytesConsumed>maxInitialLineLen)
                    || (lineCount>0 && maxLineLen>=0 && lineBytesConsumed>maxLineLen))
                throw new LineOverflowException(lineCount);
            if(lineCompleted){
                ++lineCount;
                lineBytesConsumed = 0;
                if(consumer!=null)
                    consumer.consume(this);
                if(count==0)
                    return true;
                else
                    count = 0;
            }else
                return false;
        }
        return false;
    }

    @Override
    public String toString(){
        return new String(chars, 0, count);
    }

    public int indexOf(char ch, int from){
        while(from<count){
            if(chars[from]==ch)
                return from;
            ++from;
        }
        return -1;
    }

    public int indexOf(boolean whitespace, int from){
        char chars[] = this.chars;
        if(from>=0){
            int count = this.count;
            while(from<count){
                char ch = chars[from];
                if((ch==' '||ch=='\t')==whitespace)
                    return from;
                from++;
            }
        }else{
            from = -from;
            while(from>=0){
                char ch = chars[from];
                if((ch==' '||ch=='\t')==whitespace)
                    return from;
                from--;
            }
        }
        return -1;
    }

    public boolean equals(int start, int end, CharSequence seq){
        if(seq.length()!=end-start)
            return false;
        for(int i=0; start<end; ++i, ++start){
            if(chars[start]!=seq.charAt(i))
                return false;
        }
        return true;
    }

    public static int toUpperCase(char ascii){
        return ascii>='a' && ascii<='z' ? (ascii & 0xDF) : ascii;
    }

    public boolean equalsIgnoreCase(int start, int end, CharSequence seq){
        if(seq.length()!=end-start)
            return false;
        for(int i=0; start<end; ++i, ++start){
            if(toUpperCase(chars[start])!=toUpperCase(seq.charAt(i)))
                return false;
        }
        return true;
    }

    public int parseInt(int start, int end){
        if(end-start==0)
            throw new NumberFormatException("empty string");

        int i = start;
        boolean negative = false;
        if(chars[start]<'0'){
            if(chars[start]=='-')
                negative = true;
            else if(chars[start]!='+')
                throw new NumberFormatException("for input string \""+substring(start, end)+"\"");
            if(end-start==1)
                throw new NumberFormatException("cannot have lone + or -");
            ++i;
        }

        int result = 0;
        while(i<end){
            char ch = chars[i++];
            if(ch>='0' && ch<='9')
                result = result*10 + (ch-'0');
            else
                throw new NumberFormatException("for input string \""+substring(start, end)+"\"");
        }
        return negative ? -result : result;
    }

    public static interface Consumer{
        public void consume(Line line);
    }
}
