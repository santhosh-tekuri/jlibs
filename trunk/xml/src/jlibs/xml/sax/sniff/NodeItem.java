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

import jlibs.xml.Namespaces;
import jlibs.xml.dom.DOMNavigator;
import jlibs.xml.sax.sniff.events.Event;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

import javax.xml.namespace.NamespaceContext;

/**
 * @author Santhosh Kumar T
 */
public class NodeItem implements NodeTypes, Comparable<NodeItem>{
    private final long order;
    public final int type;
    public final String location;
    public final String value;
    public final String localName;
    public final String namespaceURI;
    public final String name;

    public NodeItem(Event event){
        order = event.order();
        type = event.type();
        location = event.location();
        value = event.getValue();

        localName = event.localName();
        namespaceURI = event.namespaceURI();
        name = event.qualifiedName();
    }

    // used only for testing purposes
    public NodeItem(Node node, NamespaceContext nsContext){
        order = -1;
        if(node instanceof Attr && Namespaces.URI_XMLNS.equals(node.getNamespaceURI()))
            type = NAMESPACE;
        else
            type = node.getNodeType();
        location = new DOMNavigator().getXPath(node, nsContext);
        value = node.getNodeValue();

        localName = node.getLocalName();
        namespaceURI = node.getNamespaceURI();
        name = node.getNodeName();
    }

    @Override
    public int compareTo(NodeItem that){
        long diff = this.order - that.order;
        if(diff==0)
            return 0;
        else
            return diff>0 ? 1: -1;
    }

    @Override
    public String toString(){
        return location;
    }
}
