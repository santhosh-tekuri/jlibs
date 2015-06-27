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

import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.path.PositionalPredicate;
import jlibs.xml.sax.dog.sniff.Event;

/**
 * @author Santhosh Kumar T
 */
public class PositionTracker{
    public int position;

    private PositionMatches positionMatchesHead;

    public PositionTracker(PositionalPredicate positionPredicate){
        if(positionPredicate!=null){
            PositionMatches positionMatches = positionMatchesHead = new PositionMatches(positionPredicate);
            while((positionPredicate = positionPredicate.next)!=null)
                positionMatches = positionMatches.next = new PositionMatches(positionPredicate);
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

    public void addEvaluation(Event event){
        position++;        
        if(positionMatchesHead!=null){
            PositionMatches positionMatches = positionMatchesHead;
            do{
                positionMatches.addEvaluation(event);
            }while((positionMatches=positionMatches.next)!=null);
        }
    }

    public void startEvaluation(){
        if(positionMatchesHead!=null){
            PositionMatches positionMatches = positionMatchesHead;
            do{
                positionMatches.startEvaluation();
            }while((positionMatches=positionMatches.next)!=null);
        }
    }

    public void expired(){
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
    }
}
