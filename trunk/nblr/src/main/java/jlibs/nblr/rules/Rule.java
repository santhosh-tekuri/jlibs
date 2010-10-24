/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T <santhosh.tekuri@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.nblr.rules;

import jlibs.core.lang.StringUtil;
import jlibs.core.util.CollectionUtil;
import jlibs.nblr.matchers.Any;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Santhosh Kumar T
 */
public class Rule{
    public int id;
    public String name;
    public Node node;

    public ArrayList<Node> nodes(){
        ArrayList<Node> nodes = new ArrayList<Node>();
        ArrayList<Edge> edges = new ArrayList<Edge>();
        computeIDS(nodes, edges, node);
        return nodes;
    }

    public Node nodeWithName(String name){
        if(name==null)
            return node;
        for(Node node: nodes()){
            if(name.equals(node.name))
                return node;
        }
        return null;
    }
    
    public ArrayList<Edge> edges(){
        ArrayList<Node> nodes = new ArrayList<Node>();
        ArrayList<Edge> edges = new ArrayList<Edge>();
        computeIDS(nodes, edges, node);
        return edges;
    }

    public void computeIDS(){
        ArrayList<Node> nodes = new ArrayList<Node>();
        ArrayList<Edge> edges = new ArrayList<Edge>();
        computeIDS(nodes, edges, node);
    }

    public void computeIDS(List<Node> visited, List<Edge> edges, Node node){
        if(!visited.contains(node)){
            node.id = visited.size();
            visited.add(node);
            for(Edge edge: node.outgoing){
                edges.add(edge);
                computeIDS(visited, edges, edge.target);
            }
        }
    }

    public boolean contains(Node node){
        ArrayList<Node> nodes = new ArrayList<Node>();
        ArrayList<Edge> edges = new ArrayList<Edge>();
        computeIDS(nodes, edges, this.node);
        return nodes.contains(node);
    }

    public Set<Node> states(){
        Set<Node> states = new LinkedHashSet<Node>();
        for(Node node: nodes()){
            if(node==this.node || node.name!=null)
                states.add(node);
            else{
                for(Edge edge: node.incoming()){
                    if(edge.matcher!=null || edge.ruleTarget!=null){
                        states.add(node);
                        break;
                    }
                }
            }
        }
        return states;
    }

    public Rule copy(){
        ArrayList<Node> nodes = new ArrayList<Node>();
        ArrayList<Edge> edges = new ArrayList<Edge>();
        computeIDS(nodes, edges, node);

        Rule newRule = new Rule();
        newRule.id = id;
        newRule.name = name;

        for(int i=0; i<nodes.size(); i++){
            Node newNode = new Node();
            newNode.id = i;
            newNode.name = nodes.get(i).name;
            newNode.action = nodes.get(i).action;
            nodes.set(i, newNode);
        }
        newRule.node = nodes.get(0);
        
        for(Edge edge: edges){
            Edge newEdge = nodes.get(edge.source.id).addEdgeTo(nodes.get(edge.target.id));
            newEdge.matcher = edge.matcher;
            newEdge.fallback = edge.fallback;
            if(edge.ruleTarget!=null){
                RuleTarget ruleTarget = edge.ruleTarget;
                RuleTarget newRuleTarget = newEdge.ruleTarget = new RuleTarget();
                newRuleTarget.rule = ruleTarget.rule==this ? newRule : ruleTarget.rule;
                newRuleTarget.name = ruleTarget.name;
            }
        }

        return newRule;
    }

    public int[] matchString(){
        ArrayList<Integer> ints = new ArrayList<Integer>();

        Node node = this.node;
        while(true){
            if(node.action!=null)
                return null;

            switch(node.outgoing.size()){
                case 0:
                    return CollectionUtil.toIntArray(ints);
                case 1:
                    Edge edge = node.outgoing.get(0);
                    if(edge.loop() || !(edge.matcher instanceof Any))
                        return null;
                    Any any = (Any)edge.matcher;
                    if(any.chars==null || any.chars.length!=1)
                        return null;
                    ints.add(any.chars[0]);
                    node = edge.target;
                    continue;
                default:
                    return null;
            }
        }
    }

    @Override
    public String toString(){
        int matchString[] = matchString();
        if(matchString!=null)
            return '"'+StringUtil.toLiteral(new String(matchString, 0, matchString.length), false)+'"';
        return name;
    }
}
