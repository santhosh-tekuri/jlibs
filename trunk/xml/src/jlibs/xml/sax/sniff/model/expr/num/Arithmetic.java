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

import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.ResultType;
import jlibs.xml.sax.sniff.model.expr.Expression;
import org.jaxen.saxpath.Operator;

/**
 * @author Santhosh Kumar T
 */
public class Arithmetic extends Expression{
    private int operator;

    public Arithmetic(Node contextNode, int operator){
        super(contextNode, ResultType.NUMBER, ResultType.NUMBER, ResultType.NUMBER);
        this.operator = operator;
    }

    class MyEvaluation extends Evaluation{
        Double lhs;
        Double rhs;
        
        @Override
        public void finish(){
            double result = 0;
            switch(operator){
                case Operator.ADD:
                    result = lhs + rhs;
                    break;
                case Operator.SUBTRACT:
                    result = lhs - rhs;
                    break;
                case Operator.MULTIPLY:
                    result = lhs * rhs;
                    break;
                case Operator.DIV:
                    result = lhs / rhs;
                    break;
                case Operator.MOD:
                    result = lhs % rhs;
                    break;
            }
            setResult(result);
        }

        @Override
        protected void consume(Object member, Object result){
            if(member==members.get(0))
                lhs = (Double)result;
            if(member==members.get(1))
                rhs = (Double)result;
            if(lhs!=null && rhs!=null)
                finish();
        }

        @Override
        protected void print(){
        }
    }

    @Override
    protected Evaluation createEvaluation(){
        return new MyEvaluation();
    }
}
