/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
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
    protected Object onStartDocument(){
        return curNode = document;
    }

    @Override
    protected Object onStartElement(Event event){
        Element element = document.createElementNS(event.namespaceURI(), event.qualifiedName());
        if(curNode!=null)
            curNode.appendChild(element);
        return curNode = element;
    }

    @Override
    protected Object onEvent(Event event){
        switch(event.type()){
            case NodeType.ATTRIBUTE:
                Attr attr = document.createAttributeNS(event.namespaceURI(), event.qualifiedName());
                attr.setNodeValue(event.value());
                if(curNode!=null)
                    ((Element)curNode).setAttributeNodeNS(attr);
                return attr;
            case NodeType.COMMENT:
                Comment comment = document.createComment(event.value());
                if(curNode!=null)
                    curNode.appendChild(comment);
                return comment;
            case NodeItem.PI:
                ProcessingInstruction pi = document.createProcessingInstruction(event.localName(), event.value());
                if(curNode!=null)
                    curNode.appendChild(pi);
                return pi;
            case NodeItem.TEXT:
                Text text = document.createTextNode(event.value());
                if(curNode!=null)
                    curNode.appendChild(text);
                return text;
            case NodeItem.NAMESPACE:
                String qname = event.localName().length()==0 ? "xmlns" : "xmlns:"+event.localName();
                attr = document.createAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, qname);
                attr.setNodeValue(event.value());
                if(curNode!=null)
                    ((Element)curNode).setAttributeNodeNS(attr);
                return attr;
        }
        throw new ImpossibleException("event.type: "+event.type());
    }

    @Override
    protected Object onEndElement(){
        return curNode = curNode.getParentNode();
    }

    @Override
    protected void onEndDocument(){
        document = null;
    }

    @Override
    protected void clearCurNode(){
        curNode = null;
    }

    @Override
    protected void removeFromParent(Object node){
        if(node instanceof Attr){
            Attr attr = (Attr)node;
            Element owner = attr.getOwnerElement();
            if(owner!=null)
                owner.removeAttributeNode(attr);
        }else{
            Node child = (Node)node;
            Node parent = child.getParentNode();
            if(parent!=null)
                parent.removeChild(child);
        }
    }

    @Override
    protected boolean hasParent(){
        return curNode.getParentNode()!=null;
    }
}
