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

package jlibs.xml.xsd;

import jlibs.xml.sax.SAXUtil;
import org.apache.xerces.xs.*;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates sample values from given XML
 *
 * @author Santhosh Kumar T
 */
public class XMLSampleValueGenerator implements XSInstance.SampleValueGenerator{
    private Map<XSElementDeclaration, String> elementValues = new HashMap<XSElementDeclaration, String>();
    private Map<XSAttributeDeclaration, String> attributeValues = new HashMap<XSAttributeDeclaration, String>();

    public XMLSampleValueGenerator(final XSModel schema, InputSource sampleInput) throws SAXException, ParserConfigurationException, IOException{
        DefaultHandler handler = new DefaultHandler(){
            private List<QName> xpath = new ArrayList<QName>();
            private CharArrayWriter contents = new CharArrayWriter();

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
                xpath.add(new QName(uri, localName));
                for(int i=0; i<attributes.getLength(); i++){
                    QName qname = new QName(attributes.getURI(i), attributes.getLocalName(i));
                    xpath.add(qname);
                    XSAttributeDeclaration attr = XSUtil.findAttributeDeclaration(schema, xpath);
                    if(attr!=null)
                        attributeValues.put(attr, attributes.getValue(i));
                    xpath.remove(xpath.size()-1);
                }
                contents.reset();
            }


            @Override
            public void characters(char[] ch, int start, int length) throws SAXException{
                contents.write(ch, start, length);
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException{
                if(contents.size()>0){
                    XSElementDeclaration elem = XSUtil.findElementDeclaration(schema, xpath);
                    if(elem!=null){
                        boolean simpleType = false;
                        if(elem.getTypeDefinition().getTypeCategory()==XSTypeDefinition.SIMPLE_TYPE)
                            simpleType = true;
                        else{
                            XSComplexTypeDefinition complexType = (XSComplexTypeDefinition)elem.getTypeDefinition();
                            if(complexType.getContentType()==XSComplexTypeDefinition.CONTENTTYPE_SIMPLE)
                                simpleType = true;
                        }
                        if(simpleType)
                            elementValues.put(elem, contents.toString());
                    }
                }
                xpath.remove(xpath.size()-1);
                contents.reset();
            }
        };

        SAXUtil.newSAXParser(true, false, false).parse(sampleInput, handler);
    }

    @Override
    public String generateSampleValue(XSElementDeclaration element, XSSimpleTypeDefinition simpleType){
        return elementValues.get(element);
    }

    @Override
    public String generateSampleValue(XSAttributeDeclaration attribute, XSSimpleTypeDefinition simpleType){
        return attributeValues.get(attribute);
    }
}
