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

package jlibs.xml.sax.sniff.model;

import jlibs.core.graph.*;
import jlibs.core.graph.sequences.ConcatSequence;
import jlibs.core.graph.sequences.IterableSequence;
import jlibs.core.graph.walkers.PreorderWalker;
import jlibs.core.util.ReverseComparator;
import jlibs.xml.sax.sniff.engine.context.Context;
import jlibs.xml.sax.sniff.events.Event;
import org.jaxen.saxpath.Axis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public abstract class Node extends Notifier{
    public Root root;
    public Node parent;
    public Node constraintParent;

    public boolean hasAttibuteChild;
    public boolean hasNamespaceChild;

    @Override
    public Datatype resultType(){
        return Datatype.NODESET;
    }
    
    public boolean canBeContext(){
        return false;
    }
    
    public abstract boolean equivalent(Node node);

    /*-------------------------------------------------[ Children ]---------------------------------------------------*/
    
    private List<AxisNode> children = new ArrayList<AxisNode>();

    public Iterable<AxisNode> children(){
        return children;
    }

    @SuppressWarnings({"unchecked"})
    public <N extends AxisNode> N addChild(N axisNode){
        for(AxisNode child: children()){
            if(child.equivalent(axisNode))
                return (N)child;
        }

        axisNode.depth = depth+1;
        children.add(axisNode);
        axisNode.parent = this;
        axisNode.root = root;
        axisNode.hits.totalHits = root.hits.totalHits;

        if(axisNode.type==Axis.ATTRIBUTE)
            hasAttibuteChild = true;
        else if(axisNode.type==Axis.NAMESPACE)
            hasNamespaceChild = true;

        return axisNode;
    }

    /*-------------------------------------------------[ Constraints ]---------------------------------------------------*/

    private List<Node> constraints = new ArrayList<Node>();

    public Iterable<Node> constraints(){
        return constraints;
    }

    @SuppressWarnings({"unchecked"})
    public <N extends Node> N addConstraint(N node){
        for(Node constraint: constraints()){
            if(constraint.equivalent(node))
                return (N)constraint;
        }

        node.depth = depth;
        constraints.add(node);
        node.constraintParent = this;
        node.parent = this.parent;
        node.root = root;
        node.hits.totalHits = root.hits.totalHits;
        node.reset();
        return node;
    }

    public Node getConstraintRoot(){
        Node node = this;
        while(node.constraintParent!=null)
            node = node.constraintParent;
        return node;
    }

    /*-------------------------------------------------[ Matches ]---------------------------------------------------*/
    
    public boolean consumable(Event event){
        return false;
    }

    public boolean matches(Context context, Event event){
        return false;
    }
    
    /*-------------------------------------------------[ Reset ]---------------------------------------------------*/

    public void reset(){
        hits.reset();
        for(Node child: children())
            child.reset();
        for(Node constraint: constraints())
            constraint.reset();
    }
    
    /*-------------------------------------------------[ Debug ]---------------------------------------------------*/
    
    public void print(){
        Navigator<Node> navigator = new Navigator<Node>(){
            @Override
            public Sequence<? extends Node> children(Node elem){
                return new ConcatSequence<Node>(new IterableSequence<Node>(elem.constraints), new IterableSequence<AxisNode>(elem.children));
            }
        };
        Walker<Node> walker = new PreorderWalker<Node>(this, navigator);
        WalkerUtil.print(walker, new Visitor<Node, String>(){
            @Override
            public String visit(Node elem){
                String str = elem.toString() + "_"+elem.depth;
                if(elem.listeners.size()>0){
                    for(NotificationListener listener: elem.listeners)
                        str += "\n   ### "+listener+" ";
                }
                if(elem.contextListeners.size()>0){
                    for(ContextListener listener: elem.contextListeners)
                        str += "\n   @@@ "+listener+" ";
                }
                return str;
            }
        });
    }

    /*-------------------------------------------------[ Observers ]---------------------------------------------------*/

    private List<NotificationListener> listeners = new ArrayList<NotificationListener>();

    @Override
    public void addNotificationListener(NotificationListener listener){
        listeners.add(listener);
    }
    
    @Override
    public void notify(Context context, Object event){
        if(listeners.size()>0){
            if(debug){
                debugger.println("notifyListeners("+this+")");
                debugger.indent++;
            }
            for(NotificationListener listener: listeners)
                listener.onNotification(this, context, event);
            if(debug)
                debugger.indent--;
        }
    }
    
    /*-------------------------------------------------[ ContextListeners ]---------------------------------------------------*/

    protected List<ContextListener> contextListeners = new ArrayList<ContextListener>();
    protected List<ContextListener> _contextListeners = new ArrayList<ContextListener>();

    private Comparator<ContextListener> endComparator = new Comparator<ContextListener>(){
        @Override
        public int compare(ContextListener listener1, ContextListener listener2){
            return listener1.priority() - listener2.priority();
        }
    };

    public void addContextListener(ContextListener listener){
        contextListeners.add(listener);
        _contextListeners.add(0, listener);

        if(listener instanceof Notifier)
            ((Notifier)listener).depth = this.depth;
    }

    public void removeContextListener(ContextListener listener){
        contextListeners.remove(listener);
    }

    private boolean sorted;
    public void contextStarted(Context context, Event event){
        if(contextListeners.size()>0){
            if(debug){
                debugger.println("contextStarted(%s)",this);
                debugger.indent++;
            }

            if(!sorted){
                Collections.sort(contextListeners, new ReverseComparator<ContextListener>(endComparator));
                sorted = true;
            }

            for(ContextListener listener: contextListeners)
                listener.contextStarted(context, event);
            if(debug)
                debugger.indent--;
        }
        notify(context, event);
    }

    private boolean _sorted;
    public void contextEnded(Context context){
        if(contextListeners.size()==0)
            return;

        if(debug){
            debugger.println("contextEnded(%s)",this);
            debugger.indent++;
        }
        if(!_sorted){
            Collections.sort(_contextListeners, endComparator);
            _sorted = true;
        }

        for(ContextListener listener: _contextListeners)
            listener.contextEnded(context);
        if(debug)
            debugger.indent--;
    }

    public void notifyContext(Context context, Event event){
        Context childContext = context.childContext(this);
        contextStarted(childContext, event);
        contextEnded(childContext);
    }
}

