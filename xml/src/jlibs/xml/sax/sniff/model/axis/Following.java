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

package jlibs.xml.sax.sniff.model.axis;

import jlibs.xml.sax.sniff.engine.context.Context;
import jlibs.xml.sax.sniff.engine.context.ContextEndListener;
import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.model.*;
import org.jaxen.saxpath.Axis;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class Following extends AxisNode implements Resettable, NotificationListener{
    private Node owner;

    public Following(Node owner){
        super(Axis.FOLLOWING);
        this.owner = owner;
    }

    @Override
    public boolean canBeContext(){
        return true;
    }

    public void attachListeners(){
        owner.addNotificationListener(this);
        owner.addContextEndListener(new ContextEndListener(){
            @Override
            public void contextEnded(Context context, long order){
                Context.ContextIdentity pid = context.parentIdentity(false);
                Match match = matches.get(pid);
                if(match!=null){
                    match.end = order;
                    minMatchedOrder = Math.min(minMatchedOrder, order);
                }
            }

            @Override
            public int priority(){
                return Integer.MIN_VALUE;
            }
        });
    }

    @Override
    public boolean equivalent(Node node){
        return super.equivalent(node) && this.owner==((Following)node).owner;
    }

    public Map<Context.ContextIdentity, Match> matches = new LinkedHashMap<Context.ContextIdentity, Match>();
    private class Match{
        long start;
        long end = -1; // -1 means unknown

        private Match(Event event){
            start = event.order();
        }
    }
    private long minMatchedOrder = Long.MAX_VALUE;

    @Override
    public void onNotification(Notifier source, Context context, Object result){
        Context.ContextIdentity pi = context.parentIdentity(true);
        if(!matches.containsKey(pi))
            matches.put(pi, new Match((Event)result));
    }

    public boolean matchesWith(Context.ContextIdentity identity, Event event){
        for(Map.Entry<Context.ContextIdentity, Match> entry: matches.entrySet()){
            Match match = entry.getValue();
            if(match.start>identity.order)
                return match.end!=-1 && event.order()>match.end;
        }
        return false;
    }

    @Override
    @SuppressWarnings({"SuspiciousMethodCalls", "EqualsBetweenInconvertibleTypes", "RedundantIfStatement"})
    public boolean matches(Context context, Event event){
        if(matches.size()==0)
            return false;

        switch(event.type()){
            case Event.ELEMENT:
            case Event.TEXT:
            case Event.COMMENT:
            case Event.PI:
                return event.order()>minMatchedOrder;
            default:
                return false;
        }
    }

    @Override
    public void reset(){
        matches.clear();
        minMatchedOrder = Long.MAX_VALUE;
    }

    @Override
    public boolean canConsume(){
        return true;
    }

    @Override
    public boolean consumable(Event event){
        switch(event.type()){
            case Event.DOCUMENT:
            case Event.ELEMENT:
            case Event.TEXT:
            case Event.COMMENT:
            case Event.PI:
                return true;
            default:
                return false;
        }
    }

    @Override
    public String toString(){
        return String.format("%s(%s)::_%d", Axis.lookup(type), owner, depth);
    }
}