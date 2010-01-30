/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.xml.sax.dog.path;

import jlibs.xml.sax.dog.expr.Expression;

/**
 * @author Santhosh Kumar T
 */
public class PositionalPredicate{
    public final Expression predicate;
    public final int position;
    public final int last;

    public PositionalPredicate next;

    public PositionalPredicate(Expression predicate, int position, int last){
        this.predicate = predicate;
        this.position = position;
        this.last = last;
    }
}
