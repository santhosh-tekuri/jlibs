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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class FollowingSibling extends AxisNode implements Resettable, NotificationListener{
    private Node owner;
    private boolean dontMatch;

    public FollowingSibling(Node owner){
        super(Axis.FOLLOWING_SIBLING);
        this.owner = owner;
        AxisNode axisNode = owner.getConstraintAxis();
        switch(axisNode.type){
            case Axis.INVALID_AXIS:
            case Axis.ATTRIBUTE:
            case Axis.NAMESPACE:
                dontMatch = true;
        }
    }
    
    @Override
    public boolean canBeContext(){
        return true;
    }

    public void attachListeners(){
        if(dontMatch)
            return;
        
        owner.addNotificationListener(this);
        ContextEndListener contextEndListener = new ContextEndListener(){
            @Override
            @SuppressWarnings({"SuspiciousMethodCalls"})
            public void contextEnded(Context context, long order){
                contexts.remove(context);
            }

            @Override
            public int priority(){
                return Integer.MIN_VALUE;
            }

            @Override
            public String toString(){
                return FollowingSibling.this.toString();
            }
        };
        if(owner instanceof Descendant)
            owner.addContextEndListener(contextEndListener);
        owner.parent.addContextEndListener(contextEndListener);
    }

    @Override
    public boolean equivalent(Node node){
        return super.equivalent(node) && this.owner==((FollowingSibling)node).owner;
    }

    private Map<Context.ContextIdentity, Long> contexts = new HashMap<Context.ContextIdentity, Long>();

    @Override
    public void onNotification(Notifier source, Context context, Object result){
        Context.ContextIdentity pi = context.parentIdentity(true);
        if(!contexts.containsKey(pi))
            contexts.put(pi, ((Event)result).order());
    }

    @Override
    @SuppressWarnings({"SuspiciousMethodCalls"})
    public boolean matches(Context context, Event event){
        if(dontMatch)
            return false;

        switch(event.type()){
            case Event.ELEMENT:
            case Event.TEXT:
            case Event.COMMENT:
            case Event.PI:
                Long order = contexts.get(context);
                return order!=null && event.order()>order;
            default:
                return false;
        }
    }

    @Override
    public void reset(){
        contexts.clear();
    }

    @Override
    public String toString(){
        return String.format("%s(%s)::_%d", Axis.lookup(type), owner, depth);
    }
}
