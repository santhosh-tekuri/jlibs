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

import jlibs.xml.Namespaces;

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

    public static CrawlingRules defaultRules(){
        CrawlingRules rules = new CrawlingRules();

        QName xsd_schema = new QName(Namespaces.URI_XSD, "schema");
        QName xsd_import = new QName(Namespaces.URI_XSD, "import");
        QName attr_schemaLocation = new QName("schemaLocation");
        QName xsd_include = new QName(Namespaces.URI_XSD, "include");
        QName xsl_stylesheet = new QName(Namespaces.URI_XSL, "stylesheet");
        QName attr_href = new QName("href");
        QName wsdl_definitions = new QName(Namespaces.URI_WSDL, "definitions");
        QName attr_location = new QName("location");
        QName wsdl_types = new QName(Namespaces.URI_WSDL, "types");

        rules.addExtension("xsd", xsd_schema);
        rules.addAttributeLink(xsd_schema, xsd_import, attr_schemaLocation);
        rules.addAttributeLink(xsd_schema, xsd_include, attr_schemaLocation);

        rules.addExtension("xsl", xsl_stylesheet);
        rules.addAttributeLink(xsl_stylesheet, new QName(Namespaces.URI_XSL, "import"), attr_href);
        rules.addAttributeLink(xsl_stylesheet, new QName(Namespaces.URI_XSL, "include"), attr_href);

        rules.addExtension("wsdl", wsdl_definitions);
        rules.addAttributeLink(wsdl_definitions, new QName(Namespaces.URI_WSDL, "import"), attr_location);
        rules.addAttributeLink(wsdl_definitions, new QName(Namespaces.URI_WSDL, "include"), attr_location);
        rules.addAttributeLink(wsdl_definitions, wsdl_types, xsd_schema, xsd_import, attr_schemaLocation);
        rules.addAttributeLink(wsdl_definitions, wsdl_types, xsd_schema, xsd_include, attr_schemaLocation);

        return rules;
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

