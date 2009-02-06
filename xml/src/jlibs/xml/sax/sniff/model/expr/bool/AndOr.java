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

import jlibs.core.lang.ImpossibleException;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.ResultType;
import jlibs.xml.sax.sniff.model.expr.Expression;

/**
 * @author Santhosh Kumar T
 */
public class AndOr extends Expression{
    private Boolean or;
    public AndOr(Node contextNode, boolean or){
        super(contextNode, ResultType.BOOLEAN, ResultType.BOOLEAN, ResultType.BOOLEAN);
        this.or = or;
    }

    class MyEvaluation extends Evaluation{
        int pending = 2;

        @Override
        public void finish(){
            throw new ImpossibleException();
        }

        @Override
        protected void consume(Object member, Object result){
            if(member==members.get(0))
                pending--;
            if(member==members.get(1))
                pending--;
            if(or.equals(result))
                setResult(result);
            else if(pending==0)
                setResult(!or);
        }

        @Override
        protected void print(){}
    }

    @Override
    protected Evaluation createEvaluation(){
        return new MyEvaluation();
    }
}