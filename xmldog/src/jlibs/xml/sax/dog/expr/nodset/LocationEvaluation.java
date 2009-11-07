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

package jlibs.xml.sax.dog.expr.nodset;

import jlibs.core.util.LongTreeMap;
import jlibs.xml.sax.dog.DataType;
import jlibs.xml.sax.dog.Scope;
import jlibs.xml.sax.dog.expr.Evaluation;
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.expr.LinkableEvaluation;
import jlibs.xml.sax.dog.path.AxisListener;
import jlibs.xml.sax.dog.path.EventID;
import jlibs.xml.sax.dog.path.PositionalPredicate;
import jlibs.xml.sax.dog.path.Step;
import jlibs.xml.sax.dog.sniff.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public final class LocationEvaluation extends AxisListener<LocationExpression>{
    private final Event event;
    private final EventID eventID;
    private final int index;
    private final boolean lastStep;

    private final Step currentStep;
    private final boolean exactPosition;
    private Evaluation predicateEvaluation;
    private Boolean predicateResult = Boolean.TRUE;

    private PositionMatches positionMatchesHead;

    protected LocationEvaluation(LocationExpression expression, int stepIndex, Event event, EventID eventID){
        super(expression, event.order());
        this.event = event;
        this.eventID = eventID;
        this.index = stepIndex;
        lastStep = index==expression.locationPath.steps.length-1;

        if(expression instanceof Strings)
            stringEvaluations = new ArrayList<Evaluation>();

        currentStep = expression.locationPath.steps[stepIndex];
        exactPosition = currentStep.getPredicate() instanceof ExactPosition;
        PositionalPredicate positionPredicate = currentStep.headPositionalPredicate;
        if(positionPredicate!=null){
            PositionMatches positionMatches = positionMatchesHead = new PositionMatches(positionPredicate);
            while((positionPredicate = positionPredicate.next)!=null)
                positionMatches = positionMatches.next = new PositionMatches(positionPredicate);
        }
    }

    protected LocationEvaluation(LocationExpression expression, int stepIndex, Event event, EventID eventID, Expression predicate, Evaluation predicateEvaluation){
        this(expression, stepIndex, event, eventID);
        predicateResult = null;
        if(predicateEvaluation==null)
            this.predicateEvaluation = event.addListener(predicate, this);
        else{
            this.predicateEvaluation = predicateEvaluation;
            predicateEvaluation.addListener(this);
        }
    }

    @Override
    public void start(){
        assert predicateResult!=Boolean.FALSE;
        assert !finished;

        if(predicateEvaluation!=null){
            Expression predicate = expression.locationPath.steps[index-1].getPredicate();
            if(predicate.scope()!=Scope.DOCUMENT)
                predicateEvaluation.start();
        }
        eventID.addListener(event, currentStep, this);
    }

    private LinkableEvaluation pendingEvaluationHead, pendingEvaluationTail;
    private List<Evaluation> stringEvaluations;

    protected int position;

    @Override
    public void onHit(EventID eventID){
        assert !finished : "getting events even after finish";
        position++;

        final LocationExpression expression = this.expression;
        if(!lastStep){
            if(eventID.isEmpty(expression.locationPath.steps[index+1].axis))
                return;
        }

        final Event event = this.event;
        event.locationEvaluationStack.addFirst(this);

        if(positionMatchesHead!=null){
            PositionMatches positionMatches = positionMatchesHead;
            do{
                positionMatches.addEvaluation(event);
            }while((positionMatches=positionMatches.next)!=null);
        }

        LinkableEvaluation childEval = null;

        Expression predicate = currentStep.getPredicate();
        Object predicateResult = predicate==null ? Boolean.TRUE : event.evaluate(predicate);
        if(predicateResult==Boolean.TRUE){
            if(lastStep)
                consume(event);
            else
                childEval = new LocationEvaluation(expression, index+1, event, eventID);
        }else if(predicateResult==null){
            Evaluation predicateEvaluation = event.evaluation;
            if(lastStep)
                childEval = new PredicateEvaluation(expression, event.order(), expression.getResultItem(event), event, predicate, predicateEvaluation);
            else
                childEval = new LocationEvaluation(expression, index+1, event, eventID, predicate, predicateEvaluation);
        }

        if(childEval!=null){
            childEval.addListener(this);
            if(pendingEvaluationTail!=null){
                pendingEvaluationTail.next = childEval;
                childEval.previous = pendingEvaluationTail;
                pendingEvaluationTail = childEval;
            }else
                pendingEvaluationHead = pendingEvaluationTail = childEval;
            childEval.start();
        }

        if(positionMatchesHead!=null){
            PositionMatches positionMatches = positionMatchesHead;
            do{
                positionMatches.startEvaluation();
            }while((positionMatches=positionMatches.next)!=null);
        }

        event.locationEvaluationStack.pollFirst();

        if(exactPosition && predicateResult==Boolean.TRUE){
            manuallyExpired = true;
            expired();
        }
    }

    private PositionMatches getPositionMatches(Expression predicate){
        PositionMatches positionMatches = positionMatchesHead;
        do{
            if(positionMatches.predicate==predicate)
                return positionMatches;
        }while((positionMatches=positionMatches.next)!=null);

        return null;
    }

    void addPositionListener(PositionalEvaluation evaluation){
        PositionMatches positionMatches = getPositionMatches(evaluation.expression.predicate);
        assert positionMatches.map.lastEntry().value==positionMatches.listeners;
        positionMatches.listeners.addListener(evaluation);
    }

    void removePositionListener(PositionalEvaluation evaluation){
        PositionMatches positionMatches = getPositionMatches(evaluation.expression.predicate);
        PositionalListeners positionalListeners = positionMatches.map.get(evaluation.order);
        if(positionalListeners==null)
            evaluation.disposed = true;
        else
            positionalListeners.removeListener(evaluation);
    }

    private PositionalEvaluation lastListenerHead, lastListenerTail;
    void addLastLitener(PositionalEvaluation evaluation){
        Expression predicate = evaluation.expression.predicate;
        if(predicate==null){
            if(lastListenerTail==null)
                lastListenerHead = lastListenerTail = evaluation;
            else{
                lastListenerTail.next = evaluation;
                evaluation.previous = lastListenerTail;
                lastListenerTail = evaluation;
            }
        }else{
            PositionMatches matches = getPositionMatches(predicate);
            if(matches.lastListenerTail==null)
                matches.lastListenerHead = matches.lastListenerTail = evaluation;
            else{
                matches.lastListenerTail.next = evaluation;
                evaluation.previous = matches.lastListenerTail;
                matches.lastListenerTail = evaluation;
            }
        }
    }

    void removeLastLitener(PositionalEvaluation evaluation){
        PositionalEvaluation prev = evaluation.previous;
        PositionalEvaluation next = evaluation.next;

        Expression predicate = evaluation.expression.predicate;
        if(predicate==null){
            if(prev!=null)
                prev.next = next;
            else
                lastListenerHead = next;

            if(next!=null)
                next.previous = prev;
            else
                lastListenerTail = prev;
        }else{
            PositionMatches matches = getPositionMatches(predicate);
            if(prev!=null)
                prev.next = next;
            else
                matches.lastListenerHead = next;

            if(next!=null)
                next.previous = prev;
            else
                matches.lastListenerTail = prev;
        }
    }

    /*-------------------------------------------------[ Stages ]---------------------------------------------------*/

    private boolean expired = false;

    @Override
    public void expired(){
        assert !expired;
        expired = true;

        if(lastListenerHead!=null){
            Double last = (double)position;
            for(PositionalEvaluation lastEval=lastListenerHead; lastEval!=null; lastEval=lastEval.next)
                lastEval.setResult(last);
            lastListenerHead = lastListenerTail = null;
        }

        if(positionMatchesHead!=null){
            PositionMatches positionMatches = positionMatchesHead;
            do{
                positionMatches.expired();
            }while((positionMatches=positionMatches.next)!=null);
        }

        if(pendingEvaluationHead==null)
            resultPrepared();
    }

    private boolean resultPrepared = false;
    public void resultPrepared(){
        if(!resultPrepared){
            manuallyExpired = true;
            resultPrepared = true;

            for(LinkableEvaluation pendingEval=pendingEvaluationHead; pendingEval!=null; pendingEval=pendingEval.next)
                pendingEval.removeListener(this);
            pendingEvaluationHead = pendingEvaluationTail = null;
        }
        if(predicateResult!=null && (index!=0 || (stringEvaluations==null || stringEvaluations.size()==0)))
            finished();
        else if(result.size()==0 && predicateResult==null){ // when result is empty, there is no need to wait for predicateEvaluation to finish
            Expression predicate = expression.locationPath.steps[index-1].getPredicate();
            if(predicate.scope()!=Scope.DOCUMENT)
                predicateEvaluation.removeListener(this);
            else
                event.removeListener(predicate, this);
            finished();
        }
    }

    protected boolean finished = false;
    protected void finished(){
        if(!finished){
            finished = true;
            for(LinkableEvaluation pendingEval=pendingEvaluationHead; pendingEval!=null; pendingEval=pendingEval.next)
                pendingEval.removeListener(this);
            pendingEvaluationHead = pendingEvaluationTail = null;
            fireFinished();
        }
    }

    @Override
    public void fireFinished(){
        super.fireFinished();
        if(stringEvaluations!=null){
            for(Evaluation stringEval: stringEvaluations)
                stringEval.removeListener(this);
        }
    }

    @Override
    protected void dispose(){
        manuallyExpired = true;
        for(LinkableEvaluation pendingEval=pendingEvaluationHead; pendingEval!=null; pendingEval=pendingEval.next)
            pendingEval.removeListener(this);
        pendingEvaluationHead = pendingEvaluationTail = null;
        if(predicateResult==null)
            predicateEvaluation.removeListener(this);
    }

    /*-------------------------------------------------[ Result Management ]---------------------------------------------------*/

    protected LongTreeMap<Object> result = new LongTreeMap<Object>();

    private void consumedResult(){
        int resultSize = result.size();
        if(resultSize>0 && !expression.many){
            if(resultSize>1)
                result.deleteEntry(result.lastEntry());
            if(expression.first){
                if(pendingEvaluationHead==null || result.firstEntry().getKey()<=pendingEvaluationHead.order)
                    resultPrepared();
                else if(!expired){
                    manuallyExpired = true;
                    expired = true;
                }
            }else
                resultPrepared();
        }else if(expired && pendingEvaluationHead==null)
            resultPrepared();
    }

    protected void consume(Event event){
        Object resultItem = expression.getResultItem(event);
        if(resultItem instanceof Evaluation){
            Evaluation eval = (Evaluation)resultItem;
            stringEvaluations.add(eval);
            eval.addListener(this);
            eval.start();
        }
        assert resultItem!=null : "ResultItem should be non-null";
        result.put(event.order(), resultItem);
        consumedResult();
    }

    protected void consumeChildEvaluation(long order, Object resultItem){
        boolean prepareResult = false;
        if(expression.resultType==DataType.NUMBER){
            if(resultItem instanceof Double && ((Double)resultItem).isNaN()){
                result.clear();
                prepareResult = true;
            }
        }

        result.put(order, resultItem);
        consumedResult();

        if(prepareResult)
            resultPrepared();
    }

    protected void consumeChildEvaluation(LongTreeMap<Object> childResult){
        boolean prepareResult = false;
        int size = childResult.size();
        if(size==1 && expression.resultType==DataType.NUMBER){
            Object resultItem = childResult.firstEntry().value;
            if(resultItem instanceof Double && ((Double)resultItem).isNaN()){
                result.clear();
                prepareResult = true;
            }
        }

        if(size>0){
            if(result.size()>0)
                result.putAll(childResult);
            else
                result = childResult;
        }
        consumedResult();

        if(prepareResult)
            resultPrepared();
    }

    private void remove(LinkableEvaluation eval){
        LinkableEvaluation prev = eval.previous;
        LinkableEvaluation next = eval.next;

        if(prev!=null)
            prev.next = next;
        else
            pendingEvaluationHead = next;

        if(next!=null)
            next.previous = prev;
        else
            pendingEvaluationTail = prev;
    }

    @Override
    public final void finished(Evaluation evaluation){
        assert !finished : "can't consume evaluation result after finish";

        if(evaluation==predicateEvaluation){
            predicateResult = (Boolean)evaluation.getResult();
            assert predicateResult!=null : "evaluation result should be non-null";
            if(predicateResult==Boolean.FALSE){
                result.clear();
                if(stringEvaluations!=null){
                    for(Evaluation stringEval: stringEvaluations)
                        stringEval.removeListener(this);
                    stringEvaluations = null;
                }
                resultPrepared();
            }else if(resultPrepared)
                finished();
        }else if(evaluation instanceof PredicateEvaluation){
            PredicateEvaluation predicateEvaluation = (PredicateEvaluation)evaluation;
            remove(predicateEvaluation);

            if(predicateEvaluation.result!=null){
                Object resultItem = predicateEvaluation.result;
                if(resultItem instanceof Evaluation){
                    Evaluation stringEval = (Evaluation)resultItem;
                    stringEvaluations.add(stringEval);
                    stringEval.addListener(this);
                }
                consumeChildEvaluation(predicateEvaluation.order, resultItem);
            }else
                consumedResult();
        }else if(evaluation instanceof LocationEvaluation){
            LocationEvaluation locEval = (LocationEvaluation)evaluation;
            remove(locEval);

            if(locEval.stringEvaluations!=null){
                for(Evaluation stringEval: locEval.stringEvaluations)
                    stringEval.addListener(this);
                stringEvaluations.addAll(locEval.stringEvaluations);
            }
            boolean wasExpired = expired;
            consumeChildEvaluation(locEval.result);
            if(!wasExpired && expired){
                assert !finished;
                LinkableEvaluation eval = locEval.next;
                while(eval!=null){
                    eval.removeListener(this);
                    remove(eval);
                    eval = eval.next;
                }
                if(pendingEvaluationHead==null)
                    resultPrepared();
            }
        }else{
            stringEvaluations.remove(evaluation);
            consumeChildEvaluation(evaluation.order, evaluation.getResult());
        }
    }

    private Object finalResult;
    public Object getResult(){
        if(finalResult==null)
            finalResult = expression.getResult(result);
        return finalResult;
    }
}