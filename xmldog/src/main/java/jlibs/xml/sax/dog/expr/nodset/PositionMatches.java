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

import jlibs.core.util.LongTreeMap;
import jlibs.xml.sax.dog.expr.Evaluation;
import jlibs.xml.sax.dog.expr.EvaluationListener;
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.path.PositionalPredicate;
import jlibs.xml.sax.dog.sniff.Event;

/**
 * @author Santhosh Kumar T
 */
public final class PositionMatches extends EvaluationListener{
    final Expression predicate;
    final int positionListenerCount;
    final LongTreeMap<PositionalListeners> map = new LongTreeMap<PositionalListeners>();
    PositionMatches next;

    PositionalEvaluation lastListenerHead, lastListenerTail;

    PositionMatches(PositionalPredicate positionalPredicate){
        predicate = positionalPredicate.predicate;
        positionListenerCount = positionalPredicate.position;
    }

    PositionalListeners listeners;
    public void addEvaluation(Event event){
        Object result = event.evaluate(predicate);
        if(result!=null)
            listeners = new PositionalListeners(event.order(), (Boolean)result, positionListenerCount);
        else{
            listeners = new PositionalListeners(event.evaluation, positionListenerCount);
            event.evaluation.addListener(this);
        }

        map.put(event.order(), listeners);
    }

    public void startEvaluation(){
        if(listeners.evaluation!=null)
            listeners.evaluation.start();
        else
            finished(listeners.order, listeners.result);
    }

    private int position = 0;
    private boolean expired;

    public void expired(){
        expired = true;
        if(lastListenerHead!=null)
            checkLast();
    }

    private void checkLast(){
        if(expired && map.isEmpty()){
            Double last = (double)position;
            for(PositionalEvaluation eval=lastListenerHead; eval!=null; eval=eval.next)
                eval.setResult(last);
            lastListenerHead = lastListenerTail = null;
        }
    }

    @Override
    public void finished(Evaluation evaluation){
        finished(evaluation.order, (Boolean)evaluation.getResult());
    }

    private void finished(long order, boolean accept){
        if(accept)
            map.get(order).result = Boolean.TRUE;
        else
            map.remove(order).setPosition(Double.NaN);

        while(!map.isEmpty()){
            LongTreeMap.Entry<PositionalListeners> entry = map.firstEntry();
            if(entry.value.result==Boolean.TRUE){
                position++;
                map.deleteEntry(entry);
                entry.value.setPosition((double)position);
            }else
                return;
        }
        if(lastListenerHead!=null)
            checkLast();
    }

    public void dispose(){
        for(LongTreeMap.Entry<PositionalListeners> entry=map.firstEntry(); entry!=null; entry=entry.next())
            entry.value.evaluation.removeListener(this);
        map.clear();
    }
}

final class PositionalListeners{
    Evaluation evaluation;
    public long order;
    public Boolean result;

    private final PositionalEvaluation posListeners[];
    private int listenerCount;

    PositionalListeners(Evaluation evaluation, int listenerCount){
        posListeners = new PositionalEvaluation[listenerCount];
        order = evaluation.order;
        this.evaluation = evaluation;
    }

    PositionalListeners(long order, Boolean result, int listenerCount){
        posListeners = new PositionalEvaluation[listenerCount];
        this.order = order;
        this.result = result;
    }

    public void addListener(PositionalEvaluation listener){
        posListeners[listenerCount++] = listener;
    }

    public void removeListener(PositionalEvaluation listener){
        for(int i=0, len=listenerCount; i<len; i++){
            if(posListeners[i]==listener){
                posListeners[i] = null;
                return;
            }
        }
    }

    public void setPosition(Double position){
        for(int i=0; i<listenerCount; i++){
            PositionalEvaluation peval = posListeners[i];
            if(peval!=null && !peval.disposed)
                peval.setResult(position);
        }
    }
}