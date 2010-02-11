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

package jlibs.core.graph.filters;

import jlibs.core.graph.Filter;
import jlibs.core.graph.Path;

/**
 * @author Santhosh Kumar T
 */
public class PathFilter<E> implements Filter<E>{
    private Path path;
    private Filter<Path> delegate;

    public PathFilter(Path path, Filter<Path> delegate){
        this.path = path;
        this.delegate = delegate;
    }

    @Override
    public boolean select(E elem){
        return delegate.select(path.append(elem));
    }
}
