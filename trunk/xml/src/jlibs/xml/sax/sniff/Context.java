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

package jlibs.xml.sax.sniff;

import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.model.Node;

/**
 * @author Santhosh Kumar T
 */
public class Context implements Debuggable{
    public Context parent;
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


    public void match(Event event, Contexts contexts, XPathResults results){
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
                    if(results.hit(this, event, child)){
                        if(changeContext)
                            contexts.add(childContext(child));
                        matchConstraints(child, event, contexts, results);
                    }
                }
            }
            if(node.consumable(event)){
                if(results.hit(this, event, node)){
                    if(changeContext){
                        depth--;
                        contexts.add(this);
                    }
                    matchConstraints(node, event, contexts, results);
                }
            }else{
                if(changeContext && contexts.markSize()==0){
                    depth++;
                    contexts.add(this);
                }
            }
        }

        //noinspection ConstantConditions
        if(debug && changeContext)
            contexts.printNext(message);
    }

    private void matchConstraints(Node child, Event event, Contexts contexts, XPathResults results){
        boolean changeContext = event.hasChildren();

        for(Node constraint: child.constraints()){
            if(constraint.matches(event)){
                if(results.hit(this, event, constraint)){
                    if(changeContext)
                        contexts.add(childContext(constraint));
                    matchConstraints(constraint, event, contexts, results);
                }
            }
        }
    }

    public Context endElement(XPathResults results){
        if(depth==0){
            results.clearHitCounts(this);
            results.clearPredicateCache(depth, node);
            return parentContext();
        }else{
            if(depth>0){
//                    results.clearHitCounts(depth, node);
                depth--;
            }else{
                results.clearHitCounts(this);
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
}
