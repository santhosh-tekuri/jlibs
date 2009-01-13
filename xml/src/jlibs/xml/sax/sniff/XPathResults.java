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
import jlibs.xml.sax.sniff.model.Predicate;

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
        public ArrayDeque<Integer> resultStack = new ArrayDeque<Integer>();

        public PredicateResult(Predicate predicate){
            nodes.addAll(predicate.nodes);
            predicates.addAll(predicate.predicates);
        }

        public Integer hit(Node node){
            nodes.remove(node);
            return getResult();
        }

        public Integer hit(Predicate predicate){
            predicates.remove(predicate);
            return getResult();
        }

        public Integer getResult(){
            if(nodes.size()==0 && predicates.size()==0 && !resultStack.isEmpty())
                return resultStack.pop();
            else
                return null;
        }
    }

    /*-------------------------------------------------[ Hit ]---------------------------------------------------*/

    public void hit(Node node, Object resultWrapper){
        if(node.resultInteresed()){
            results.add(resultWrapper.toString());

            if(node.userGiven)
                addResult(node, results.size()-1);

            for(Predicate predicate: node.predicates){
                PredicateResult predicateResult = cachedMap.get(predicate);
                if(predicateResult==null)
                    cachedMap.put(predicate, predicateResult=new PredicateResult(predicate));
                predicateResult.resultStack.push(results.size()-1);
                if(Sniffer.debug)
                    System.out.format("Cached Predicate Result %2d: %s ---> %s %n", results.size(), node, resultWrapper);
            }
            for(Predicate member: node.memberOf){
                PredicateResult predicateResult = cachedMap.get(member);
                Integer result = predicateResult.hit(node);
                if(result!=null){
                    if(member.userGiven)
                        addResult(member, result);
                    hit(member);
                }
            }
        }
    }

    private void hit(Predicate predicate){
        for(Predicate member: predicate.memberOf){
            PredicateResult predicateResult = cachedMap.get(member);
            if(predicateResult!=null){
                Integer result = predicateResult.hit(predicate);
                if(result!=null){
                    if(member.userGiven)
                        addResult(member, result);
                    hit(member);
                }
            }
        }
    }

    private void hit(){
        if(minHits>0){
            minHits--;
            if(minHits==0)
                throw STOP_PARSING;
        }
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

        if(xpath.predicate!=null){
            List<Integer> list = predicateResultsMap.get(xpath.predicate);
            if(list!=null)
                indexes.addAll(list);
        }

        List<String> result = new ArrayList<String>();
        for(int i: indexes)
            result.add(results.get(i));
        
        return result;
    }
}
