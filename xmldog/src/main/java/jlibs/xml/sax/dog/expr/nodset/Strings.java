/**
 * Copyright 2015 Santhosh Kumar Tekuri
 *
 * The JLibs authors license this file to you under the Apache License,
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
import jlibs.xml.sax.dog.DataType;
import jlibs.xml.sax.dog.NodeType;
import jlibs.xml.sax.dog.expr.Evaluation;
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.expr.Literal;
import jlibs.xml.sax.dog.path.LocationPath;
import jlibs.xml.sax.dog.sniff.Event;

/**
 * @author Santhosh Kumar T
 */
public final class Strings extends LocationExpression{
    public Strings(LocationPath locationPath, DataType resultType, boolean many, boolean first){
        super(locationPath, resultType, many, first);
    }

    @Override
    public final Object getResult(){
        return resultType.defaultValue;
    }

    @Override
    protected final Object getResultItem(Event event){
        switch(event.type()){
            case NodeType.DOCUMENT:
            case NodeType.ELEMENT:
                StringEvaluation stringEvaluation = event.stringEvaluation;
                if(stringEvaluation==null || stringEvaluation.order!=event.order())
                    return event.stringEvaluation = new StringEvaluation(this, event);
                else
                    return new DelegatingEvaluation(this, event.order(), stringEvaluation);
            default:
                if(resultType==DataType.NUMBER || resultType==DataType.NUMBERS){
                    try{
                        return Double.parseDouble(event.value());
                    }catch(NumberFormatException ex){
                        return Double.NaN;
                    }
                }else
                    return event.value();
        }
    }

    @Override
    protected String getName(){
        switch(resultType){
            case STRING:
                return "string";
            case STRINGS:
                return "strings";
            case NUMBER:
                return many ? "sum" : "number";
            case NUMBERS:
                return "numbers";
            default:
                throw new ImpossibleException();
        }
    }

    @Override
    public final Expression simplify(){
        if(locationPath==LocationPath.IMPOSSIBLE)
            return new Literal(resultType.defaultValue, resultType);
        else
            return this;
    }
}

final class DelegatingEvaluation extends Evaluation<Strings>{
    private StringEvaluation delegate;
    protected DelegatingEvaluation(Strings expression, long order, StringEvaluation delegate){
        super(expression, order);
        this.delegate = delegate;
        delegate.addListener(expression, this);
    }

    @Override
    public void start(){}

    private Object result;

    @Override
    public Object getResult(){
        return result;
    }

    @Override
    public void finished(Evaluation evaluation){
        result = evaluation.getResult();
        fireFinished();
    }

    @Override
    protected void dispose(){
        delegate.removeListener(expression, this);
    }
}