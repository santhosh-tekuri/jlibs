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

import java.util.ArrayList;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Variable implements Expression{
    public ArrayList<Expression> children = new ArrayList<>();

    @Override
    public Object evaluate(Object root, Object current){
        current = root;
        if(current==null)
            return null;
        for(Expression child: children){
            current = child.evaluate(root, current);
            if(current==null)
                return null;
        }
        return current;
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        for(Expression child: children){
            if(child instanceof GetField){
                if(builder.length()>0)
                    builder.append('.');
            }
            if(child instanceof Lookup)
                builder.append('[');
            builder.append(child);
            if(child instanceof Lookup)
                builder.append(']');
        }
        return builder.toString();
    }
}
