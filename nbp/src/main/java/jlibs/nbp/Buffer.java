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
public class Buffer{
    private StringBuilder buff = new StringBuilder();
    private final IntStack stack = new IntStack();

    public boolean isBufferring(){
        return stack.size()>0;
    }
    
    public void push(){
        stack.push(buff.length());
    }

    public void append(char ch){
        buff.append(ch);
    }

    public String pop(int begin, int end){
        String text = buff.substring(begin+ stack.pop(), buff.length()-end);
        if(stack.size()==0)
            buff.setLength(0);
        return text;
    }

    public void clear(){
        buff.setLength(0);
        stack.clear();
    }
}
