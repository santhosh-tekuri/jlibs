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
import jlibs.xml.sax.sniff.Context;
import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.model.computed.ComputedResults;
import jlibs.xml.sax.sniff.model.computed.FilteredNodeSet;
import org.jaxen.saxpath.Axis;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public abstract class Node extends UserResults{
    public Root root;
    public Node parent;
    public Node constraintParent;

    public boolean hasAttibuteChild;

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

        children.add(axisNode);
        axisNode.parent = this;
        axisNode.root = root;
        axisNode.hits.totalHits = root.hits.totalHits;

        if(axisNode.type==Axis.ATTRIBUTE)
            hasAttibuteChild = true;

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
        constraints.add(node);
        node.constraintParent = this;
        node.parent = this.parent;
        node.root = root;
        node.hits.totalHits = root.hits.totalHits;
        node.reset();
        return node;
    }

    /*-------------------------------------------------[ Matches ]---------------------------------------------------*/
    
    public boolean consumable(Event event){
        return false;
    }

    public boolean matches(Event event){
        return false;
    }
    
    /*-------------------------------------------------[ Requires ]---------------------------------------------------*/

    public boolean resultInteresed(){
        return userGiven || listeners.size()>0 || observers.size()>0;
    }

    /*-------------------------------------------------[ Hit ]---------------------------------------------------*/

    public boolean hit(Context context, Event event){
        if(userGiven)
            addResult(event.order(), event.getResult());
        return true;
    }
    
    /*-------------------------------------------------[ On Context End ]---------------------------------------------------*/

    public void endingContext(Context context){
        if(context.depth==0){
            for(FilteredNodeSet observer: cleanupObservers())
                observer.endingContext(context);
        }
        
        for(Node child: context.node.children())
            clearHitCounts(context, child);
    }

    private void clearHitCounts(Context context, Node node){
        for(Node constraint: node.constraints()){
            if(constraint instanceof Position){
                Position position = (Position)constraint;
                position.clearHitCount(context);
            }
            clearHitCounts(context, constraint);
        }
    }

    /*-------------------------------------------------[ Reset ]---------------------------------------------------*/

    public void reset(){
        super.reset();
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
                String str = elem.toString();
                if(elem.userGiven)
                    str += " --> userGiven";
                if(elem.observers.size()>0){
                    str += " ==>";
                    for(ComputedResults observer: elem.observers())
                        str += observer+" ";
                }
                if(elem.cleanupObservers.size()>0){
                    str += " -->";
                    for(FilteredNodeSet observer: elem.cleanupObservers())
                        str += observer+" ";
                }
                return str;
            }
        });
    }
}
