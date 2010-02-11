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

/**
 * @author Santhosh Kumar T
 */
public class DuplicateSequence<E> extends AbstractSequence<E>{
    private E elem;
    private int count;

    public DuplicateSequence(E elem){
        this(elem, 1);
    }

    public DuplicateSequence(E elem, int count){
        //noinspection ConstantConditions
        if(elem==null)
            throw new IllegalArgumentException("elem can't be null");
        if(count<0)
            throw new IllegalArgumentException(String.format("can't duplicate %d times", count));
        
        this.elem = elem;
        this.count = count;
        _reset();
    }

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/

    private int pos;

    @Override
    protected E findNext(){
        pos++;
        return pos<=count ? elem : null;
    }

    /*-------------------------------------------------[ Reuse ]---------------------------------------------------*/
    
    @Override
    public void reset(){
        super.reset();
        _reset();
    }

    private void _reset(){
        pos = 0;
    }

    @Override
    public DuplicateSequence<E> copy(){
        return new DuplicateSequence<E>(elem, count);
    }

    /*-------------------------------------------------[ Optimization ]---------------------------------------------------*/

    @Override
    public int length(){
        return count;
    }
}
