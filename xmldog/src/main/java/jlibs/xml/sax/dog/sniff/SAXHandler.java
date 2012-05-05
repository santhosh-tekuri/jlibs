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

import jlibs.xml.Namespaces;
import jlibs.xml.sax.SAXUtil;
import jlibs.xml.sax.helpers.MyNamespaceSupport;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * @author Santhosh Kumar T
 */
public final class SAXHandler extends DefaultHandler2{
    final Event event;
    final boolean langInterested;

    public SAXHandler(Event event, boolean langInterested){
        this.event = event;
        this.langInterested = langInterested;
    }

    public void startDocument() throws SAXException{
        nsSupport.startDocument();
        event.onStartDocument();
    }

    private final MyNamespaceSupport nsSupport = new MyNamespaceSupport();

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException{
        nsSupport.startPrefixMapping(prefix, uri);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException{
        nsSupport.startElement();

        Event event = this.event;
        event.onText();
        event.onStartElement(uri, localName, qName, langInterested ? attrs.getValue(Namespaces.URI_XML, "lang") : null);
        event.onNamespaces(nsSupport);
        event.onAttributes(attrs);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException{
        event.appendText(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException{
        nsSupport.endElement();

        event.onText();
        event.onEndElement();
    }

    @Override
    public void endDocument() throws SAXException{
        event.onEndDocument();
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException{
        event.onText();
        event.onPI(target, data);
    }

    @Override
    public void comment(char[] ch, int start, int length) throws SAXException{
        event.onText();
        event.onComment(ch, start, length);
    }

    private static SAXParserFactory factory;
    private static final ThreadLocal<SAXParser> saxParserLocal = new ThreadLocal<SAXParser>();

    private SAXParser getParser() throws ParserConfigurationException, SAXException{
        SAXParser parser = saxParserLocal.get();
        if(parser==null){
            if(factory==null)
                factory = SAXUtil.newSAXFactory(true, false, false);
            parser = factory.newSAXParser();
            saxParserLocal.set(parser);
        }
        return parser;
    }
}
