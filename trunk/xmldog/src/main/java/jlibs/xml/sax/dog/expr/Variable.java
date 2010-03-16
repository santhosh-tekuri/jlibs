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

package jlibs.xml.sax.dog.expr;

import jlibs.core.lang.ImpossibleException;
import jlibs.xml.sax.dog.DataType;
import jlibs.xml.sax.dog.Scope;
import jlibs.xml.sax.dog.sniff.Event;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathVariableResolver;

/**
 * @author Santhosh Kumar T
 */
public final class Variable extends Expression{
    public final XPathVariableResolver variableResolver;
    public final QName qname;

    public Variable(XPathVariableResolver variableResolver, QName qname){
        super(Scope.DOCUMENT, DataType.PRIMITIVE);
        this.variableResolver = variableResolver;
        this.qname = qname;
    }

    @Override
    public Object getResult(){
        throw new ImpossibleException();
    }

    @Override
    public Object getResult(Event event){
        return variableResolver.resolveVariable(qname);
    }

    @Override
    public String toString(){
        return '$'+ qname.toString();
    }
}
