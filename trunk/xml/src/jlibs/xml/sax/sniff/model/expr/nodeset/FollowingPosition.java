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

import jlibs.core.lang.ImpossibleException;
import jlibs.xml.sax.sniff.engine.context.Context;
import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.model.Datatype;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.NotificationListener;
import jlibs.xml.sax.sniff.model.Notifier;
import jlibs.xml.sax.sniff.model.axis.Following;
import jlibs.xml.sax.sniff.model.expr.Expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class FollowingPosition extends ValidatedExpression{
    public FollowingPosition(Node contextNode, Notifier member, Expression predicate){
        super(Datatype.NUMBER, contextNode, member, predicate);
        Following following = ((Node)member).getFollowing();
        following.owner.addNotificationListener(new NotificationListener(){
            @Override
            public void onNotification(Notifier source, Context context, Object result){
                map.put(((Event)result).order(), 0);
            }
        });
    }

    private Map<Long, Integer> map = new HashMap<Long, Integer>();
    private long lastConsumedOrder = -1;

    class MyEvaluation extends DelayedEvaluation{
        @Override
        protected Object getCachedResult(){
            return null;
        }

        @Override
        protected void predicateAccepted(){
            evaluate();
        }

        private void evaluate(){
            List<Double> result = new ArrayList<Double>(map.size());
            for(Map.Entry<Long, Integer> entry: map.entrySet()){
                if(contextIdentity.order>entry.getKey()){
                    int pos = entry.getValue();
                    if(lastConsumedOrder!=contextIdentity.order)
                        pos++;
                    entry.setValue(pos);
                    result.add((double)pos);
                }
            }
            lastConsumedOrder = contextIdentity.order;
            setResult(result);
        }

        long order;
        @Override
        protected void consumeMemberResult(Object result){
            if(result instanceof Event){
                order = ((Event)result).order();
                if(members.size()==1) // has no predicate
                    evaluate();
            }else if(result instanceof Double)
                throw new ImpossibleException();
        }
    }

    @Override
    protected Evaluation createEvaluation(){
        return new MyEvaluation();
    }

    @Override
    public void reset(){
        map.clear();
        lastConsumedOrder = -1;
        super.reset();
    }
}