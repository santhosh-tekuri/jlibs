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

package jlibs.xml.sax.sniff.model.expr;

import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.Datatype;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import java.util.Arrays;

/**
 * @author Santhosh Kumar T
 */
public class UserFunction extends Function{
    private QName functionName;

    public UserFunction(Node contextNode, QName functionName){
        super(contextNode, Datatype.PRIMITIVE);
        this.functionName = functionName;
    }

    @Override
    public Datatype memberType(int index){
        return Datatype.PRIMITIVE;
    }

    @Override
    protected Object evaluate(Object[] args){
        XPathFunction function = evaluationStartNode.root.functionResolver.resolveFunction(functionName, members.size());
        if(function==null)
            throw new RuntimeException("Function '"+functionName+"' is resolved to null");
        try{
            return function.evaluate(Arrays.asList(args));
        }catch(XPathFunctionException ex){
            throw new RuntimeException(ex);
        }
    }
}
