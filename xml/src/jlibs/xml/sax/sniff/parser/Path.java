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

package jlibs.xml.sax.sniff.parser;

import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.Datatype;
import jlibs.xml.sax.sniff.model.expr.Expression;

/**
 * @author Santhosh Kumar T
 */
public abstract class Path{
    Node contextNode;

    protected Path(Node contextNode){
        this.contextNode = contextNode;
    }

    public abstract Expression createFunction(String name);
    public abstract Expression create(Datatype expected);
}
