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

package jlibs.xml.sax.sniff.model.expr;

import jlibs.core.lang.ImpossibleException;
import jlibs.xml.sax.sniff.NodeItem;
import jlibs.xml.sax.sniff.model.Datatype;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.Notifier;
import jlibs.xml.sax.sniff.model.expr.nodeset.ValidatedExpression;

import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author Santhosh Kumar T
 */
public class Union extends Function{
    private boolean storeDocumentOrder;

    public Union(Node contextNode, Expression lhs, Expression rhs){
        super(contextNode, lhs.resultType(), lhs.resultType(), rhs.resultType());
        if(lhs.resultType()!=rhs.resultType())
            throw new IllegalArgumentException();
        addMember(lhs);
        addMember(rhs);

        if(lhs instanceof Union)
            ((Union)lhs).storeDocumentOrder = true;
        else
            storeDocumentOrder((ValidatedExpression)lhs);

        if(rhs instanceof Union)
            ((Union)rhs).storeDocumentOrder = true;
        else
            storeDocumentOrder((ValidatedExpression)rhs);
    }

    @Override
    public Datatype resultType(){
        return members.get(0).resultType();
    }

    private void storeDocumentOrder(ValidatedExpression expr){
        while(true){
            expr.storeDocumentOrder = true;
            Notifier member = expr.members.get(0);
            if(member instanceof ValidatedExpression)
                expr = (ValidatedExpression)member;
            else
                return;
        }
    }

    @Override
    @SuppressWarnings({"unchecked"})
    protected Object evaluatePending(Object[] args){
        switch(resultType()){
            case NUMBER:
                for(Object arg: args){
                    TreeMap<Integer, Double> map = (TreeMap<Integer, Double>)arg;
                    if(map!=null && map.size()==1 && map.firstEntry().getValue().isNaN())
                        return map;
                }
                break;
            case BOOLEAN:
                for(Object arg: args){
                    if(arg==Boolean.TRUE)
                        return Boolean.TRUE;
                }
                break;
        }
        return null;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    protected Object evaluate(Object[] args){
        switch(resultType()){
            case STRING:
                Map.Entry<Long, String> result = null;
                for(Object arg: args){
                    TreeMap<Long, String> map = (TreeMap<Long, String>)arg;
                    Map.Entry<Long, String> entry = map.firstEntry();
                    if(result!=null){
                        if(result.getKey()<entry.getKey())
                            continue;
                    }
                    result = entry;
                }
                if(storeDocumentOrder){
                    TreeMap<Long, String> map = new TreeMap<Long, String>();
                    map.put(result.getKey(), result.getValue());
                    return map;
                }else
                    return result.getValue();
            case NUMBER:{
                TreeMap<Long, Double> results = new TreeMap<Long, Double>();
                for(Object arg: args){
                    TreeMap<Long, Double> map = (TreeMap<Long, Double>)arg;
                    results.putAll(map);
                }

                if(storeDocumentOrder)
                    return results;
                
                double d = 0;
                for(Double value: results.values())
                    d += value;
                return d;
            }
            case BOOLEAN:
                for(Object arg: args){
                    if(arg==Boolean.TRUE)
                        return Boolean.TRUE;
                }
                return Boolean.FALSE;
            case NODESET:{
                TreeSet<NodeItem> results = new TreeSet<NodeItem>();
                for(Object arg: args)
                    results.addAll((TreeSet<NodeItem>)arg);
                return results;
            }
            default:
                throw new ImpossibleException();
        }
    }
}
