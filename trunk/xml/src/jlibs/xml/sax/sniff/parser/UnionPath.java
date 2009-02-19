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
import jlibs.xml.sax.sniff.model.expr.Union;

/**
 * @author Santhosh Kumar T
 */
public class UnionPath extends Path{
    public Path lhs, rhs;

    public UnionPath(Node contextNode){
        super(contextNode);
    }

    public void setLHS(Path lhs){
        this.lhs = lhs;
    }

    public void setRHS(Path rhs){
        this.rhs = rhs;
    }

    public Expression create(Datatype expected){
        Expression lhsExpr = lhs.create(expected);
        Expression rhsExpr = rhs.create(expected);
        return new Union(contextNode, lhsExpr, rhsExpr);
    }

    @Override
    public Expression createFunction(String name){
        Expression lhsExpr = lhs.createFunction(name);
        Expression rhsExpr = rhs.createFunction(name);
        return new Union(contextNode, lhsExpr, rhsExpr);
    }
}
