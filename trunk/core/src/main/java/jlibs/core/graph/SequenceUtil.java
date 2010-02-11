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

package jlibs.core.graph;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class SequenceUtil{
    public static <E> int indexOf(Sequence<? extends E> seq, E elem){
        if(elem==null)
            return -1;

        for(E item; (item=seq.next())!=null;){
            if(elem.equals(item))
                break;
        }
        
        return seq.index();
    }

    public static <E, C extends Collection<E>> C addAll(C collection, Sequence<? extends E> seq){
        for(E elem; (elem=seq.next())!=null;)
            collection.add(elem);
        return collection;
    }

    @SuppressWarnings("unchecked")
    public static <C, E> C[] toArray(Class<?> clazz, Sequence<E> seq){
        List<E> list = addAll(new LinkedList<E>(), seq);
        return list.toArray((C[])Array.newInstance(clazz, list.size()));
    }
}
