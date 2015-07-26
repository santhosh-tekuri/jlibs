/**
 * Copyright 2015 Santhosh Kumar Tekuri
 *
 * The JLibs authors license this file to you under the Apache License,
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

package jlibs.xml.sax.crawl;

import jlibs.xml.Namespaces;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class CrawlingRules{
    Element doc = new Element(null);

    public void addExtension(String extension, QName... elementPath){
        doc.descendant(elementPath).extension = extension;
    }

    public void addLink(QName elementPath[], QName namespaceAttribute, QName locationAttribute){
        Element element = doc.descendant(elementPath);
        element.namespaceAttribute = namespaceAttribute;
        element.locationAttribute = locationAttribute;
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
        QName attr_namespace = new QName("namespace");

        rules.addExtension("xsd", xsd_schema);
        rules.addLink(new QName[]{xsd_schema, xsd_import}, attr_namespace, attr_schemaLocation);
        rules.addLink(new QName[]{xsd_schema, xsd_include}, null, attr_schemaLocation);

        rules.addExtension("xsl", xsl_stylesheet);
        rules.addLink(new QName[]{xsl_stylesheet, new QName(Namespaces.URI_XSL, "import")}, null, attr_href);
        rules.addLink(new QName[]{xsl_stylesheet, new QName(Namespaces.URI_XSL, "include")}, null, attr_href);

        rules.addExtension("wsdl", wsdl_definitions);
        rules.addLink(new QName[]{wsdl_definitions, new QName(Namespaces.URI_WSDL, "import")}, attr_namespace, attr_location);
        rules.addLink(new QName[]{wsdl_definitions, new QName(Namespaces.URI_WSDL, "include")}, null, attr_location);
        rules.addLink(new QName[]{wsdl_definitions, wsdl_types, xsd_schema, xsd_import}, attr_namespace, attr_schemaLocation);
        rules.addLink(new QName[]{wsdl_definitions, wsdl_types, xsd_schema, xsd_include}, null, attr_schemaLocation);

        return rules;
    }
}

class Element{
    QName qname;
    QName locationAttribute;
    QName namespaceAttribute;
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

