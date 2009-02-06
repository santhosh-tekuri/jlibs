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

import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.ResultType;
import jlibs.xml.sax.sniff.model.UserResults;
import jlibs.xml.sax.sniff.model.expr.Expression;

/**
 * @author Santhosh Kumar T
 */
public class Predicate extends ValidatedExpression{
    public Predicate(Node contextNode, UserResults member, Expression predicate){
        super(ResultType.BOOLEAN, contextNode, member, predicate);
    }

    private class MyEvaluation extends DelayedEvaluation{
        private boolean memberHit;

        @Override
        protected Object getCachedResult(){
            return memberHit;
        }

        @Override
        protected void predicateAccepted(){
            if(memberHit)
                setResult(memberHit);
        }

        @Override
        protected void consumeMemberResult(Object result){
            if(result instanceof Event || result==Boolean.TRUE){
                memberHit = true;
                resultPrepared();
            }
        }
    }

    @Override
    protected Expression.Evaluation createEvaluation(){
        return new MyEvaluation();
    }
}
