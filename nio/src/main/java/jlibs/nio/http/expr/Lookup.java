/*
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

package jlibs.nio.http.expr;

import java.util.Map;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Lookup implements Expression{
    public final Expression child;
    public Lookup(Expression child){
        this.child = child;
    }

    @Override
    public Object evaluate(Object root, Object current){
        String name = TypeConversion.toString(child.evaluate(root, root));
        if(current instanceof Map)
            return ((Map)current).get(name);
        else
            return name==null ? null : ((ValueMap)current).getValue(name);
    }

    @Override
    public String toString(){
        return child.toString();
    }
}
