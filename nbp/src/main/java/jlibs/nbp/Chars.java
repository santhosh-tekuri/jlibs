/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package jlibs.nbp;

import java.nio.CharBuffer;
import java.util.Arrays;

import static java.lang.Character.*;

/**
 * @author Santhosh Kumar T
 */
public final class Chars implements CharSequence{
    private char buff[] = new char[100];
    public int count;

    private int stack[] = new int[50];
    int free = 0;

    public boolean isBuffering(){
        return free>0;
    }

    public void push(){
        if(free>=stack.length)
            stack = Arrays.copyOf(stack, stack.length<<1);
        stack[free++] = count;
    }

    public void expandCapacity(int increment){
        int newCapacity = (buff.length+increment)<<1;
        if(newCapacity<0)
            newCapacity = Integer.MAX_VALUE;
        buff = Arrays.copyOf(buff, newCapacity);
    }

    public void append(char character){
        if(count==buff.length)
            expandCapacity(1);
        buff[count++] = character;
    }

    public void append(char chars[], int offset, int len){
        if(count+len==chars.length)
            expandCapacity(len);
        System.arraycopy(chars, offset, buff, count, len);
        count += len;
    }

    public void append(int codePoint){
        if(codePoint<MIN_SUPPLEMENTARY_CODE_POINT){
            if(count==buff.length)
                expandCapacity(1);
            buff[count++] = (char)codePoint;
        }else{
            if(count==buff.length-1)
                expandCapacity(2);
            int offset = codePoint - MIN_SUPPLEMENTARY_CODE_POINT;
            buff[count++] = (char)((offset >>> 10) + MIN_HIGH_SURROGATE);
            buff[count++] = (char)((offset & 0x3ff) + MIN_LOW_SURROGATE);
        }
    }

    public Chars pop(int begin, int end){
        offset = begin + stack[--free];
        length = count-end;
        if(free==0)
            count = 0;
        return this;
    }

    public void clear(){
        free = count = 0;
    }
    
    /*-------------------------------------------------[ CharSequence ]---------------------------------------------------*/
    
    private int offset, length;
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
        return (start==0 && end==length) ? this : CharBuffer.wrap(buff, this.offset+start, end-start);
    }

    @Override
    public String toString(){
        return new String(buff, offset, length);
    }
}