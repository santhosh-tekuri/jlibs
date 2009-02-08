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

package jlibs.xml.sax.sniff.model.expr.string;

import jlibs.xml.sax.sniff.model.Datatype;
import jlibs.xml.sax.sniff.model.Node;

/**
 * @author Santhosh Kumar T
 */
public class Substring extends Function{
    public Substring(Node contextNode){
        super(contextNode, Datatype.STRING, Datatype.STRING, Datatype.NUMBER, Datatype.NUMBER);
    }

    @Override
    protected Object evaluate(Object[] args){
        String str = (String)args[0];

        int start = round((Double)args[1])-1; // subtract 1 as Java strings are zero based

        int len = args.length==3 ? round((Double)args[2]) : str.length();
        if(len<0)
            return "";
        else if(len>str.length())
            len = str.length();

        if(start<0)
            start = 0;
        else if(start>len)
            return "";

        int end = start + len;
        if(end>str.length())
            end = str.length();

        return str.substring(start, end);
    }

    private int round(Double d){
        if(d.isNaN() || !d.isInfinite())
            return d.intValue();
        else
            return (int)Math.round(d);
    }
}
