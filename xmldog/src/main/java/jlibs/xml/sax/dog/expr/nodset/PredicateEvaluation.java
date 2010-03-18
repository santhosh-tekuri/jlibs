/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T <santhosh.tekuri@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.xml.sax.dog.expr.nodset;

import jlibs.xml.sax.dog.Scope;
import jlibs.xml.sax.dog.expr.Evaluation;
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.expr.LinkableEvaluation;
import jlibs.xml.sax.dog.sniff.Event;

/**
 * @author Santhosh Kumar T
 */
public final class PredicateEvaluation extends LinkableEvaluation<LocationExpression>{
    private final Event event;
    private Object resultItem;
    private final Expression predicate;
    private final Evaluation booleanEvaluation;

    public PredicateEvaluation(LocationExpression expression, long order, Object resultItem, Event event, Expression predicate, Evaluation booleanEvaluation){
        super(expression, order);
        this.resultItem = resultItem;
        this.event = event;
        this.predicate = predicate;
        this.booleanEvaluation = booleanEvaluation;
    }

    @Override
    public void start(){
        if(resultItem instanceof Evaluation){
            Evaluation eval = (Evaluation)resultItem;
            eval.addListener(this);
            eval.start();
        }

        if(booleanEvaluation==null)
            event.addListener(predicate, this);
        else{
            booleanEvaluation.addListener(this);
            booleanEvaluation.start();
        }
    }

    protected Object result;

    @Override
    public void finished(Evaluation evaluation){
        if(evaluation==resultItem)
            resultItem = evaluation.getResult();
        else{
            if(evaluation.getResult()==Boolean.TRUE)
                result = resultItem;
            fireFinished();
        }
    }

    @Override
    protected void fireFinished(){
        super.fireFinished();
        if(resultItem instanceof Evaluation)
            ((Evaluation)resultItem).removeListener(this);
    }

    @Override
    protected void dispose(){
        if(resultItem instanceof Evaluation)
            ((Evaluation)resultItem).removeListener(this);
        else if(nodeSetListener !=null)
            nodeSetListener.discard(order);

        if(booleanEvaluation==null){
            if(predicate.scope()==Scope.DOCUMENT)
                event.removeListener(predicate, this);
        }else
            booleanEvaluation.removeListener(this);
    }

    @Override
    public Object getResult(){
        throw new UnsupportedOperationException();
    }

    public NodeSetListener nodeSetListener;
}
