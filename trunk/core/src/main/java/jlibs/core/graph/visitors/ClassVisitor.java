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

import jlibs.core.graph.Sequence;
import jlibs.core.graph.Visitor;
import jlibs.core.graph.sequences.IterableSequence;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class ClassVisitor<R> implements Visitor<Class<?>, R>{
    private Map<Class<?>, R> map = new HashMap<Class<?>,R>();
    private Sequence<Class<?>> topologicalSequence;
    
    public void map(Class<?> clazz, R returnValue){
        topologicalSequence = null;
        map.put(clazz, returnValue);
    }

    @Override
    public R visit(Class<?> elem){
        if(topologicalSequence==null)
            topologicalSequence = new IterableSequence<Class<?>>(ClassSorter.sort(map.keySet()));

        topologicalSequence.reset();
        for(Class<?> clazz; (clazz=topologicalSequence.next())!=null;){
            if(clazz.isAssignableFrom(elem))
                return map.get(clazz);
        }
        return null;
    }
}
