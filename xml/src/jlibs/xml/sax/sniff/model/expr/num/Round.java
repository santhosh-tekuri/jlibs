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

package jlibs.xml.sax.sniff.model.expr.num;

import jlibs.xml.sax.sniff.model.Datatype;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.expr.Function;

/**
 * @author Santhosh Kumar T
 */
public class Round extends Function{
    protected Round(Node contextNode){
        super(contextNode, Datatype.NUMBER, Datatype.NUMBER);
    }

    @Override
    protected Object evaluate(Object[] args){
        return evaluate((Double)args[0]);
    }

    public static double evaluate(double d){
        if(Double.isNaN(d) || Double.isInfinite(d))
            return d;

        return (double)Math.round(d);
    }
}
