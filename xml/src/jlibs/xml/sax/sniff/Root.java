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

import jlibs.xml.DefaultNamespaceContext;
import jlibs.xml.Namespaces;
import org.jaxen.saxpath.SAXPathException;

import javax.xml.namespace.NamespaceContext;
import java.util.HashMap;

/**
 * @author Santhosh Kumar T
 */
class Root extends Node{
    protected int totalMinHits;
    protected int timer;
    private NamespaceContext nsContext;

    public Root(NamespaceContext nsContext){
        super(null);
        root = this;
        this.nsContext = nsContext;
    }

    protected void reset(){
        timer = totalMinHits;
        super.reset();
    }
    
    public Node add(String xpath, int minHits) throws SAXPathException{
        if(!xpath.startsWith("/"))
            xpath = '/'+xpath;
        Node node = add(xpath);
        node.setMinHits(minHits);
        return node;
    }
    
    private XPathParser parser;
    private Node add(String xpath) throws SAXPathException{
        if(parser==null)
            parser = new XPathParser(nsContext);
        Node node = parser.parse(xpath);
        Node merged = root.merge(node.root, new HashMap<Node, Node>()).get(node);
        return merged!=null ? merged : node;
    }

    @Override
    protected String getStep(){
        return "";
    }

    @Override
    protected boolean canMerge(Node node){
        return node.getClass()==getClass();
    }

    public static void main(String[] args) throws SAXPathException{
        DefaultNamespaceContext nsContext = new DefaultNamespaceContext();
        nsContext.declarePrefix("xsd", Namespaces.URI_XSD);
        Root root = new Root(nsContext);

        root.add("xsd:schema//xsd:import/@xsd:test");
        root.add("xsd:schema/xsd:import/@xsd:test1");
        System.out.println("");
    }
}
