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
import jlibs.xml.sax.dog.path.Axis;
import jlibs.xml.sax.dog.path.Constraint;
import jlibs.xml.sax.dog.path.LocationPath;
import jlibs.xml.sax.dog.path.Step;
import jlibs.xml.sax.dog.path.tests.PITarget;
import jlibs.xml.sax.dog.sniff.Event;

/**
 * @author Santhosh Kumar T
 */
public final class NamespaceURI extends FirstEventData{
    public NamespaceURI(LocationPath locationPath){
        super(locationPath);
    }

    @Override
    protected Object getResultItem(Event event){
        return event.namespaceURI();
    }

    @Override
    protected String getName(){
        return "namespace-uri";
    }

    @Override
    public Expression simplify(){
        if(locationPath.steps.length>0){
            Step lastStep = locationPath.steps[locationPath.steps.length-1];
            // for ::text(), ::processing-instruction() and namespace::, namespace-uri() is empty
            int id = lastStep.constraint.id;
            if(id==Constraint.ID_TEXT || id==Constraint.ID_PI || lastStep.constraint instanceof PITarget
                    || lastStep.axis==Axis.NAMESPACE)
                return new Literal("", DataType.STRING);
        }
        return super.simplify();
    }
}