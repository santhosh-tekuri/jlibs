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

import jlibs.core.graph.Navigator;
import jlibs.core.graph.Sequence;
import jlibs.core.graph.Walker;
import jlibs.core.graph.WalkerUtil;
import jlibs.core.graph.sequences.ConcatSequence;
import jlibs.core.graph.sequences.IterableSequence;
import jlibs.core.graph.walkers.PreorderWalker;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public abstract class Node{
    public boolean userGiven;
    
    Root root;
    public Node parent;
    public List<AxisNode> children = new ArrayList<AxisNode>();
    public List<Node> constraints = new ArrayList<Node>();

    protected Node(){}
    public Node(Node node){
        this.root = node.root;

        if(this instanceof AxisNode){
            this.parent = node;
            node.children.add((AxisNode)this);
        }else{
            this.parent = node.parent;
            node.constraints.add(this);
        }
    }

    public boolean consumable(){
        return false;
    }
    
    public boolean matchesElement(String uri, String name, int position){
        return false;
    }

    public boolean matchesAttribute(String uri, String name, String value){
        return false;
    }

    public boolean matchesText(String text){
        return false;
    }

    public void print(){
        Navigator<Node> navigator = new Navigator<Node>(){
            @Override
            public Sequence<? extends Node> children(Node elem){
                return new ConcatSequence<Node>(new IterableSequence<Node>(elem.constraints), new IterableSequence<AxisNode>(elem.children));
            }
        };
        Walker<Node> walker = new PreorderWalker<Node>(this, navigator);
        WalkerUtil.print(walker, null);
    }
}
