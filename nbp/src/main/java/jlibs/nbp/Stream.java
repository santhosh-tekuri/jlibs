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
public final class Stream{
    final int chars[];
    int begin = 0;
    int end = 0;

    public Stream(int capacity){
        chars = new int[capacity+1];
    }

    int capacity(){
        return chars.length-1;
    }

    public int length(){
        int len = end-begin;
        return len<0 ? len+chars.length : len;
    }

    public int charAt(int index){
        assert index>=0 && index<length();
        return chars[(begin+index)%chars.length];
    }

    public void clear(){
        begin = end = lookAhead.end = 0;
    }

    String toString(int length){
        StringBuilder buff = new StringBuilder();
        for(int i=0; i<length; i++){
            int data = charAt(i);
            if(data==-1)
                buff.append("<EOF>");
            else
                buff.appendCodePoint(data);
        }
        return buff.toString();
    }

    @Override
    public String toString(){
        return toString(length());
    }

    public final LookAhead lookAhead = new LookAhead();
    public final class LookAhead{
        int end;

        public int length(){
            int len = end-begin;
            return len<0 ? len+chars.length : len;
        }

        public boolean isEmpty(){
            return end==begin;
        }

        public int charAt(int index){
            assert index>=0 && index<length();
            return chars[(begin+index)%chars.length];
        }

        // returns true if this was a fresh data added
        public boolean add(int ch){
            if(this.end!=Stream.this.end){ // hasNext()
                assert getNext()==ch : "expected char: "+ch;
                end = (end+1)%chars.length;
                return false;
            }else{
                assert capacity()>Stream.this.length() : "Stream is Full";
                chars[end] = ch;
                this.end = Stream.this.end = (end+1)%chars.length;
                return true;
            }
        }

        public void consumed(){
            assert Stream.this.length()>0 : "nothing found to consume";
            
            if(begin==end) // length()==0
                begin = end = (begin+1)%chars.length;
            else
                begin = (begin+1)%chars.length;
        }

        public void reset(){
            end = begin;
        }

        public int getNext(){
            return this.end==Stream.this.end ? -2 : chars[end];
        }

        @Override
        public String toString(){
            return Stream.this.toString(length());
        }
    }
}
