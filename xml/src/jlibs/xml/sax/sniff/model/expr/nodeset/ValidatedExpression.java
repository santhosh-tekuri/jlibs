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
import jlibs.xml.sax.sniff.model.AxisNode;
import jlibs.xml.sax.sniff.model.Datatype;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.Notifier;
import jlibs.xml.sax.sniff.model.axis.Following;
import jlibs.xml.sax.sniff.model.expr.Expression;

/**
 * @author Santhosh Kumar T
 */
public abstract class ValidatedExpression extends Expression{
    public boolean storeDocumentOrder;
    
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
        if(listeners.get(0) instanceof ValidatedExpression)
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

    private boolean delegateOnNotification;
    
    public void onNotification2(Notifier source, Context context, Object result){
        if(source==members.get(0) && result instanceof Event && source!=evaluationStartNode)
            onNotification1(source, context, result);
        else
            super.onNotification(source, context, result);
    }

    @SuppressWarnings({"EqualsBetweenInconvertibleTypes"})
    protected boolean canEvaluate(Node source, Evaluation evaluation, Context context, Event event){
        if(evaluationStack.size()==1)
            return true;

        int diff = source.depth-evaluationStartNode.depth;
        if(diff==0)
            return true;

        Following following = null;
        if(source instanceof Following)
            following = (Following)source;
        else{
            AxisNode axisNode = source.getConstraintAxis();
            if(axisNode instanceof Following)
                following = (Following)axisNode;
        }
        if(following!=null)
            return following.matchesWith(evaluation.contextIdentity, event);

        if(event.hasChildren())
            return evaluation.contextIdentity.isChild(context);
        else
            return evaluation.contextIdentity.equals(context) || evaluation.contextIdentity.isChild(context); 
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
            if(canEvaluate((Node)source, eval, context, (Event)result))
                evalutate(eval, source, context, result);
        }

        if(debug)
            debugger.indent--;
    }
}
