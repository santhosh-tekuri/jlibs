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

import java.util.Arrays;
import java.util.EmptyStackException;

/**
 * @author Santhosh Kumar T
 */
public class IntStack{
    private int data[];
    private int free = 0;

    public IntStack(int capacity){
        data = new int[capacity];
    }

    public IntStack(){
        this(50);
    }

    public int size(){
        return free;
    }

    public boolean isEmpty(){
        return free==0;
    }
    
    public void push(int i){
        if(free>=data.length)
            data = Arrays.copyOf(data, 2*data.length);
        data[free++] = i;
    }

    public int pop(){
        if(free==0)
            throw new EmptyStackException();
        return data[--free];
    }

    public int peek(){
        return peek(0);
    }
    
    public int peek(int index){
        if(free==0)
            throw new EmptyStackException();
        return data[free-1-index];
    }

    public void setPeek(int i){
        if(free==0)
            throw new EmptyStackException();
        data[free-1] = i;
    }

    public void clear(){
        free = 0;
    }
}
