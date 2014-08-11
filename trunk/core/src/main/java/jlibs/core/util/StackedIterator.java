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

package jlibs.core.util;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author Santhosh Kumar Tekuri
 */
public class StackedIterator<T> implements Iterator<T>{
    private LinkedList<Iterator<T>> list = new LinkedList<Iterator<T>>();

    public StackedIterator(Iterator<T> delegate){
        list.addLast(delegate);
    }

    private Iterator<T> current(){
        while(!list.isEmpty()){
            Iterator<T> current = list.getLast();
            if(current.hasNext())
                return current;
            list.removeLast();
        }
        return EmptyIterator.instance();
    }

    @Override
    public boolean hasNext(){
        return current().hasNext();
    }

    private Iterator<T> current;
    @Override
    public T next(){
        current = current();
        return current.next();
    }

    @Override
    public void remove(){
        current.remove();
    }

    public void push(Iterator<T> iter){
        list.addLast(iter);
    }

    public Iterator<T> pop(){
        return list.isEmpty() ? null : list.removeLast();
    }
}
