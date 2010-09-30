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
public class Stream{
    private int chars[];
    private int begin = 0;
    private int end = 0;

    public Stream(int capacity){
        chars = new int[capacity+1];
    }

    private int capacity(){
        return chars.length-1;
    }

    public int length(){
        int len = end-begin;
        return len<0 ? len+chars.length : len;
    }

    public int charAt(int index){
        if(index<0 && index>=length())
            throw new IndexOutOfBoundsException("index: "+index+" length: "+length());
        return chars[(begin+index)%chars.length];
    }

    public void clear(){
        begin = end = lookAhead.end = 0;
    }

    public LookAhead lookAhead = new LookAhead();
    public class LookAhead{
        private int end;

        public int length(){
            int len = end-begin;
            return len<0 ? len+chars.length : len;
        }

        public int charAt(int index){
            if(index>=0 && index<length())
                return Stream.this.charAt(index);
            else
                throw new IndexOutOfBoundsException("index: "+index+" length: "+length());            
        }

        public void add(int ch){
            if(hasNext()){
                if(getNext()!=ch)
                    throw new IllegalArgumentException("expected char: "+ch);
                end = (end+1)%chars.length;
            }else{
                if(capacity()==Stream.this.length())
                    throw new RuntimeException("Stream is Full");
                chars[end] = ch;
                this.end = Stream.this.end = (end+1)%chars.length;
            }
        }

        public void consumed(){
            if(Stream.this.length()==0)
                throw new RuntimeException("nothing found to consume");
            
            if(length()==0)
                begin = end = (begin+1)%chars.length;
            else
                begin = (begin+1)%chars.length;
        }

        public void reset(){
            end = begin;
        }

        public boolean hasNext(){
            return this.end!=Stream.this.end;
        }

        public int getNext(){
            return Stream.this.charAt(length());
        }
    }
}
