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

package jlibs.xml.sax.dog;

import jlibs.core.lang.ImpossibleException;
import jlibs.xml.dom.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public abstract class XPathEngine{
    public abstract String getName();
    public abstract List<Object> evaluate(TestCase testCase, String file) throws Exception;

    protected final Document toDOM(String file) throws ParserConfigurationException, IOException, SAXException{
        return DOMUtil.newDocumentBuilder(true, false, true, false).parse(new InputSource(file));
    }

    @SuppressWarnings({"unchecked"})
    public List<NodeItem> translate(Object result, NamespaceContext nsContext){
        List<NodeItem> nodeList = new ArrayList<NodeItem>();

        if(result instanceof NodeList){
            NodeList nodeSet = (NodeList)result;
            for(int i=0; i<nodeSet.getLength(); i++){
                Node node = nodeSet.item(i);
                NodeItem item = new NodeItem(node, nsContext);
                nodeList.add(item);
            }
        }else{
            if(result instanceof List){
                for(Object obj: (Collection)result){
                    NodeItem item;
                    if(obj instanceof Node)
                        item = new NodeItem((Node)obj, nsContext);
                    else if(obj instanceof net.sf.saxon.om.NodeInfo){
                        net.sf.saxon.om.NodeInfo info = (net.sf.saxon.om.NodeInfo)obj;
                        Node node = (Node)((net.sf.saxon.dom.NodeWrapper)info.getParent()).getUnderlyingNode();
                        item = new NodeItem(node, info.getLocalPart(), info.getStringValue(), nsContext);
                    }else
                        throw new ImpossibleException();
                    nodeList.add(item);
                }
            }
        }

        return nodeList;
    }
}
