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

import java.util.*;

/**
 * @author Santhosh Kumar T
 */
public class XPathResults{
    static final RuntimeException STOP_PARSING = new RuntimeException();
    
    int minHits = -1;
    private List<String> results = new ArrayList<String>();
    private Map<Node, List<Integer>> map = new HashMap<Node, List<Integer>>();

    public XPathResults(int minHits){
        this.minHits = minHits;
    }

    public void add(Node node, String value){
        results.add(value);

        List<Integer> list = map.get(node);
        if(list==null)
            map.put(node, list=new ArrayList<Integer>());
        list.add(results.size()-1);
        if(Sniffer.debug)
            System.out.format("Hit %2d: %s ---> %s %n", results.size(), node, value);

        if(minHits>0){
            minHits--;
            if(minHits==0)
                throw STOP_PARSING;
        }
    }

    public List<String> getResult(XPath xpath){
        TreeSet<Integer> indexes = new TreeSet<Integer>();
        for(Node node: xpath.nodes){
            List<Integer> list = map.get(node);
            if(list!=null)
                indexes.addAll(list);
        }

        List<String> result = new ArrayList<String>();
        for(int i: indexes)
            result.add(results.get(i));
        
        return result;
    }
}
