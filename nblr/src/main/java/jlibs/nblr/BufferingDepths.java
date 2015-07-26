/**
 * Copyright 2015 Santhosh Kumar Tekuri
 *
 * The JLibs authors license this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package jlibs.nblr;

import jlibs.nblr.actions.BufferAction;
import jlibs.nblr.actions.ErrorAction;
import jlibs.nblr.actions.PublishAction;
import jlibs.nblr.rules.*;

import javax.swing.tree.TreePath;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class BufferingDepths{
    public Map<Rule, Map<Node, Map<Integer, Integer>>> ruleMap = new HashMap<Rule, Map<Node, Map<Integer, Integer>>>();

    private Map<Integer, Integer> depths(RuleTarget ruleTarget, boolean create){
        Map<Node, Map<Integer, Integer>> nodeMap = ruleMap.get(ruleTarget.rule);
        if(nodeMap==null){
            if(!create)
                return null;
            ruleMap.put(ruleTarget.rule, nodeMap=new HashMap<Node, Map<Integer, Integer>>());
        }
        Map<Integer, Integer> depthMap = nodeMap.get(ruleTarget.node());
        if(depthMap==null){
            if(!create)
                return null;
            nodeMap.put(ruleTarget.node(), depthMap=new HashMap<Integer, Integer>());
        }
        return depthMap;
    }

    private Integer get(RuleTarget ruleTarget, int depth){
        Map<Integer, Integer> depths = depths(ruleTarget, false);
        return depths==null ? null : depths.get(depth);
    }

    private void set(RuleTarget ruleTarget, int enteringDepth, int exitingDepth){
        Map<Integer, Integer> depths = depths(ruleTarget, true);
        Integer value = depths.get(enteringDepth);
        if(value!=null && !value.equals(exitingDepth))
            throw new Error(ruleTarget+"["+enteringDepth+"] "+value+", "+exitingDepth);
        depths.put(enteringDepth, exitingDepth);
    }

    private Deque<TreePath> stack = new ArrayDeque<TreePath>();
    private TreePath path;

    public void calculate(Rule rule){
        stack.clear();
        path = null;
        
        RuleTarget ruleTarget = new RuleTarget();
        ruleTarget.rule = rule;
        Edge edge = new Edge(new Node(), new Node());
        edge.ruleTarget = ruleTarget;
        stack.push(new TreePath(new Element(edge, 0)));

        if(rule.node.outgoing.isEmpty()){
            process(rule.node, 0);
            return;
        }

        add(rule.node.outgoing.get(0));

        outer: while(path!=null){
            Element elem = (Element)path.getLastPathComponent();

            Edge next = null;

            if(elem.edge.ruleTarget!=null){
                if(elem.processRuleTarget){
                    elem.processRuleTarget = false;

                    Integer exitDepth = get(elem.edge.ruleTarget, elem.depth);
                    if(exitDepth==null){
                        Node node = elem.edge.ruleTarget.node();
                        if(node.outgoing.isEmpty())
                            process(node, elem.depth);
                        else{
                            stack.push(path);
                            next = node.outgoing.get(0);
                        }
                    }
                }else
                    stack.pop();
            }

            if(next==null)
                next = findEdge(null, elem.edge.target);
            if(next==null && elem.edge.target.outgoing.size()==0)
                process(elem.edge.target, elem.depth);
            
            while(next==null && path!=null){
                if(stack.peek()==path)
                    continue outer;
                elem = (Element)path.getLastPathComponent();
                next = findEdge(elem.edge, elem.edge.source);
                path = path.getParentPath();
            }
            if(next==null)
                return;
            else
                add(next);
        }
    }

    private void process(Node node, int depth){
        depth = update(depth, node);
        if(node.action instanceof ErrorAction)
            return;
        Element e = (Element)stack.peek().getLastPathComponent();
        set(e.edge.ruleTarget, e.depth, depth);
    }

    private int update(int depth, Node node){
        if(node.action instanceof BufferAction)
            depth++;
        else if(node.action instanceof PublishAction){
            depth--;
            if(depth<0)
                throw new IllegalStateException("invalid buffer state");
        }

        Answer ans = depth>0 ? Answer.YES : Answer.NO;
        if(node.buffering==null)
            node.buffering = ans;
        else if(node.buffering!=ans)
            node.buffering = Answer.MAY_BE;
        return depth;
    }

    private void add(Edge edge){
        int depth = 0;
        if(path!=null){
            Element lastElem = (Element)path.getLastPathComponent();
            if(lastElem.edge.ruleTarget==null)
                depth = lastElem.depth;
            else if(lastElem.edge.ruleTarget.node()==edge.source)
                depth = lastElem.depth;
            else{
                Integer exitingDepth = get(lastElem.edge.ruleTarget, lastElem.depth);
                if(exitingDepth!=null) // null in case of recursion
                    depth = exitingDepth;
            }
        }

        depth = update(depth, edge.source);
        Element elem = new Element(edge, depth);
        if(path==null)
            path = new TreePath(elem);
        else
            path = path.pathByAddingChild(elem);
    }

    private Edge findEdge(Edge curEdge, Node node){
        int index = 0;
        if(curEdge!=null)
            index = node.outgoing.indexOf(curEdge)+1;
        for(;index<node.outgoing.size(); index++){
            Edge edge = node.outgoing.get(index);
            if(edge.loop() && edge.ruleTarget==null)
                continue;
            if(!contains(edge))
                return edge;
        }
        return null;
    }

    private boolean contains(Edge edge){
        TreePath path = this.path;
        while(path!=null){
            if(((Element)path.getLastPathComponent()).edge==edge)
                return true;
            path = path.getParentPath();
        }
        return false;
    }
    
}

class Element{
    final Edge edge;
    final int depth;

    boolean processRuleTarget = true;

    Element(Edge edge, int depth){
        this.edge = edge;
        this.depth = depth;
    }

    @Override
    public String toString(){
        return edge.toString();
    }
}
