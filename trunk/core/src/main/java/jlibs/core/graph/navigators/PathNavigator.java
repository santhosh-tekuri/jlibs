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
import jlibs.core.graph.sequences.PathSequence;
import jlibs.core.graph.Navigator;
import jlibs.core.graph.Path;

/**
 * @author Santhosh Kumar T
 */
public class PathNavigator<E> implements Navigator<Path>{
    private Navigator<E> delegate;

    public PathNavigator(Navigator<E> delegate){
        this.delegate = delegate;
    }

    @Override
    public Sequence<Path> children(Path path){
        @SuppressWarnings({"unchecked"})
        Sequence<? extends E> seq = delegate.children((E)path.getElement());
        return new PathSequence<E>(path, seq);
    }
}
