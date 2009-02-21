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
import jlibs.xml.sax.sniff.engine.context.Context;
import jlibs.xml.sax.sniff.engine.context.ContextEndListener;
import jlibs.xml.sax.sniff.engine.context.ContextListener;
import jlibs.xml.sax.sniff.engine.context.ContextStartListener;
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

        if(node instanceof Resettable)
            root.resettables.add((Resettable)node);
        return node;
    }

    public Node getConstraintRoot(){
        Node node = this;
        while(node.constraintParent!=null)
            node = node.constraintParent;
        return node;
    }

    public AxisNode getConstraintAxis(){
        if(this instanceof AxisNode)
            return (AxisNode)this;

        Node node = this;
        while(node.constraintParent!=null){
            node = node.constraintParent;
            if(node instanceof AxisNode)
                return (AxisNode)node;
        }

        if(node instanceof AxisNode)
            return (AxisNode)node;
        else
            return null;
    }

    public boolean isConstraintAncestor(Node ancestor){
        Node node = this;
        while(node.constraintParent!=null){
            node = node.constraintParent;
            if(node==ancestor)
                return true;
        }
        return false;
    }

    /*-------------------------------------------------[ Matches ]---------------------------------------------------*/
    
    public boolean canConsume(){
        return false;
    }
    
    public boolean consumable(Event event){
        return false;
    }

    public boolean matches(Context context, Event event){
        return false;
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
                if(elem.listeners!=null){
                    for(NotificationListener listener: elem.listeners)
                        str += "\n   ### "+listener+" ";
                }
                if(elem.contextStartListeners!=null){
                    for(ContextStartListener listener: elem.contextStartListeners)
                        str += "\n   {{{ "+listener+" ";
                }
                if(elem.contextEndListeners!=null){
                    for(ContextEndListener listener: elem.contextEndListeners)
                        str += "\n   }}} "+listener+" ";
                }
                return str;
            }
        });
    }

    /*-------------------------------------------------[ Observers ]---------------------------------------------------*/

    private List<NotificationListener> listeners;

    @Override
    public void addNotificationListener(NotificationListener listener){
        if(listeners==null)
            listeners = new ArrayList<NotificationListener>();
        listeners.add(listener);
    }
    
    @Override
    public void notify(Context context, Object event){
        if(listeners!=null){
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
    
    /*-------------------------------------------------[ ContextStartListeners ]---------------------------------------------------*/

    private List<ContextStartListener> contextStartListeners;
    private static Comparator<ContextStartListener> startComparator = new Comparator<ContextStartListener>(){
        @Override
        public int compare(ContextStartListener listener1, ContextStartListener listener2){
            return listener2.priority() - listener1.priority();
        }
    };

    private boolean startListenersSorted;

    public void addContextStartListener(ContextStartListener listener){
        if(contextStartListeners==null)
            contextStartListeners = new ArrayList<ContextStartListener>();
        
        contextStartListeners.add(listener);
        startListenersSorted = contextStartListeners.size()<2;
    }

    public void removeContextStartListener(ContextStartListener listener){
        contextStartListeners.remove(listener);
        
        if(contextStartListeners.size()==0)
            contextStartListeners = null;
    }

    public void contextStarted(Context context, Event event){
        if(contextStartListeners!=null){
            if(debug){
                debugger.println("contextStarted(%s)",this);
                debugger.indent++;
            }

            if(!startListenersSorted){
                Collections.sort(contextStartListeners, startComparator);
                startListenersSorted = true;
            }

            for(ContextStartListener listener: contextStartListeners)
                listener.contextStarted(context, event);

            if(debug)
                debugger.indent--;
        }
        notify(context, event);
    }

    /*-------------------------------------------------[ ContextEndListeners ]---------------------------------------------------*/
    
    private List<ContextEndListener> contextEndListeners;
    private static Comparator<ContextEndListener> endComparator = new Comparator<ContextEndListener>(){
        @Override
        public int compare(ContextEndListener listener1, ContextEndListener listener2){
            return listener1.priority() - listener2.priority();
        }
    };

    private boolean endListenersSorted;
    public void addContextEndListener(ContextEndListener listener){
        if(contextEndListeners==null)
            contextEndListeners = new ArrayList<ContextEndListener>();
        
        contextEndListeners.add(0, listener);
        endListenersSorted = contextEndListeners.size()<2;
    }

    public void removeContextEndListener(ContextEndListener listener){
        contextEndListeners.remove(listener);

        if(contextEndListeners.size()==0)
            contextEndListeners = null;
    }

    public void contextEnded(Context context, long order){
        if(contextEndListeners!=null){
            if(debug){
                debugger.println("contextEnded(%s)",this);
                debugger.indent++;
            }

            if(!endListenersSorted){
                Collections.sort(contextEndListeners, endComparator);
                endListenersSorted = true;
            }

            for(ContextEndListener listener: contextEndListeners)
                listener.contextEnded(context, order);

            if(debug)
                debugger.indent--;
        }
    }

    /*-------------------------------------------------[ ContextListeners ]---------------------------------------------------*/

    public void addContextListener(ContextListener listener){
        addContextStartListener(listener);
        addContextEndListener(listener);
    }

    public void removeContextListener(ContextListener listener){
        removeContextStartListener(listener);
        removeContextEndListener(listener);
    }

    public void notifyContext(Context context, Event event){
        Context childContext = context.childContext(this);
        contextStarted(childContext, event);
        contextEnded(childContext, event.order());
    }
}

