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

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Santhosh Kumar T
 */
public class IterableSequence<E> extends AbstractSequence<E>{
    private Iterable<E> iterable;

    public IterableSequence(Iterable<E> iterable){
        this.iterable = iterable;
        _reset();
    }

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/

    private Iterator<E> iter;

    @Override
    protected E findNext(){
        return iter.hasNext() ? iter.next() : null;
    }

    /*-------------------------------------------------[ Reuse ]---------------------------------------------------*/

    @Override
    public void reset(){
        super.reset();
        _reset();
    }

    private void _reset(){
        iter = iterable.iterator();
    }

    @Override
    public IterableSequence<E> copy(){
        return new IterableSequence<E>(iterable);
    }

    /*-------------------------------------------------[ Optimization ]---------------------------------------------------*/

    @Override
    public int length(){
        if(iterable instanceof Collection)
            return ((Collection<?>) iterable).size();
        else
            return super.length();
    }
}
