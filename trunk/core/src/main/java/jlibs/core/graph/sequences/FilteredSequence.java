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
import jlibs.core.graph.Filter;

/**
 * @author Santhosh Kumar T
 */
public class FilteredSequence<E> extends AbstractSequence<E>{
    private Sequence<? extends E> delegate;
    private Filter<E> filter;

    public FilteredSequence(Sequence<? extends E> delegate, Filter<E> filter){
        this.delegate = delegate;
        this.filter = filter;
    }

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/
    
    @Override
    protected E findNext(){
        while(true){
            E elem = delegate.next();
            if(elem==null || filter.select(elem))
                return elem;
        }
    }

    /*-------------------------------------------------[ Reuse ]---------------------------------------------------*/
    
    @Override
    public void reset(){
        super.reset();
        _reset();
    }

    private void _reset(){
        delegate.reset();
    }

    @Override
    public Sequence<E> copy(){
        return new FilteredSequence<E>(delegate.copy(), filter);
    }
}
