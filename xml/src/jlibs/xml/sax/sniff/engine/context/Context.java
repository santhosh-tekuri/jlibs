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
import jlibs.xml.sax.sniff.model.DocumentNode;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.Root;

import java.util.ArrayList;
import java.util.List;

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

    public ContextIdentity parentIdentity(boolean justCreated){
        if(depth==0){
            if(justCreated)
                return parent.identity();
            else{
                if(parent.depth<0)
                    return new ContextIdentity(parent, parent.depth+1);
                else if(parent.depth>0)
                    return new ContextIdentity(parent, parent.depth-1);
                else
                    return new ContextIdentity(parent, parent.depth);
            }
        }else if(depth<0)
            return new ContextIdentity(this, depth+1);
        else
            return new ContextIdentity(this, depth-1);
    }

    public static final class ContextIdentity{
        Context context;
        int depth;
        List<Integer> depths = new ArrayList<Integer>();
//        LinkedHashMap<Context, Integer> map = new LinkedHashMap<Context, Integer>();
        public long order;

        ContextIdentity(Context context){
            this(context, context.depth);
        }

        ContextIdentity(Context context, int depth){
            this.context = context;
            this.depth = depth;
            order = context.order;

            int diff = context.depth-depth;
            while(context!=null){
                depths.add(context.depth-diff);
//                map.put(context, context.depth);
                context = context.parent;
            }
        }

        public ContextIdentity parentIdentity(){
            if(depth==0)
                return new ContextIdentity(context.parent, depths.get(1));
            else if(depth<0)
                return new ContextIdentity(context, depth+1);
            else
                return new ContextIdentity(context, depth-1);
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
                if(c==null)
                    return false;
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

        public boolean isParent(Context c){
            if(c.node instanceof Root || c.node instanceof DocumentNode)
                return true;
            
            int i = 0;
            Context c1 = this.context;

            while(c1!=null){
                c1 = c1.parent;
                i++;

                if(c1.node.depth<c.node.depth)
                    return false;
                if(c1==c && depths.get(i)==c.depth)
                    return true;
            }
            return false;
        }

        public boolean isParent(ContextIdentity cid){
            if(cid.context.node instanceof Root || cid.context.node instanceof DocumentNode)
                return true;

            int i = 0;
            Context c1 = this.context;

            while(c1!=null){
                c1 = c1.parent;
                i++;

                if(c1.node.depth<cid.context.node.depth)
                    return false;
                if(c1==cid.context && depths.get(i)==cid.depth)
                    return true;
            }
            return false;
        }
    }
}
