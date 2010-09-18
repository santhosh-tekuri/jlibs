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

import jlibs.xml.sax.SAXProducer;
import jlibs.xml.sax.XMLDocument;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class Rule implements SAXProducer{
    public int id;
    public String name;
    public Node node;

    public ArrayList<Node> nodes(){
        ArrayList<Node> nodes = new ArrayList<Node>();
        ArrayList<Edge> edges = new ArrayList<Edge>();
        computeIDS(nodes, edges, node);
        return nodes;
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

    @Override
    public String toString(){
        return name;
    }

    /*-------------------------------------------------[ SAXProducer ]---------------------------------------------------*/

    @Override
    public void serializeTo(QName rootElement, XMLDocument xml) throws SAXException{
        xml.startElement("rule");
        xml.addAttribute("name", name);

        ArrayList<Node> nodes = new ArrayList<Node>();
        ArrayList<Edge> edges = new ArrayList<Edge>();
        computeIDS(nodes, edges, node);
        for(Node node: nodes)
            xml.add(node);
        for(Edge edge: edges)
            xml.add(edge);
        xml.endElement();
    }
}
