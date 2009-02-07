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

import jlibs.core.lang.ImpossibleException;
import jlibs.xml.sax.sniff.model.Datatype;
import jlibs.xml.sax.sniff.model.Node;

/**
 * @author Santhosh Kumar T
 */
public class Literal extends Expression{
    private Object literal;

    public Literal(Node contextNode, Object literal){
        super(contextNode, findReturnType(literal));
        this.literal = literal;
    }

    private static Datatype findReturnType(Object literal){
        if(literal instanceof String)
            return Datatype.STRING;
        else if(literal instanceof Number)
            return Datatype.NUMBER;
        else if(literal instanceof Boolean)
            return Datatype.BOOLEAN;
        else
            throw new ImpossibleException(literal.getClass().getName());
    }

    class MyEvaluation extends Evaluation{
        MyEvaluation(){
            setResult(literal);
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
        return literal.toString();
    }
}
