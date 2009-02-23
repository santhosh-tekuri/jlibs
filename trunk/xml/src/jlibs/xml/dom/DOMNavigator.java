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

package jlibs.xml.dom;

import jlibs.core.graph.Navigator2;
import jlibs.core.graph.PredicateConvertor;
import jlibs.core.graph.Sequence;
import jlibs.core.graph.sequences.ConcatSequence;
import jlibs.xml.sax.sniff.NodeTypes;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.NamespaceContext;

/**
 * @author Santhosh Kumar T
 */
public class DOMNavigator extends Navigator2<Node>{
    @Override
    public Node parent(Node node){
        if(node instanceof Attr)
            return ((Attr)node).getOwnerElement();
        else
            return node.getParentNode();
    }

    @Override
    public Sequence<? extends Node> children(Node node){
        Sequence<Node> seq = new NodeListSequence(node.getChildNodes());
        if(node instanceof Element){
            Element elem = (Element)node;
            seq = new ConcatSequence<Node>(new NamedNodeMapSequence(elem.getAttributes()), seq);
        }
        return seq;
    }

    private class XPathConvertor extends PredicateConvertor<Node>{
        public XPathConvertor(NamespaceContext nsContext){
            super(DOMNavigator.this, new DOMXPathNameConvertor(nsContext));
        }

        @Override
        public String convert(Node source){
            switch(source.getNodeType()){
                case Node.ATTRIBUTE_NODE:
                case NodeTypes.NAMESPACE:
                    return delegate.convert(source);
            }
            return super.convert(source);
        }
    }

    public String getXPath(Node node, NamespaceContext nsContext){
        if(node instanceof Document)
            return "/";
        else
            return getPath(node, new XPathConvertor(nsContext), "/");
    }
}
