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

package jlibs.xml.sax.dog.expr.nodset;

import jlibs.xml.sax.dog.DataType;
import jlibs.xml.sax.dog.Scope;
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.expr.Literal;
import jlibs.xml.sax.dog.path.LocationPath;
import jlibs.xml.sax.dog.sniff.Event;

/**
 * @author Santhosh Kumar T
 */
public final class Bool extends LocationExpression{
    public Bool(LocationPath locationPath){
        super(locationPath.scope, locationPath, DataType.BOOLEAN, false, false);
    }

    @Override
    public Object getResult(){
        return locationPath==LocationPath.IMPOSSIBLE ? Boolean.FALSE : Boolean.TRUE;
    }

    @Override
    protected Object getResultItem(Event event){
        return Boolean.TRUE;
    }

    @Override
    protected String getName(){
        return "boolean";
    }

    @Override
    public Expression simplify(){
        if(locationPath.scope==Scope.LOCAL && locationPath.steps.length==0)
            return new Literal(Boolean.TRUE, DataType.BOOLEAN);
        else
            return super.simplify();
    }
}