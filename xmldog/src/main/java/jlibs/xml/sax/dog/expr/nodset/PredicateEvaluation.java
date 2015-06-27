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

import jlibs.xml.sax.dog.Scope;
import jlibs.xml.sax.dog.expr.Evaluation;
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.expr.LinkableEvaluation;
import jlibs.xml.sax.dog.sniff.Event;

/**
 * @author Santhosh Kumar T
 */
public final class PredicateEvaluation extends LinkableEvaluation<Expression>{
    private final Event event;
    private Object resultItem;
    private final Expression predicate;
    private final Evaluation booleanEvaluation;

    public PredicateEvaluation(Expression expression, long order, Object resultItem, Event event, Expression predicate, Evaluation booleanEvaluation){
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
