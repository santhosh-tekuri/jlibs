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

import jlibs.core.lang.NotImplementedException;
import jlibs.core.util.LongTreeMap;
import jlibs.xml.sax.dog.Scope;
import jlibs.xml.sax.dog.expr.Evaluation;
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.path.LocationPath;
import jlibs.xml.sax.dog.sniff.Event;

import java.util.ArrayList;

/**
 * TODO: simplify has to be implemented
 * 
 * @author Santhosh Kumar T
 */
public class PathExpression extends Expression{
    public final LocationPath union;
    public final LocationExpression contexts[];
    public final LocationExpression relativeExpression;

    public PathExpression(LocationPath union, LocationExpression relativeExpression){
        super(Scope.DOCUMENT, relativeExpression.resultType);
        assert relativeExpression.scope()==Scope.LOCAL;

        this.union = union;
        contexts = new LocationExpression[union.contexts.size()];
        for(int i=0; i<contexts.length; i++)
            contexts[i] = new NodeSet(union.contexts.get(i));

        this.relativeExpression = relativeExpression;
        relativeExpression.rawResult = true;
        
        if(union.hasPosition)
            throw new NotImplementedException("positional evaluations in path expressions");
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
        for(LocationExpression context: contexts){
            if(buff.length()>0)
                buff.append(", ");
            buff.append(context);
        }
        if(union.getPredicate()!=null){
            buff.insert(0, '(');
            buff.append(')');
            buff.append('[');
            buff.append(union.getPredicate());
            buff.append(']');
        }
        String relativePath = relativeExpression.locationPath.toString();
        if(relativePath.length()>0)
            return String.format("%s(context(%s), %s)", relativeExpression.getName(), buff, relativePath);
        else
            return String.format("%s(context(%s))", relativeExpression.getName(), buff);
    }
}

class PathEvaluation extends Evaluation<PathExpression> implements NodeSetListener{
    private Event event;

    public PathEvaluation(PathExpression expression, Event event){
        super(expression, event.order());
        this.event = event;
        contextsPending = expression.contexts.length;
    }

    @Override
    public void start(){
        for(LocationExpression context: expression.contexts){
            Object result = event.evaluate(context);
            if(result==null)
                ((LocationEvaluation)event.result(context)).nodeSetListener = this;
            else
                throw new NotImplementedException();
        }
    }

    protected LongTreeMap<EvaluationInfo> evaluations = new LongTreeMap<EvaluationInfo>();
    @Override
    public void mayHit(){
        long order = event.order();
        LongTreeMap.Entry<EvaluationInfo> entry = evaluations.getEntry(order);
        if(entry==null){
            Expression predicate = expression.union.getPredicate();
            Object predicateResult = predicate==null ? Boolean.TRUE : event.evaluate(predicate);
            if(predicateResult==Boolean.TRUE){
                Object r = event.evaluate(expression.relativeExpression);
                if(r==null){
                    event.evaluation.addListener(this);
                    event.evaluation.start();
                    evaluations.put(order, new EvaluationInfo(order, event.evaluation));
                }else{
                    evaluations.put(order, new EvaluationInfo(order, r));
                }
            }else if(predicateResult==null){
                Evaluation predicateEvaluation = event.evaluation;
                Evaluation childEval = new PredicateEvaluation(expression.relativeExpression, event.order(), expression.relativeExpression.getResult(event), event, predicate, predicateEvaluation);
                childEval.addListener(this);
                childEval.start();
                evaluations.put(order, new EvaluationInfo(order, childEval));
            }
        }else
            entry.value.hitCount++;
    }

    @Override
    public void discard(long order){
        LongTreeMap.Entry<EvaluationInfo> entry = evaluations.getEntry(order);
        if(entry!=null){
            if(--entry.value.hitCount==0){
                evaluations.deleteEntry(entry);
                entry.value.eval.removeListener(this);
            }
        }
    }

    private int contextsPending;
    @Override
    public void finished(){
        contextsPending--;
        if(canFinish())
            fireFinished();
    }

    private boolean canFinish(){
        if(contextsPending>0)
            return false;
        for(LongTreeMap.Entry<EvaluationInfo> entry = evaluations.firstEntry(); entry!=null; entry=entry.next())
            if(entry.value.eval!=null)
                return false;
        return true;
    }

    @Override
    @SuppressWarnings({"unchecked", "UnnecessaryBoxing"})
    public Object getResult(){
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
            }else
                evaluations.deleteEntry(entry);
        }else{
            entry.value.result = (LongTreeMap)evaluation.getResult();
            entry.value.eval = null;
        }
        if(canFinish())
            fireFinished();
    }
}

class EvaluationInfo{
    long order;
    Evaluation eval;
    LongTreeMap result;
    int hitCount;

    EvaluationInfo(long order, Evaluation eval){
        this.order = order;
        this.eval = eval;
    }

    @SuppressWarnings({"unchecked"})
    EvaluationInfo(long order, Object result){
        this.order = order;
        this.result = new LongTreeMap<Object>();
        this.result.put(order, result);
    }
}