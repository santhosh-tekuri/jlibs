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

import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.Position;
import jlibs.xml.sax.sniff.model.Predicate;
import jlibs.xml.sax.sniff.events.Event;

import java.util.*;

/**
 * @author Santhosh Kumar T
 */
public class XPathResults implements Debuggable{
    static final RuntimeException STOP_PARSING = new RuntimeException();
    
    int minHits = -1;
    private List<String> results = new ArrayList<String>();
    private Map<Node, List<Integer>> resultsMap = new HashMap<Node, List<Integer>>();
    private Map<Predicate, List<Integer>> predicateResultsMap = new HashMap<Predicate, List<Integer>>();

    public XPathResults(int minHits){
        this.minHits = minHits;
    }

    private Map<Predicate, PredicateResult> cachedMap = new HashMap<Predicate, PredicateResult>();

    class PredicateResult{
        public List<Node> nodes = new ArrayList<Node>();
        public List<Predicate> predicates = new ArrayList<Predicate>();

        public Map<Predicate, ArrayDeque<Integer>> predicateResultMap = new HashMap<Predicate, ArrayDeque<Integer>>();
        public ArrayDeque<Integer> resultStack = new ArrayDeque<Integer>();

        public PredicateResult(Predicate predicate){
            nodes.addAll(predicate.nodes);
            predicates.addAll(predicate.predicates);

            for(Predicate p: predicate.memberOf)
                predicateResultMap.put(p, new ArrayDeque<Integer>());
        }

        public void addResult(){
            resultStack.push(results.size()-1);
            for(ArrayDeque<Integer> stack: predicateResultMap.values())
                stack.push(results.size()-1);
        }

        public void removeResult(Node node){
            resultStack.pop();
        }
        
        public void removeResult(Predicate p){
            predicateResultMap.get(p).pop();
        }

        public Integer hit(Node node){
            nodes.remove(node);
            return getResult(node);
        }

        public Integer hit(Predicate predicate){
            predicates.remove(predicate);
            return getResult(predicate);
        }

        public Integer getResult(Node node){
            return canGiveResult() ? resultStack.peek() : null;
        }

        public Integer getResult(Predicate p){
            if(canGiveResult()){
                ArrayDeque<Integer> stack = predicateResultMap.get(p);
                return stack==null ? null : stack.peek();
            }else
                return null;
        }

        private boolean canGiveResult(){
            return nodes.size()==0 && predicates.size()==0 && !resultStack.isEmpty();
        }

        private Integer getResult(){
            if(canGiveResult())
                return resultStack.peek();
            else
                return null;
        }
    }

    /*-------------------------------------------------[ Hit ]---------------------------------------------------*/

    private PositionTracker positionTracker = new PositionTracker();

    public boolean hit(ContextManager.Context context, Event event, Node node){
        if(node instanceof Position){
            Position position = (Position)node;
            if(!positionTracker.hit(context, position))
                return false;
        }

        if(node.resultInteresed()){
            Object resultWrapper = event.getResultWrapper();
            if(resultWrapper!=null)
                results.add(resultWrapper.toString());

            if(node.userGiven)
                addResult(node, results.size()-1);

            for(Predicate predicate: node.predicates()){
                PredicateResult predicateResult = cachedMap.get(predicate);
                if(predicateResult==null)
                    cachedMap.put(predicate, predicateResult=new PredicateResult(predicate));
                predicateResult.addResult();
                if(Sniffer.debug)
                    System.out.format("Cached Predicate Result %2d: %s ---> %s %n", results.size(), node, resultWrapper);

                checkMembers(predicate);

//                Integer result = predicateResult.getResult();
//                if(result!=null){
//                    if(predicate.userGiven)
//                        addResult(predicate, result);
//                    hitMemberOf(predicate);
//                }
            }
            
            for(Predicate member: node.memberOf()){
                PredicateResult predicateResult = cachedMap.get(member);
                Integer result = predicateResult.hit(node);
                if(result!=null){
                    if(member.userGiven){
                        addResult(member, result);
                        predicateResult.removeResult(node);
                    }
                    int consumed = hitMemberOf(member);
//                    if(consumed>0)
//                        predicateResult.removeResult();
                }
            }
            
            for(Predicate predicate: node.predicates()){
                if(node.memberOf.contains(predicate))
                    return true;
                PredicateResult predicateResult = cachedMap.get(predicate);
                if(predicateResult!=null){
                    Integer result = predicateResult.getResult(node);
                    if(result!=null){
                        if(predicate.userGiven){
                            addResult(predicate, result);
                            predicateResult.removeResult(node);
                        }
                        hitMemberOf(predicate);
                    }
                }
            }
        }
        return true;
    }

    private int checkMembers(Predicate predicate){
        int consumed = 0;
        for(Predicate member: predicate.predicates){
            PredicateResult predicateResult = cachedMap.get(member);
            if(predicateResult!=null){
                consumed++;
                Integer result = predicateResult.getResult(predicate);
                if(result!=null){
                    if(predicate.userGiven)
                        cachedMap.get(predicate).hit(member);
                    checkMembers(member);
                }
            }
        }
        return consumed;
    }

    private int hitMemberOf(Predicate predicate){
        int consumed = 0;
        for(Predicate member: predicate.memberOf){
            PredicateResult predicateResult = cachedMap.get(member);
            if(predicateResult!=null){
                consumed++;
                Integer result = predicateResult.hit(predicate);
                if(member.userGiven){
                    Integer userResult = predicateResult.getResult((Node)null);
                    if(userResult!=null){
                        addResult(member, userResult);
                        predicateResult.removeResult((Node)null);
                    }
                }
                if(result!=null){
                    hitMemberOf(member);
                }
            }
        }
        return consumed;
    }

    private void hit(){
        if(minHits>0){
            minHits--;
            if(minHits==0)
                throw STOP_PARSING;
        }
    }

    void clearHitCounts(ContextManager.Context context, Node node){
        positionTracker.contextEnded(context);
    }

    void clearPredicateCache(int depth, Node node){
        for(Predicate predicate: node.predicates()){
            for(Node n: predicate.nodes){
                while(n!=null){
                    n = n.parent;
                    if(n==node){
                        clearCache(predicate);
                        break;
                    }
                }
            }
        }
    }

    private void clearCache(Predicate predicate){
        if(debug)
            System.out.println("cleared cache of: "+predicate);
        cachedMap.remove(predicate);
        for(Predicate member: predicate.memberOf)
            clearCache(member);
    }

    /*-------------------------------------------------[ Add Result ]---------------------------------------------------*/

    private void addResult(Node node, Integer result){
        List<Integer> list = resultsMap.get(node);
        if(list==null)
            resultsMap.put(node, list=new ArrayList<Integer>());
        list.add(result);
        if(debug)
            System.out.format("Node-Hit %2d: %s ---> %s %n", results.size(), node, results.get(result));

        hit();
    }

    private void addResult(Predicate predicate, Integer result){
        List<Integer> list = predicateResultsMap.get(predicate);
        if(list==null)
            predicateResultsMap.put(predicate, list=new ArrayList<Integer>());
        list.add(result);
        if(debug)
            System.out.format("Predicate-Hit %2d: %s ---> %s %n", results.size(), predicate, results.get(result));

        hit();
    }

    /*-------------------------------------------------[ Get Result ]---------------------------------------------------*/

    public List<String> getResult(XPath xpath){
        TreeSet<Integer> indexes = new TreeSet<Integer>();
        for(Node node: xpath.nodes){
            List<Integer> list = resultsMap.get(node);
            if(list!=null)
                indexes.addAll(list);
        }

        for(Predicate predicate: xpath.predicates){
            List<Integer> list = predicateResultsMap.get(predicate);
            if(list!=null)
                indexes.addAll(list);
        }

        List<String> result = new ArrayList<String>();
        for(int i: indexes)
            result.add(results.get(i));
        
        return result;
    }
}
