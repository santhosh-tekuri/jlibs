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

package jlibs.xml.sax.sniff.model.expr.nodeset;

import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.model.Datatype;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.Notifier;
import jlibs.xml.sax.sniff.model.expr.Expression;

import java.util.TreeMap;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class Count extends ValidatedExpression{
    public Count(Node contextNode, Notifier member, Expression predicate){
        super(Datatype.NUMBER, contextNode, member, predicate);
    }

    class MyEvaluation extends DelayedEvaluation{
        private TreeMap<Integer, Double> map;
        private double count;

        MyEvaluation(){
            if(storeDocumentOrder)
                map = new TreeMap<Integer, Double>();
        }

        @Override
        protected Object getCachedResult(){
            if(storeDocumentOrder)
                return map;
            else
                return count;
        }

        @Override
        @SuppressWarnings({"unchecked"})
        protected void consumeMemberResult(Object result){
            if(result instanceof Event){
                if(storeDocumentOrder)
                    map.put(((Event)result).order(), 1d);
                else
                    count++;
            }else if(result instanceof Double)
                count += (Double)result;
            else if(result instanceof TreeMap)
                map.putAll((Map<Integer,Double>)result);
        }
    }

    @Override
    protected Evaluation createEvaluation(){
        return new MyEvaluation();
    }
}
