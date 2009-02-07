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
import jlibs.xml.sax.sniff.model.Notifier;

/**
 * @author Santhosh Kumar T
 */
public class TypeCast extends Expression{
    public TypeCast(Node contextNode, Datatype returnType){
        super(contextNode, assertType(returnType), Datatype.STRING);
    }

    private static Datatype assertType(Datatype datatype){
        switch(datatype){
            case STRING:
            case BOOLEAN:
            case NUMBER:
                return datatype;
            default:
                throw new IllegalArgumentException();
        }
    }
    
    @Override
    public void addMember(Notifier member){
        assertType(member.resultType());
        _addMember(member);
    }

    class MyEvaluation extends Evaluation{
        @Override
        public void finish(){
            throw new ImpossibleException();
        }

        @Override
        protected void consume(Object member, Object result){
            setResult(resultType().convert(result));
        }

        @Override
        protected void print(){}
    }

    @Override
    protected Evaluation createEvaluation(){
        return new MyEvaluation();
    }
}