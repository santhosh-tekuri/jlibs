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

import jlibs.xml.sax.sniff.model.Node;

/**
 * @author Santhosh Kumar T
 */
public class NotEquals extends Comparison{
    public NotEquals(Node contextNode){
        super(contextNode, "!=");
    }

    @Override
    protected boolean evaluateObjects(Object lhs, Object rhs){
        if(lhs instanceof Double){
            if(Double.isNaN((Double)lhs) || Double.isNaN((Double)rhs))
                return true;
        }
        return !lhs.equals(rhs);
    }
}