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
import java.util.NoSuchElementException;

/**
 * Abstract Implementation of Iterator<T>. Subclass need to override
 * only <code>computeNext()</code> method.
 *
 * @author Santhosh Kumar T
 */
public abstract class AbstractIterator<T> implements Iterator<T>{
    private T next;
    private boolean eof;

    protected abstract T computeNext();

    protected AbstractIterator<T> reset(){
        next = null;
        eof = false;
        return this;
    }

    @Override
    public final boolean hasNext(){
        if(!eof && next==null){
            next = computeNext();
            if(next==null)
                eof = true;
        }
        return !eof;
    }

    @Override
    public final T next(){
        if(hasNext()){
            T current = next;
            next = null;
            return current;
        }else
            throw new NoSuchElementException();
    }

    @Override
    public void remove(){
        throw new UnsupportedOperationException();
    }
}
