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

/**
 * @author Santhosh Kumar T
 */
public class PredicateConvertor<E> implements Convertor<E, String>{
    protected Navigator2<E> navigator;
    protected Convertor<E, String> delegate;

    public PredicateConvertor(Navigator2<E> navigator, Convertor<E, String> delegate){
        this.navigator = navigator;
        this.delegate = delegate;
    }

    @Override
    public String convert(E source){
        String name = delegate.convert(source);
        E parent = navigator.parent(source);
        if(parent==null)
            return name;
        
        Sequence<? extends E> children = navigator.children(parent);
        int predicate = 1;
        while(children.hasNext()){
            E child = children.next();
            if(child.equals(source))
                break;
            if(name.equals(delegate.convert(child)))
                predicate++;
        }
        return name += "["+predicate+']';
    }
}
