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

package jlibs.xml.sax.sniff.engine.context;

import jlibs.xml.sax.sniff.Debuggable;
import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.model.AxisNode;
import jlibs.xml.sax.sniff.model.Node;

/**
 * @author Santhosh Kumar T
 */
public class Context implements Debuggable{
    public long order;
    public Context parent, constraintParent;
    public Node node;
    public int depth;

    public Context(Node root){
        node = root;
    }

    public Context childContext(Node child){
        Context context = new Context(child);
        context.parent = this;
        return context;
    }

    public Context parentContext(){
        return parent;
    }

    public void match(Event event, Contexts contexts){
        boolean changeContext = event.hasChildren();

        @SuppressWarnings({"UnusedAssignment"})
        String message = toString();

        contexts.mark();

        if(depth>0){
            if(changeContext){
                depth++;
                contexts.add(this);
            }
        }else{
            for(Node child: node.children()){
                if(child.matches(this, event)){
                    if(changeContext){
                        Context childContext = childContext(child);
                        childContext.order = event.order();
                        contexts.add(childContext);
                        child.contextStarted(childContext, event);
                    }else
                        child.notifyContext(this, event);

                    matchConstraints(child, event, contexts, null);
                }
            }
            if(node.consumable(event)){
                matchConstraints(node, event, contexts, null);
                if(changeContext){
                    depth--;
                    contexts.add(this);
                    node.contextStarted(this, event);
                    order = event.order();
                }else
                    node.notifyContext(this, event);
            }else{
                if(changeContext && contexts.markSize()==0){
                    depth++;
                    contexts.add(this);
                }
            }
        }

        //noinspection ConstantConditions,PointlessBooleanExpression
        if(debug && changeContext)
            contexts.printNext(message);
    }

    private void matchConstraints(Node child, Event event, Contexts contexts, Context constraintParent){
        boolean changeContext = event.hasChildren();

        if(child instanceof AxisNode){
            for(Node constraint: ((AxisNode)child).descendantFollowings){
                if(constraint.matches(this, event)){
                    Context childContext = null;
                    if(changeContext){
                        childContext = childContext(constraint);
                        childContext.constraintParent = constraintParent;
                        contexts.add(childContext);
                        childContext.order = event.order();
                        constraint.contextStarted(childContext, event);
                    }else
                        constraint.notifyContext(this, event);
                    matchConstraints(constraint, event, contexts, childContext);
                }
            }
        }

        for(Node constraint: child.constraints()){
            if(constraint.matches(this, event)){
                Context childContext = null;
                if(changeContext){
                    childContext = childContext(constraint);
                    childContext.constraintParent = constraintParent;
                    contexts.add(childContext);
                    childContext.order = event.order();
                    constraint.contextStarted(childContext, event);
                }else
                    constraint.notifyContext(this, event);
                matchConstraints(constraint, event, contexts, childContext);
            }
        }
    }

    public Context endElement(long order){
        if(depth==0){
            node.contextEnded(this, order);
            return parentContext();
        }else{
            if(depth>0)
                depth--;
            else{
                node.contextEnded(this, order);
                depth++;
            }

            return this;
        }
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof ContextIdentity)
            return obj.equals(this);
        else
            return super.equals(obj);
    }

    @Override
    public String toString(){
        return "["+depth+"] "+node+"@"+System.identityHashCode(this)+"@";
    }

    /*-------------------------------------------------[ Identity ]---------------------------------------------------*/

    public ContextIdentity identity(){
        return new ContextIdentity(this);
    }

    // todo[Performance]:
    //      change the unused argument justCreated to lightVersion. If
    //      lightVersion is true, then during ContextIdentity creation
    //      don't need to populate ContextIdentity.depths
    public ContextIdentity parentIdentity(boolean justCreated){
        if(depth==0)
            return parent.identity();
        else if(depth<0)
            return new ContextIdentity(this, depth+1);
        else
            return new ContextIdentity(this, depth-1);
    }
}
