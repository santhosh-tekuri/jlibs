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
    private Map<String, List<String>> map;

    public XPathResults(List<XPath> xpaths){
        map = new HashMap<String, List<String>>(xpaths.size());
        for(XPath xpath: xpaths)
            map.put(xpath.toString(), collectResult(xpath));
    }

    private List<String> collectResult(XPath xpath){
        Map<Integer, String> results = new TreeMap<Integer, String>();

        for(Node node: xpath.nodes){
            node.prepareResults();
            if(node.hasResult())
                    results.putAll(node.results);
        }

        for(Predicate predicate: xpath.predicates){
            if(predicate.hasResult())
                results.putAll(predicate.results);
        }

        return new ArrayList<String>(results.values());
    }

    public List<String> getResult(XPath xpath){
        return map.get(xpath.toString());
    }
}
