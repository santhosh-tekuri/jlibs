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

package jlibs.core.util;

import java.util.Iterator;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class ResettableIterator<E> implements Iterator<E>{
    private List<E> list;
    private int cursor;

    public ResettableIterator(List<E> list){
        this.list = list;
    }
    
    @Override
    public boolean hasNext(){
        return cursor<list.size();
    }

    @Override
    public E next(){
        return list.get(cursor++);
    }

    @Override
    public void remove(){
        list.remove(cursor-1);
    }

    public ResettableIterator<E> reset(){
        cursor = 0;
        return this;
    }
    
    public ResettableIterator<E> reset(List<E> list){
        this.list = list;
        cursor = 0;
        return this;
    }
}
