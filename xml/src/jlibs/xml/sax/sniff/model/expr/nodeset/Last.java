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

import jlibs.xml.sax.sniff.engine.context.Context;
import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.model.ContextListener;
import jlibs.xml.sax.sniff.model.Datatype;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.Notifier;
import jlibs.xml.sax.sniff.model.expr.Expression;

/**
 * @author Santhosh Kumar T
 */
public class Last extends ValidatedExpression{
    public Last(Node contextNode, Notifier member, Expression predicate){
        super(Datatype.BOOLEAN, contextNode, member, predicate);
        contextNode.parent.addContextListener(new ContextListener(){
            @Override
            public void contextStarted(Context context, Event event){}

            @Override
            public void contextEnded(Context context){
                boolean last = true;
                while(!evaluationStack.isEmpty()){
                    MyEvaluation eval = (MyEvaluation)evaluationStack.pop();
                    if(!eval.finished){
                        finishEvaluation(eval, last);
                        last = false;
                    }
                }
            }

            @Override
            public int priority(){
                return evalDepth-1;
            }
        });
        evaluationEndNode = contextNode.parent;
        evaluationEndNode.addContextListener(this);
    }

    class MyEvaluation extends DelayedEvaluation{
        boolean last;

        @Override
        protected Object getCachedResult(){
            return last;
        }

        @Override
        protected void consumeMemberResult(Object result){}
    }

    @Override
    protected Evaluation createEvaluation(){
        return new MyEvaluation();
    }

    @Override
    public void contextStarted(Context context, Event event){
        if(members.size()==1) // has no predicate
            finishEvaluation(false);
        super.contextStarted(context, event);
    }

    private void finishEvaluation(boolean last){
        if(!evaluationStack.isEmpty())
            finishEvaluation((MyEvaluation)evaluationStack.pop(), last);
    }

    private void finishEvaluation(MyEvaluation eval, boolean last){
        if(!eval.finished){
            eval.last = last;
            eval.finish();
            if(debug)
                debugger.println("finishedEvaluation: %s", this);
        }
    }

    @Override
    public void contextEnded(Context context){}
}