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

import jlibs.core.graph.Navigator;
import jlibs.core.graph.Sequence;
import jlibs.core.graph.Filter;
import jlibs.core.graph.sequences.FilteredSequence;

/**
 * @author Santhosh Kumar T
 */
public class FilteredNavigator<E> implements Navigator<E>{
    private Navigator<E> navigator;
    private Filter<E> filter;

    public FilteredNavigator(Navigator<E> navigator, Filter<E> filter){
        this.navigator = navigator;
        this.filter = filter;
    }

    @Override
    public Sequence<? extends E> children(E elem){
        return new FilteredSequence<E>(navigator.children(elem), filter);
    }
}
