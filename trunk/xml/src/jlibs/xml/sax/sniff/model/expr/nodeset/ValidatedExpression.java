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
import jlibs.xml.sax.sniff.model.Datatype;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.Notifier;
import jlibs.xml.sax.sniff.model.expr.Expression;

/**
 * @author Santhosh Kumar T
 */
public abstract class ValidatedExpression extends Expression{
    public ValidatedExpression(Datatype returnType, Node contextNode, Notifier member, Expression predicate){
        super(contextNode, returnType, member.resultType(), Datatype.BOOLEAN);
        if(member.resultType()!=Datatype.NODESET && member.resultType()!=returnType)
            throw new IllegalArgumentException();
        addMember(member);
        if(predicate!=null)
            addMember(predicate);

        if(member instanceof Node){
            Node memberNode = (Node)member;
            int diff = member.depth-contextNode.depth;
            if(diff==1 && !memberNode.canBeContext())
                delegateOnNotification = true;
        }
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
        int id;

        protected DelayedEvaluation(){
            id = evaluationStack.size();
        }

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

    private boolean delegateOnNotification;
    
    public void onNotification2(Notifier source, Context context, Object result){
        if(!(source instanceof Predicate) || source==members.get(0)){
            if(result instanceof Event && source!=contextNode)
                onNotification1(source, context, result);
            else
                super.onNotification(source, context, result);
            return;
        }

        if(debug){
            debugger.println("onNotification: %s", this);
            debugger.indent++;
        }

        int evaluationIndex = ((Predicate)source).evaluationIndex;
        for(Evaluation eval: evaluationStack){
            DelayedEvaluation evaluation = (DelayedEvaluation)eval;
            if(evaluation.id==evaluationIndex){
                if(!evaluation.finished){
                    if(debug){
                        debugger.println("Evaluation:");
                        debugger.indent++;
                    }
                    this.context = context;
                    evaluation.consume(source, result);
                    if(debug){
                        if(!evaluation.finished)
                            evaluation.print();
                        debugger.indent--;
                    }
                }
            }
        }

        if(debug)
            debugger.indent--;
    }

    protected boolean canEvaluate(Node source, Evaluation evaluation, Context context, Event event){
        int diff = source.depth-contextNode.depth;
        if(diff==0)
            return true;
        else if(event.hasChildren())
            return evaluation.contextIdentity.isChild(context);
        else
            return evaluation.contextIdentity.equals(context.identity()) || evaluation.contextIdentity.isChild(context); 
    }

    public void onNotification1(Notifier source, Context context, Object result){
        if(delegateOnNotification || !(result instanceof Event)){
            super.onNotification(source, context, result);
            return;
        }

        if(debug){
            debugger.println("onNotification: %s", this);
            debugger.indent++;
        }

        for(Evaluation eval: evaluationStack){
            if(canEvaluate((Node)source, eval, context, (Event)result)){
                if(!eval.finished){
                    if(debug){
                        debugger.println("Evaluation:");
                        debugger.indent++;
                    }
                    this.context = context;
                    ((DelayedEvaluation)eval).consume(source, result);
                    if(debug){
                        if(!eval.finished)
                            ((DelayedEvaluation)eval).print();
                        debugger.indent--;
                    }
                }
            }
        }

        if(debug)
            debugger.indent--;
    }
}
