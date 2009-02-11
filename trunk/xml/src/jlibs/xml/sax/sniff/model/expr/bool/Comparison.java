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

import jlibs.xml.sax.sniff.model.Datatype;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.expr.Expression;

import java.util.Collections;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public abstract class Comparison extends Expression{
    private String name;

    public Comparison(Node contextNode, String name){
        super(contextNode, Datatype.BOOLEAN, Datatype.STRINGS, Datatype.STRINGS);
        this.name = name;
    }

    class MyEvaluation extends Evaluation{
        public List lhsResults;
        public List rhsResults;

        @Override
        public void finish(){
            if(lhsResults!=null && rhsResults!=null){
                Datatype lhsType = members.get(0).resultType();
                if(lhsType==Datatype.STRINGS)
                    lhsType = Datatype.STRING;

                Datatype rhsType = members.get(1).resultType();
                if(rhsType==Datatype.STRINGS)
                    rhsType = Datatype.STRING;

                for(Object lhs: lhsResults){
                    for(Object rhs: rhsResults){
                        if(evaluateObjectObject(lhs, rhs)){
                            setResult(true);
                            return;
                        }
                    }
                }
            }
            setResult(false);
        }

        @Override
        protected void consume(Object member, Object result){
            if(member==members.get(0))
                lhsResults = toList(result);
            if(member==members.get(1))
                rhsResults = toList(result);
            if(lhsResults!=null && rhsResults!=null)
                finish();
        }

        private List toList(Object result){
            if(result instanceof List)
                return (List)result;
            else
                return Collections.singletonList(result);
        }

        @Override
        protected void print(){}
    }

    protected abstract boolean evaluateObjectObject(Object lhs, Object rhs);
    
    @Override
    protected Evaluation createEvaluation(){
        return new MyEvaluation();
    }

    @Override
    public String getName(){
        return name;
    }
}
