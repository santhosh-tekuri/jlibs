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
import jlibs.xml.sax.sniff.model.Node;

import java.util.LinkedHashMap;

/**
 * @author Santhosh Kumar T
 */
public class Context implements Debuggable{
    public int order;
    public Context parent, constraintParent;
    public Node node;
    public int depth;
//        int parentDepths[];

    public Context(Node root){
        node = root;
//            parentDepths = new int[0];
    }

    public Context childContext(Node child){
        Context context = new Context(child);
        context.parent = this;

//            context.parentDepths = new int[parentDepths.length+1];
//            System.arraycopy(parentDepths, 0, context.parentDepths, 0, parentDepths.length);
//            context.parentDepths[context.parentDepths.length-1] = depth;

        return context;
    }

    public Context parentContext(){
        return parent;

//            Context parent = new Context();
//            parent.node = node.parent;
//            parent.depth = parentDepths[parentDepths.length-1];
//
//            parent.parentDepths = new int[parentDepths.length-1];
//            System.arraycopy(parentDepths, 0, parent.parentDepths, 0, parent.parentDepths.length);
//            return parent;
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
                if(changeContext){
                    depth--;
                    contexts.add(this);
                    node.contextStarted(this, event);
                    order = event.order();
                }else
                    node.notifyContext(this, event);
                matchConstraints(node, event, contexts, null);
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

    public Context endElement(){
        if(depth==0){
            node.contextEnded(this);
            node.endingContext(this);
            return parentContext();
        }else{
            if(depth>0)
                depth--;
            else{
                node.contextEnded(this);
                node.endingContext(this);
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

    public static final class ContextIdentity{
        Context context;
        int depth;
        int depths[];
        LinkedHashMap<Context, Integer> map = new LinkedHashMap<Context, Integer>();
        public int order;

        ContextIdentity(Context context){
            this.context = context;
            depth = context.depth;
            order = context.order;

            while(context!=null){
                map.put(context, context.depth);
                context = context.parent;
            }
        }

        @Override
        public boolean equals(Object obj){
            if(obj instanceof ContextIdentity){
                ContextIdentity that = (ContextIdentity)obj;
                return this.context==that.context && this.depth==that.depth;
            }else if(obj instanceof Context){
                Context that = (Context)obj;
                return this.context==that && this.depth==that.depth;
            }else
                return false;
        }

        @Override
        public int hashCode(){
            return context.hashCode();
        }

        @Override
        public String toString(){
            return context+"["+depth+']';
        }

        public boolean isChild(Context c){
            if(c.node.depth<this.context.node.depth)
                return false;
            
            if(this.context==c)
                return Math.abs(this.depth)<Math.abs(c.depth);

            while(c.node.depth>this.context.node.depth)
                c = c.parent;

            if(c.node==this.context.node)
                return this.context==c;
            
            Node node = c.node;
            while(node!=null){
                if(node==this.context.node)
                    return this.context==c;
                node = node.constraintParent;
                c = c.constraintParent;
            }

            return false;
        }

        public int getDepthTo(Context c){
            int depth = 0;
            while(c!=null){
                if(this.context==c){
                    depth += Math.abs(c.depth)-Math.abs(this.depth);
                    return depth;
                }else{
                    depth += Math.abs(c.depth);
                    c = c.parent;
                }
            }
            return -1;
        }
    }
}
