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

package jlibs.xml.sax.crawl;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class CrawlingRules{
    Element doc = new Element(null);

    public void addExtension(String extension, QName... elementPath){
        doc.descendant(elementPath).extension = extension;
    }

    public void addAttributeLink(QName... attributePath){
        QName elementPath[] = Arrays.copyOf(attributePath, attributePath.length-1);
        doc.descendant(elementPath).attribute = attributePath[attributePath.length-1];
    }
}

class Element{
    QName qname;
    QName attribute;
    String extension;

    Element parent;

    Element(QName qname){
        this.qname = qname;
    }

    List<Element> children;
    public Element findChild(String namespaceURI, String localName){
        if(children!=null){
            for(Element child: children){
                QName qname = child.qname;
                if(qname.getNamespaceURI().equals(namespaceURI) &&  qname.getLocalPart().equals(localName))
                    return child;
            }
        }
        return null;
    }

    public Element child(QName element){
        Element child = findChild(element.getNamespaceURI(), element.getLocalPart());
        if(child==null){
            if(children==null)
                children = new ArrayList<Element>();
            child = new Element(element);
            children.add(child);
            child.parent = this;
        }
        return child;
    }

    public Element descendant(QName... path){
        Element child = this;
        for(QName elem: path)
            child = child.child(elem);
        return child;
    }
}

