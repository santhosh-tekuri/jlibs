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

import java.util.*;

/**
 * @author Santhosh Kumar T
 */
public class Predicate extends Results{
    public Node memberNode;
    public Predicate memberPredicate;
    public List<Predicate> memberOf = new ArrayList<Predicate>();

    @SuppressWarnings({"ManualArrayToCollectionCopy"})
    public Predicate(Node node){
        memberNode = node;
    }

    public Predicate(Predicate predicate){
        memberPredicate = predicate;
//        predicate.memberOf.add(this);
    }

    public boolean equivalent(Predicate predicate){
        return this.memberNode==predicate.memberNode && this.memberPredicate==predicate.memberPredicate;
    }

    @Override
    public String toString(){
        StringBuilder buff1 = new StringBuilder();
        if(userGiven)
            buff1.append("userGiven ");
        if(memberNode!=null){
            if(buff1.length()>0)
                buff1.append(", ");
            buff1.append(memberNode);
        }

        if(memberPredicate!=null){
            if(buff1.length()>0)
                buff1.append(", ");
            buff1.append(memberPredicate);
        }

//        if(memberOf.size()>0)
//            buff1.append("==> ");
//        for(Predicate predicate: memberOf){
//            if(buff1.length()>0)
//                buff1.append(", ");
//            buff1.append(predicate);
//        }

        return "["+buff1+"]";
    }

    /*-------------------------------------------------[ Cache ]---------------------------------------------------*/

    private Cache cache;

    public boolean hasCache(){
        return cache!=null;
    }
    
    public Cache cache(){
        if(cache==null)
            cache = new Cache();
        return cache;
    }

    public void clearCache(){
        if(debug)
            System.out.println("cleared cache of: "+this);

        cache = null;
        for(Predicate member: memberOf)
            member.clearCache();
    }

    public class Cache{
        public List<Node> nodes = new ArrayList<Node>();
        public List<Predicate> predicates = new ArrayList<Predicate>();

        public Map<Predicate, TreeMap<Integer, String>> predicateResultMap = new HashMap<Predicate, TreeMap<Integer, String>>();
        public TreeMap<Integer, String> resultStack = new TreeMap<Integer, String>();

        public Cache(){
            if(memberNode!=null)
                nodes.add(memberNode);
            if(memberPredicate!=null)
                predicates.add(memberPredicate);

            for(Predicate p: memberOf)
                predicateResultMap.put(p, new TreeMap<Integer, String>());
        }

        public void addResult(int docOrder, String result){
            resultStack.put(docOrder, result);
            for(TreeMap<Integer, String> stack: predicateResultMap.values())
                stack.put(docOrder, result);

            if(debug)
                System.out.format("Cached Predicate Result %2d: %s ---> %s %n", resultStack.size(), Predicate.this, result);
        }

        public void removeResult(Node node){
            resultStack.remove(resultStack.lastKey());
        }

        public void removeResult(Predicate p){
            TreeMap<Integer, String> map = predicateResultMap.get(p);
            map.remove(map.lastKey());
        }

        public Map.Entry<Integer, String> hit(Node node){
            nodes.remove(node);
            return getResult(node);
        }

        public Map.Entry<Integer, String> hit(Predicate predicate){
            predicates.remove(predicate);
            return getResult(memberNode);
        }

        public Map.Entry<Integer, String> getResult(Node node){
            return canGiveResult() ? resultStack.lastEntry() : null;
        }

        public Map.Entry<Integer, String> getResult(Predicate p){
            if(canGiveResult()){
                TreeMap<Integer, String> stack = predicateResultMap.get(p);
                return stack==null ? null : stack.lastEntry();
            }else
                return null;
        }

        private boolean canGiveResult(){
            return nodes.size()==0 && predicates.size()==0 && !resultStack.isEmpty();
        }
    }

    /*-------------------------------------------------[ Reset ]---------------------------------------------------*/

    public void reset(){
        super.reset();
        cache = null;
    }
}
