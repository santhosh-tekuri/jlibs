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

import jlibs.xml.sax.sniff.model.Datatype;
import jlibs.xml.sax.sniff.model.Node;

import javax.xml.namespace.QName;

/**
 * @author Santhosh Kumar T
 */
public class VariableReference extends Expression{
    private QName variableName;

    public VariableReference(Node contextNode, QName variableName){
        super(contextNode, Datatype.PRIMITIVE);
        this.variableName = variableName;
    }

    class MyEvaluation extends Evaluation{
        MyEvaluation(){
            Object value = evaluationStartNode.root.variableResolver.resolveVariable(variableName);
            if(value==null)
                throw new RuntimeException("Variable '"+variableName+"' is resolved to null");
            setResult(value);
        }

        @Override
        public void finish(){}

        @Override
        protected void consume(Object member, Object result){}

        @Override
        protected void print(){}
    }

    @Override
    protected Evaluation createEvaluation(){
        return new MyEvaluation();
    }

    @Override
    public String getName(){
        return '$'+variableName.toString();
    }
}