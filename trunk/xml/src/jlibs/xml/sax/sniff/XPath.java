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

import java.util.List;
import java.util.Collections;

/**
 * @author Santhosh Kumar T
 */
public class XPath{
    String xpath;
    List<Node> nodes = Collections.emptyList();
    Predicate predicate;
    int minHits;

    public XPath(String xpath, List<Node> nodes){
        this.xpath = xpath;
        this.nodes = nodes;
    }

    public XPath(String xpath, Predicate predicate){
        this.xpath = xpath;
        this.predicate = predicate;
    }

    @Override
    public String toString(){
        return xpath;
    }
}
