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

package jlibs.core.graph.navigators;

import jlibs.core.graph.Sequence;
import jlibs.core.graph.sequences.EmptySequence;
import jlibs.core.graph.sequences.FilteredTreeSequence;
import jlibs.core.graph.Filter;
import jlibs.core.graph.Navigator;

/**
 * @author Santhosh Kumar T
 */
public class FilteredTreeNavigator<E> implements Navigator<E>{
    private Navigator<E> delegate;
    private Filter<E> filter;

    public FilteredTreeNavigator(Navigator<E> delegate){
        this(delegate, null);
    }

    public FilteredTreeNavigator(Navigator<E> delegate, Filter<E> filter){
        this.delegate = delegate;
        this.filter = filter;
    }

    @Override
    public Sequence<? extends E> children(E elem){
        Sequence<? extends E> seq = delegate.children(elem);
        if(seq==null)
            return EmptySequence.getInstance();
        if(filter!=null)
            seq = new FilteredTreeSequence<E>(seq, this, filter);
        return seq;
    }
}