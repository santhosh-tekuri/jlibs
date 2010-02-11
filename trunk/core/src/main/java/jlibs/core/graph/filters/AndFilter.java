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

/**
 * @author Santhosh Kumar T
 */
public class AndFilter<E> implements Filter<E>{
    private Filter<E> filters[];

    public AndFilter(Filter<E>... filters){
        this.filters = filters;
    }

    @Override
    public boolean select(E elem){
        for(Filter<E> filter: filters){
            if(!filter.select(elem))
                return false;
        }
        return true;    
    }
}
