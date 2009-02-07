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
import jlibs.xml.sax.sniff.model.Position;

import java.util.ArrayList;

/**
 * @author Santhosh Kumar T
 */
public class Context implements Debuggable{
    public Context parent;
    public ArrayList<Context> childContexts = new ArrayList<Context>();
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
        childContexts.add(context);

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


    @SuppressWarnings({"SimplifiableIfStatement"})
    private boolean hit(Event event, Node node){
        if(node instanceof Position){
            Position position = (Position)node;
            if(!position.hit(this))
                return false;
        }

        if(node.resultInteresed())
            return node.hit(this, event);
        else
            return true;
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
                if(child.matches(event)){
                    if(hit(event, child)){
                        if(changeContext){
                            Context childContext = childContext(child);
                            contexts.add(childContext);
                            child.contextStarted(event);
                        }else
                            child.notifyContext(event);

                        matchConstraints(child, event, contexts);
                    }
                }
            }
            if(node.consumable(event)){
                if(hit(event, node)){
                    if(changeContext){
                        depth--;
                        contexts.add(this);
                        node.contextStarted(event);
                    }else
                        node.notifyContext(event);
                    matchConstraints(node, event, contexts);
                }
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

    private void matchConstraints(Node child, Event event, Contexts contexts){
        boolean changeContext = event.hasChildren();

        for(Node constraint: child.constraints()){
            if(constraint.matches(event)){
                if(hit(event, constraint)){
                    if(changeContext){
                        Context childContext = childContext(constraint);
                        contexts.add(childContext);
                        constraint.contextStarted(event);
                    }else{
                        constraint.notifyContext(event);
                    }
                    matchConstraints(constraint, event, contexts);
                }
            }
        }
    }

    public Context endElement(){
        if(depth==0){
            node.contextEnded();
            node.endingContext(this);
            return parentContext();
        }else{
            if(depth>0)
                depth--;
            else{
                node.contextEnded();
                node.endingContext(this);
                depth++;
            }

            return this;
        }
    }

    @Override
    public int hashCode(){
        return depth + node.hashCode();
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof Context){
            Context that = (Context)obj;
            return this.depth==that.depth && this.node==that.node;
        }else
            return false;
    }

    @Override
    public String toString(){
        return "["+depth+"] "+node;
    }

    /*-------------------------------------------------[ Identity ]---------------------------------------------------*/

    public Object identity(){
        return new ContextIdentity(this);
    }

    static final class ContextIdentity{
        Context context;
        int depth;

        ContextIdentity(Context context){
            this.context = context;
            depth = context.depth;
        }

        @Override
        public boolean equals(Object obj){
            if(obj instanceof ContextIdentity){
                ContextIdentity that = (ContextIdentity)obj;
                return this.context==that.context && this.depth==that.depth;
            }else
                return false;
        }

        @Override
        public int hashCode(){
            return System.identityHashCode(context)+depth;
        }

        @Override
        public String toString(){
            return context+"["+depth+']';
        }
    }
}
