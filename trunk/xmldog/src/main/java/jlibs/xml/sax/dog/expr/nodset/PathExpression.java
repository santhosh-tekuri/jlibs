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

import jlibs.core.lang.ImpossibleException;
import jlibs.core.lang.NotImplementedException;
import jlibs.core.util.LongTreeMap;
import jlibs.xml.sax.dog.DataType;
import jlibs.xml.sax.dog.Scope;
import jlibs.xml.sax.dog.expr.Evaluation;
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.path.LocationPath;
import jlibs.xml.sax.dog.sniff.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: simplify has to be implemented
 * 
 * @author Santhosh Kumar T
 */
public class PathExpression extends Expression{
    public final LocationPath union;
    public final Expression contexts[];
    public final LocationExpression relativeExpression;

    public PathExpression(LocationPath union, LocationExpression relativeExpression){
        super(Scope.DOCUMENT, relativeExpression.resultType);
        assert relativeExpression.scope()==Scope.LOCAL;

        this.union = union;
        contexts = new Expression[union.contexts.size()];
        for(int i=0; i<contexts.length; i++)
            contexts[i] = union.contexts.get(i).typeCast(DataType.NODESET);

        this.relativeExpression = relativeExpression;
        relativeExpression.rawResult = true;
        
        if(union.hitExpression!=null)
            union.hitExpression.pathExpression = this;
    }


    @Override
    public Object getResult(){
        return null;
    }

    @Override
    public Object getResult(Event event){
        return new PathEvaluation(this, event);
    }

    @Override
    public String toString(){
        StringBuilder buff = new StringBuilder();
        for(Expression context: contexts){
            if(buff.length()>0)
                buff.append(", ");
            buff.append(context);
        }
        if(union.predicateSet.getPredicate()!=null){
            buff.insert(0, '(');
            buff.append(')');
            buff.append('[');
            buff.append(union.predicateSet.getPredicate());
            buff.append(']');
        }
        String relativePath = relativeExpression.locationPath.toString();
        if(relativePath.length()>0)
            return String.format("%s(context(%s), %s)", relativeExpression.getName(), buff, relativePath);
        else
            return String.format("%s(context(%s))", relativeExpression.getName(), buff);
    }

    public static class HitExpression extends Expression{
        public PathExpression pathExpression;
        
        public HitExpression(){
            super(Scope.LOCAL, DataType.BOOLEAN);
        }

        @Override
        public Object getResult(){
            throw new ImpossibleException();
        }

        @Override
        public Object getResult(Event event){
            PathEvaluation pathEvaluation = (PathEvaluation)event.result(pathExpression);
            return pathEvaluation.evaluations.get(event.order());
        }
    }
}

final class PathEvaluation extends Evaluation<PathExpression> implements NodeSetListener{
    private Event event;

    private PositionTracker positionTracker;
    public PathEvaluation(PathExpression expression, Event event){
        super(expression, event.order());
        this.event = event;
        contextsPending = expression.contexts.length;
        if(expression.union.predicateSet.hasPosition)
            positionTracker = new PositionTracker(expression.union.predicateSet.headPositionalPredicate);
    }

    @Override
    public void start(){
        for(Expression context: expression.contexts){
            Object result = event.evaluate(context);
            if(result==null){
                Object eval = event.result(context);
                if(eval instanceof LocationEvaluation)
                    ((LocationEvaluation)eval).nodeSetListener = this;
                else
                    ((PathEvaluation)eval).nodeSetListener = this;
            }else
                throw new NotImplementedException();
        }
    }

    protected LongTreeMap<EvaluationInfo> evaluations = new LongTreeMap<EvaluationInfo>();
    @Override
    public void mayHit(){
        long order = event.order();
        EvaluationInfo evalInfo = evaluations.get(order);
        if(evalInfo==null){
            evaluations.put(order, evalInfo=new EvaluationInfo(event, expression.union.hitExpression, order, nodeSetListener));
            
            if(positionTracker!=null){
                event.positionTrackerStack.addFirst(positionTracker);
                positionTracker.addEvaluation(event);
            }
            Expression predicate = expression.union.predicateSet.getPredicate();
            Object predicateResult = predicate==null ? Boolean.TRUE : event.evaluate(predicate);
            if(predicateResult==Boolean.TRUE){
                Object r = event.evaluate(expression.relativeExpression);
                if(r==null){
                    event.evaluation.addListener(this);
                    event.evaluation.start();
                    evalInfo.eval = event.evaluation;
                    if(nodeSetListener!=null){
                        if(event.evaluation instanceof LocationEvaluation)
                            ((LocationEvaluation)event.evaluation).nodeSetListener = evalInfo;
                        else
                            ((PathEvaluation)event.evaluation).nodeSetListener = evalInfo;
                    }
                }else{
                    if(nodeSetListener!=null)
                        nodeSetListener.mayHit();
                    evalInfo.setResult(r);
                }
            }else if(predicateResult==null){
                Evaluation predicateEvaluation = event.evaluation;
                Object resultItem = expression.relativeExpression.getResult(event);
                if(nodeSetListener!=null){
                    if(resultItem instanceof LocationEvaluation)
                        ((LocationEvaluation)resultItem).nodeSetListener = evalInfo;
                    else
                        ((PathEvaluation)resultItem).nodeSetListener = evalInfo;
                }
                Evaluation childEval = new PredicateEvaluation(expression.relativeExpression, event.order(), resultItem, event, predicate, predicateEvaluation);
                childEval.addListener(this);
                childEval.start();
                evalInfo.eval = childEval;
            }else
                throw new ImpossibleException();
        }
        evalInfo.hitCount++;
        
        if(evalInfo.hitCount==1 && positionTracker!=null){
            positionTracker.startEvaluation();
            event.positionTrackerStack.pollFirst();
        }
    }

    @Override
    public void discard(long order){
        LongTreeMap.Entry<EvaluationInfo> entry = evaluations.getEntry(order);
        if(entry!=null){
            if(entry.value.discard()==0){
                evaluations.deleteEntry(entry);
                if(entry.value.eval!=null)
                    entry.value.eval.removeListener(this);
            }
        }
    }

    private int contextsPending;
    @Override
    public void finished(){
        contextsPending--;
        if(contextsPending==0){
            if(expression.union.hitExpression!=null){
                for(EvaluationInfo evalInfo: new ArrayList<EvaluationInfo>(evaluations.values()))
                    evalInfo.doFinish();
                if(positionTracker!=null)
                    positionTracker.expired();
            }
        }
        tryToFinish();
    }

    private Object finalResult;
    private void tryToFinish(){
        if(finalResult==null){
            if(contextsPending>0)
                return;
            for(LongTreeMap.Entry<EvaluationInfo> entry = evaluations.firstEntry(); entry!=null; entry=entry.next()){
                if(entry.value.eval!=null)
                    return;
            }
            finalResult = computeResult();
            if(nodeSetListener!=null)
                nodeSetListener.finished();
            fireFinished();
        }
    }
    
    @SuppressWarnings({"unchecked", "UnnecessaryBoxing"})    
    public Object computeResult(){
        LongTreeMap result = new LongTreeMap();
        for(LongTreeMap.Entry<EvaluationInfo> entry = evaluations.firstEntry(); entry!=null; entry=entry.next())
            result.putAll(entry.value.result);

        switch(expression.resultType){
            case NODESET:
            case STRINGS:
            case NUMBERS:
                return new ArrayList(result.values());
            case NUMBER:
                if(expression.relativeExpression instanceof Count)
                    return new Double(result.size());
                else{
                    double d = 0;
                    for(LongTreeMap.Entry entry=result.firstEntry(); entry!=null; entry=entry.next())
                        d += (Double)entry.value;
                    return d;
                }
            case BOOLEAN:
                return !result.isEmpty();
            default:
                if(result.isEmpty())
                    return expression.resultType.defaultValue;
                else
                    return result.firstEntry().value;
        }
    }

    @Override
    public Object getResult(){
        return finalResult;
    }
    
    @Override
    @SuppressWarnings({"unchecked"})
    public void finished(Evaluation evaluation){
        LongTreeMap.Entry<EvaluationInfo> entry = evaluations.getEntry(evaluation.order);
        assert entry.value.eval==evaluation;
        if(evaluation instanceof PredicateEvaluation){
            PredicateEvaluation predicateEvaluation = (PredicateEvaluation)evaluation;
            if(predicateEvaluation.result!=null){
                if(predicateEvaluation.result instanceof Evaluation){
                    entry.value.eval = (Evaluation)predicateEvaluation.result;
                    entry.value.eval.addListener(this);
                    return;
                }else{
                    if(predicateEvaluation.result instanceof LongTreeMap)
                        entry.value.result = (LongTreeMap)predicateEvaluation.result;
                    else{
                        entry.value.result = new LongTreeMap();
                        entry.value.result.put(evaluation.order, predicateEvaluation.result);
                    }
                    entry.value.eval = null;
                }
            }else{
                entry.value.doDiscards();
                evaluations.deleteEntry(entry);
            }
        }else{
            Object r = evaluation.getResult();
            if(r instanceof LongTreeMap)
                entry.value.result = (LongTreeMap)r;
            else
                entry.value.setResult(r);
            entry.value.eval = null;
        }
        tryToFinish();
    }

    private NodeSetListener nodeSetListener;
}

final class EvaluationInfo extends Evaluation<PathExpression.HitExpression> implements NodeSetListener{
    Event event;
    Evaluation eval;
    LongTreeMap result;

    EvaluationInfo(Event event, PathExpression.HitExpression expression, long order, NodeSetListener nodeSetListener){
        super(expression, order);
        this.event = event;
        this.nodeSetListener = nodeSetListener;
        if(nodeSetListener!=null)
            mayHits = new ArrayList<Long>();
    }
    
    @SuppressWarnings({"unchecked"})
    public void setResult(Object result){
        this.result = new LongTreeMap();
        this.result.put(order, result);
    }

    public int hitCount;
    private Boolean hit;

    public int discard(){
        if(--hitCount==0){
            hit = Boolean.FALSE;
            doDiscards();
            if(listener!=null)
                fireFinished();
        }
        return hitCount;
    }

    public void doFinish(){
        hit = Boolean.TRUE;
        if(listener!=null)
            fireFinished();
    }
    
    @Override
    public void start(){}

    @Override
    public Object getResult(){
        return hit;
    }

    @Override
    public void finished(Evaluation evaluation){}

    /*-------------------------------------------------[ NodeSetListener ]---------------------------------------------------*/
    
    public NodeSetListener nodeSetListener;
    private List<Long> mayHits;
    
    @Override
    public void mayHit(){
        mayHits.add(event.order());
        nodeSetListener.mayHit();
    }

    public void doDiscards(){
        if(nodeSetListener!=null){
            for(long order: mayHits)
                nodeSetListener.discard(order);
        }
    }
    
    @Override
    public void discard(long order){
        mayHits.remove(order);
        nodeSetListener.discard(order);
    }

    @Override
    public void finished(){}
}