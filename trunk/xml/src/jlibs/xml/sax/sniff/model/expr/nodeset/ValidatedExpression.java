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

package jlibs.xml.sax.sniff.model.expr.nodeset;

import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.Notifier;
import jlibs.xml.sax.sniff.model.ResultType;
import jlibs.xml.sax.sniff.model.expr.Expression;

/**
 * @author Santhosh Kumar T
 */
public abstract class ValidatedExpression extends Expression{
    public ValidatedExpression(ResultType returnType, Node contextNode, Notifier member, Expression predicate){
        super(contextNode, returnType, member.resultType(), ResultType.BOOLEAN);
        if(member.resultType()!=ResultType.NODESET && member.resultType()!=returnType)
            throw new IllegalArgumentException();
        addMember(member);
        if(predicate!=null)
            addMember(predicate);
    }

    @Override
    protected Object defaultValue(){
        if(listener instanceof ValidatedExpression)
            return null;
        else
            return super.defaultValue();
    }

    protected abstract class DelayedEvaluation extends Evaluation{
        private Boolean predicateHit = members.size()==2 ? null : Boolean.TRUE;

        protected abstract Object getCachedResult();

        @Override
        public void finish(){
            if(predicateHit==Boolean.TRUE)
                setResult(getCachedResult());
            else
                setResult(null);
        }

        public boolean resultPrepared;
        protected void resultPrepared(){
            resultPrepared = true;
            if(predicateHit==Boolean.TRUE)
                setResult(getCachedResult());
        }
        
        protected abstract void consumeMemberResult(Object result);
        protected void predicateAccepted(){}

        @Override
        protected void consume(Object member, Object result){
            if(member==members.get(0)){
                if(!resultPrepared)
                    consumeMemberResult(result);
            }
            if(predicateHit==null && member==members.get(1)){
                predicateHit = (Boolean)result;
                if(predicateHit==Boolean.TRUE){
                    predicateAccepted();
                    return;
                }
            }

            if(predicateHit==Boolean.FALSE)
                setResult(null);
        }

        @Override
        protected void print(){
            debugger.println("cached: %s", getCachedResult());
            debugger.println("predicateHit: %s", predicateHit);
        }
    }
}
