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

package jlibs.core.graph.visitors;

import jlibs.core.graph.Filter;
import jlibs.core.graph.Navigator;
import jlibs.core.graph.Sequence;
import jlibs.core.graph.WalkerUtil;
import jlibs.core.graph.sequences.FilteredSequence;
import jlibs.core.graph.sequences.IterableSequence;

import java.util.Collection;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class ClassSorter{
    public static List<Class<?>> sort(final Sequence<Class<?>> classes){
        return WalkerUtil.topologicalSort(classes, new Navigator<Class<?>>(){
            @Override
            public Sequence<Class<?>> children(final Class<?> parent){
                return new FilteredSequence<Class<?>>(classes.copy(), new Filter<Class<?>>(){
                    @Override
                    public boolean select(Class<?> child){
                        return child!=parent && child.isAssignableFrom(parent);
                    }
                });
            }
        });
    }

    public static List<Class<?>> sort(Collection<Class<?>> classes){
        return sort(new IterableSequence<Class<?>>(classes));
    }
}
