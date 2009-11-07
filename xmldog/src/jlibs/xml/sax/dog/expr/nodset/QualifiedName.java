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

package jlibs.xml.sax.dog.expr.nodset;

import jlibs.xml.sax.dog.DataType;
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.expr.Literal;
import jlibs.xml.sax.dog.path.Constraint;
import jlibs.xml.sax.dog.path.LocationPath;
import jlibs.xml.sax.dog.sniff.Event;

/**
 * @author Santhosh Kumar T
 */
public final class QualifiedName extends FirstEventData{
    public QualifiedName(LocationPath locationPath){
        super(locationPath);
    }

    @Override
    protected Object getResultItem(Event event){
        return event.qualifiedName();
    }

    @Override
    protected String getName(){
        return "name";
    }

    @Override
    public Expression simplify(){
        if(locationPath.steps.length>0){
            // for ::text(), name() is empty
            if(locationPath.steps[locationPath.steps.length-1].constraint.id==Constraint.ID_TEXT)
                return new Literal("", DataType.STRING);
        }
        return super.simplify();
    }
}