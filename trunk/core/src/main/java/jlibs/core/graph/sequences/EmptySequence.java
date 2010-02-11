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

package jlibs.core.graph.sequences;

import jlibs.core.graph.Sequence;

/**
 * @author Santhosh Kumar T
 */
public final class EmptySequence<E> implements Sequence<E>{
    private static final EmptySequence INSTANCE = new EmptySequence();

    @SuppressWarnings("unchecked")
    public static <T> EmptySequence<T> getInstance(){
        return (EmptySequence<T>)INSTANCE;
    }

    private EmptySequence(){}

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/

    @Override
    public boolean hasNext(){
        return false;
    }

    @Override
    public E next(){
        return null;
    }

    @Override
    public E next(int count){
        return null;
    }

    /*-------------------------------------------------[ Query ]---------------------------------------------------*/

    @Override
    public int index(){
        return 0;
    }

    @Override
    public E current(){
        return null;
    }

    @Override
    public int length(){
        return 0;
    }

    /*-------------------------------------------------[ Reuse ]---------------------------------------------------*/
    
    @Override
    public void reset(){}

    @Override
    public EmptySequence<E> copy(){
        return this;
    }
}
