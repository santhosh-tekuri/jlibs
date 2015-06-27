/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package jlibs.xml.sax.dog.expr.nodset;

import jlibs.core.lang.ImpossibleException;
import jlibs.core.lang.NotImplementedException;
import jlibs.core.util.LongTreeMap;
import jlibs.xml.sax.dog.DataType;
import jlibs.xml.sax.dog.Scope;
import jlibs.xml.sax.dog.expr.Evaluation;
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.expr.Literal;
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
    public final Expression relativeExpression;
    public final boolean forEach;

    public PathExpression(LocationPath union, Expression relativeExpression, boolean forEach){
        super(Scope.DOCUMENT, relativeExpression.resultType);
        assert relativeExpression.scope()!=Scope.DOCUMENT;

        this.union = union;
        contexts = new Expression[union.contexts.size()];
        for(int i=0; i<contexts.length; i++)
            contexts[i] = union.contexts.get(i).typeCast(DataType.NODESET);

        this.relativeExpression = relativeExpression;
        if(relativeExpression instanceof LocationExpression)
            ((LocationExpression)relativeExpression).rawResult = true;
        else
            ((Literal)relativeExpression).rawResultRequired();
        
        this.forEach = forEach;
        
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
        return String.format("path-expression(context(%s), %s, %s)", buff, relativeExpression, forEach);
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

final class PathEvaluation extends Evaluation<PathExpression> implements NodeSetListener, NodeSetListener.Support{
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
                    pendingCount++;
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
                if(nodeSetListener!=null && !(nodeSetListener instanceof Event)){ // nodeSetListener will be event if xmlBuilder is set
                    if(resultItem instanceof LocationEvaluation)
                        ((LocationEvaluation)resultItem).nodeSetListener = evalInfo;
                    else
                        ((PathEvaluation)resultItem).nodeSetListener = evalInfo;
                }
                Evaluation childEval = new PredicateEvaluation(expression.relativeExpression, event.order(), resultItem, event, predicate, predicateEvaluation);
                childEval.addListener(this);
                childEval.start();
                evalInfo.eval = childEval;
                pendingCount++;
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
                if(entry.value.eval!=null){
                    pendingCount--;
                    entry.value.eval.removeListener(this);
                }
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

    private int pendingCount;
    private int pendingCount(){
        int count = 0;
        for(LongTreeMap.Entry<EvaluationInfo> entry = evaluations.firstEntry(); entry!=null; entry=entry.next()){
            if(entry.value.eval!=null)
                count++;
        }
        return count;
    }
    
    private Object finalResult;
    private void tryToFinish(){
        if(finalResult==null){
            if(contextsPending>0)
                return;
            assert pendingCount==pendingCount();
            if(pendingCount==0){
                finalResult = computeResult();
                if(nodeSetListener!=null)
                    nodeSetListener.finished();
                fireFinished();
            }
        }
    }
    
    @SuppressWarnings({"unchecked"})
    public Object computeResult(){
        if(expression.forEach){
            List<Object> result = new ArrayList<Object>(evaluations.size());
            for(LongTreeMap.Entry entry = evaluations.firstEntry(); entry!=null; entry=entry.next())
                result.add(computeResultItem(((EvaluationInfo)entry.value).result));
            return result;
        }else{
            LongTreeMap result = new LongTreeMap();
            for(LongTreeMap.Entry<EvaluationInfo> entry = evaluations.firstEntry(); entry!=null; entry=entry.next())
                result.putAll(entry.value.result);
            return computeResultItem(result);
        }
    }

    @SuppressWarnings({"unchecked", "UnnecessaryBoxing"})
    private Object computeResultItem(LongTreeMap result){
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
                    entry.value.setResult(predicateEvaluation.result);
                    entry.value.eval = null;
                    pendingCount--;
                }
            }else{
                entry.value.doDiscards();
                evaluations.deleteEntry(entry);
                if(entry.value.eval!=null)
                    pendingCount--;
            }
        }else{
            entry.value.setResult(evaluation.getResult());
            entry.value.eval = null;
            pendingCount--;
        }
        tryToFinish();
    }

    private NodeSetListener nodeSetListener;

    @Override
    public void setNodeSetListener(NodeSetListener nodeSetListener){
        this.nodeSetListener = nodeSetListener;
    }
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
        if(result instanceof LongTreeMap)
            this.result = (LongTreeMap)result;
        else{
            this.result = new LongTreeMap();
            this.result.put(order, result);
        }
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