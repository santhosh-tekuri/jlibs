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

import jlibs.core.graph.Path;

/**
 * @author Santhosh Kumar T
 */
public abstract class PathReflectionVisitor<E, R> extends ReflectionVisitor<E, R>{
    protected Path path;

    @Override
    @SuppressWarnings({"unchecked"})
    public final R visit(E elem){
        if(elem instanceof Path){
            path = (Path)elem;
            elem = (E)path.getElement();
        }
        try{
            return _visit(elem);
        }finally{
            path = null;
        }
    }

    protected R _visit(E elem){
        return super.visit(elem);
    }
}
