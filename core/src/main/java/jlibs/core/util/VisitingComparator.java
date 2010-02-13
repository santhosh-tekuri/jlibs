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

import jlibs.core.graph.Visitor;

/**
 * @author Santhosh Kumar T
 */
public class VisitingComparator<T> extends DefaultComparator<T>{
    private Visitor<T, Comparable> visitor;

    public VisitingComparator(Visitor<T, Comparable> visitor){
        this.visitor = visitor;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    protected int _compare(T o1, T o2) {
        return visitor.visit(o1).compareTo(visitor.visit(o2));
    }
}
