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
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.expr.Literal;
import jlibs.xml.sax.dog.path.Constraint;
import jlibs.xml.sax.dog.path.LocationPath;

/**
 * @author Santhosh Kumar T
 */
public abstract class FirstEventData extends LocationExpression{
    public FirstEventData(LocationPath locationPath){
        super(locationPath.enlargedScope(), locationPath, DataType.STRING, false, true);
    }

    @Override
    public final Object getResult(){
        return "";
    }

    @Override
    public Expression simplify(){
        Expression expr = super.simplify();
        if(expr!=this)
            return expr;

        if(locationPath.steps.length>0){
            // for text() and comment(), namespace-uri(), local-name(), name() is empty
            int id = locationPath.steps[locationPath.steps.length-1].constraint.id;
            if(id==Constraint.ID_TEXT || id==Constraint.ID_COMMENT)
                return new Literal("", DataType.STRING);
        }
        return this;
    }
}