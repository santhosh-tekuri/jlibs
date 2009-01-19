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
import jlibs.xml.sax.sniff.model.Root;
import jlibs.xml.sax.sniff.model.functions.Function;

import java.util.*;

/**
 * @author Santhosh Kumar T
 */
public class XPathResults implements Debuggable{
    private Map<XPath, List<String>> map;

    public XPathResults(Root root, List<XPath> xpaths){
        map = new HashMap<XPath, List<String>>(xpaths.size());
        for(XPath xpath: xpaths)
            map.put(xpath, collectResult(root, xpath));
    }

    private List<String> collectResult(Root root, XPath xpath){
        Map<Integer, String> results = new TreeMap<Integer, String>();

        for(Node node: xpath.nodes){
            node = node.locateIn(root);
            if(node instanceof Function){
                Function function = (Function)node;
                function.joinResults();
                if(function.hasResult())
                    results.putAll(function.results);
                else
                    results.put(-1, function.defaultResult());
            }else if(node.hasResult())
                results.putAll(node.results);
        }

        for(Predicate predicate: xpath.predicates){
            predicate = predicate.locateIn(root);
            if(predicate.hasResult())
                results.putAll(predicate.results);
        }

        return new ArrayList<String>(results.values());
    }

    public List<String> getResult(XPath xpath){
        return map.get(xpath);
    }
}
