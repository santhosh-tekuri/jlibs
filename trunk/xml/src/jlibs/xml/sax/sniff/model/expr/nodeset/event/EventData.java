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

package jlibs.xml.sax.sniff.model.expr.nodeset.event;

import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.model.Datatype;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.Notifier;
import jlibs.xml.sax.sniff.model.expr.Expression;
import jlibs.xml.sax.sniff.model.expr.nodeset.ValidatedExpression;

import java.util.Collections;

/**
 * @author Santhosh Kumar T
 */
public abstract class EventData extends ValidatedExpression{
    public EventData(Node contextNode, Notifier member, Expression predicate){
        super(Datatype.STRING, contextNode, member, predicate);
    }

    protected abstract String getData(Event event);

    class MyEvaluation extends DelayedEvaluation{
        private long order;
        private String data;

        @Override
        protected Object getCachedResult(){
            if(storeDocumentOrder)
                return Collections.singletonMap(order, data);
            else
                return data;
        }

        @Override
        protected void consumeMemberResult(Object result){
            if(result instanceof Event){
                Event event = (Event)result;
                order = event.order();
                data = getData(event);
                resultPrepared();
            }else{
                long _order = ((Expression)members.get(0)).contextIdentityOfLastEvaluation.order;
                if(data!=null){
                    if(_order>order)
                        return;
                }
                order = _order;
                this.data = (String)result;
            }
        }
    }

    @Override
    protected Evaluation createEvaluation(){
        return new MyEvaluation();
    }
}