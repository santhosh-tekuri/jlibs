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

package jlibs.xml.sax;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A SAXDelegate that prints sax event information before delgating.
 * This is useful for debugging purposes.
 * 
 * @author Santhosh Kumar T
 */
public class SAXDebugHandler extends SAXDelegate{
    public SAXDebugHandler(DefaultHandler delegate){
        super(delegate);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
        System.out.print("<"+qName);
        for(int i=0; i<attributes.getLength(); i++)
            System.out.format(" %s='%s'", attributes.getQName(i), attributes.getValue(i));
        System.out.print(">");
        super.startElement(uri, localName, qName, attributes);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException{
        System.out.print(new String(ch, start, length));
        super.characters(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException{
        System.out.print("</"+qName+">");
        super.endElement(uri, localName, qName);
    }
}
