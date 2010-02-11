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

package jlibs.core.util;

import java.util.Comparator;

/**
 * @author Santhosh Kumar T
 */
public class ChainedComparator<T> implements Comparator<T>{
    private Comparator<T> delegates[];

    public ChainedComparator(Comparator<T>... delegates){
        this.delegates = delegates;
    }

    @Override
    public int compare(T o1, T o2){
        for(Comparator<T> delegate: delegates){
            int result = delegate.compare(o1, o2);
            if(result!=0)
                return result;
        }
        return 0;
    }
}
