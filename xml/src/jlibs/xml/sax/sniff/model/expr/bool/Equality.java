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

package jlibs.xml.sax.sniff.model.expr.bool;

import jlibs.xml.sax.sniff.model.Datatype;
import jlibs.xml.sax.sniff.model.Node;

/**
 * @author Santhosh Kumar T
 */
public abstract class Equality extends Comparison{
    public Equality(Node contextNode, String name){
        super(contextNode, name);
    }

    @Override
    protected boolean evaluateObjectObject( Object lhs, Object rhs){
        if(lhs instanceof Boolean || rhs instanceof Boolean){
            boolean b1 = Datatype.asBoolean(lhs);
            boolean b2 = Datatype.asBoolean(rhs);
            return evaluateObjects(b1, b2);
        }else if(lhs instanceof Double || rhs instanceof Double){
            double d1 = Datatype.asNumber(lhs);
            double d2 = Datatype.asNumber(rhs);
            return evaluateObjects(d1, d2);
        }else{
            String s1 = Datatype.asString(lhs);
            String s2 = Datatype.asString(rhs);
            return evaluateObjects(s1, s2);
        }
    }

    protected abstract boolean evaluateObjects(Object lhs, Object rhs);
}
