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

import jlibs.core.lang.Util;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * @author Santhosh Kumar T
 */
public class DOMUtil{
    public static DocumentBuilder newDocumentBuilder(boolean nsAware, boolean validating) throws ParserConfigurationException{
        return newDocumentBuilder(nsAware, validating, false, false);
    }

    public static DocumentBuilder newDocumentBuilder(boolean nsAware, boolean validating, boolean coalescing, boolean ignoreComments) throws ParserConfigurationException{
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(nsAware);
        factory.setValidating(validating);
        factory.setCoalescing(coalescing);
        factory.setIgnoringComments(ignoreComments);
        return factory.newDocumentBuilder();
    }

    public static int getPosition(Element elem){
        int pos = 1;
        NodeList list = elem.getParentNode().getChildNodes();
        for(int i=0; i<list.getLength(); i++){
            Node node = list.item(i);
            if(node==elem)
                break;
            if(node instanceof Element
                    && Util.equals(node.getNamespaceURI(), elem.getNamespaceURI())
                    && node.getLocalName().equals(elem.getLocalName()))
                pos++;
        }
        return pos;
    }
}
