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

package jlibs.xml.sax.sniff.model.expr.nodeset.list;

import jlibs.xml.sax.sniff.model.Datatype;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.Notifier;
import jlibs.xml.sax.sniff.model.expr.Expression;

import java.util.TreeMap;

/**
 * @author Santhosh Kumar T
 */
public class Sum extends NodeList{
    public Sum(Node contextNode, Notifier member, Expression predicate){
        super(Datatype.NUMBER, contextNode, member, predicate);
    }

    class MyEvaluation extends NodeListEvaluation{
        private TreeMap<Integer, Double> map;
        private double d;

        MyEvaluation(){
            if(storeDocumentOrder)
                map = new TreeMap<Integer, Double>();
        }

        @Override
        @SuppressWarnings({"unchecked"})
        protected void consume(Object result){
            if(result instanceof Double){
                d += (Double)result;
                if(Double.isNaN(d))
                    resultPrepared();
            }else{
                TreeMap<Integer, Double> resultMap = (TreeMap<Integer, Double>)result;
                if(resultMap.size()==1 && Double.isNaN(resultMap.firstEntry().getValue())){
                    map = resultMap;
                    resultPrepared();
                }else
                    map.putAll(resultMap);
            }
        }

        @Override
        protected void consume(String str){
            Double number = Datatype.asNumber(str);
            if(storeDocumentOrder){
                int order = ((Expression)members.get(0)).contextIdentityOfLastEvaluation.order;
                if(number.isNaN())
                    map.clear();
                map.put(order, number);
            }else
                d += number;
            
            if(Double.isNaN(number))
                resultPrepared();
        }

        @Override
        protected Object getCachedResult(){
            if(storeDocumentOrder)
                return map;
            else
                return d;
        }
    }

    @Override
    protected Evaluation createEvaluation(){
        return new MyEvaluation();
    }
}