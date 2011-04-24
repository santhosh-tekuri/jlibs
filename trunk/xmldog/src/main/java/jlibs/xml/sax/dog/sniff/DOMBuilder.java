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

package jlibs.xml.sax.dog.sniff;

import jlibs.core.lang.ImpossibleException;
import jlibs.xml.sax.dog.NodeItem;
import jlibs.xml.sax.dog.NodeType;
import org.w3c.dom.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * @author Santhosh Kumar T
 */
public class DOMBuilder extends XMLBuilder{
    private static final DocumentBuilder domBuilder;
    static{
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try{
            domBuilder = factory.newDocumentBuilder();
        }catch(ParserConfigurationException ex){
            throw new RuntimeException(ex);
        }
    }

    private Document document = domBuilder.newDocument();
    private Node curNode;

    @Override
    public Object onStartDocument(){
        return curNode = document;
    }

    @Override
    public Object onStartElement(String uri, String localName, String qualifiedName){
        if(active){
            Element element = document.createElementNS(uri, qualifiedName);
            if(curNode!=null)
                curNode.appendChild(element);
            return curNode = element;
        }
        return curNode;
    }

    @Override
    public Object onEvent(Event event){
        switch(event.type()){
            case NodeType.ATTRIBUTE:
                Attr attr = null;
                if(active){
                    attr = document.createAttributeNS(event.namespaceURI(), event.qualifiedName());
                    attr.setNodeValue(event.value());
                    if(curNode!=null)
                        ((Element)curNode).setAttributeNodeNS(attr);
                }
                return attr;
            case NodeType.COMMENT:
                Comment comment =null;
                if(active){
                    comment = document.createComment(event.value());
                    if(curNode!=null)
                        curNode.appendChild(comment);
                }
                return comment;
            case NodeItem.PI:
                ProcessingInstruction pi = null;
                if(active){
                    pi = document.createProcessingInstruction(event.localName(), event.value());
                    if(curNode!=null)
                        curNode.appendChild(pi);
                }
                return pi;
            case NodeItem.TEXT:
                Text text = null;
                if(active){
                    text = document.createTextNode(event.value());
                    if(curNode!=null)
                        curNode.appendChild(text);
                }
                return text;
            case NodeItem.NAMESPACE:
                attr = null;
                if(active){
                    String qname = event.localName().length()==0 ? "xmlns" : "xmlns:"+event.localName();
                    attr = document.createAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, qname);
                    attr.setNodeValue(event.value());
                    if(curNode!=null)
                        ((Element)curNode).setAttributeNodeNS(attr);
                }
                return attr;
        }
        throw new ImpossibleException("event.type: "+event.type());
    }

    @Override
    public Object onEndElement(){
        if(curNode!=null){
            curNode = curNode.getParentNode();
            if(curNode==null)
                active = false;
        }
        return curNode;
    }

    @Override
    public void onEndDocument(){
        document = null;
    }
}
