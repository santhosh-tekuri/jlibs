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

package jlibs.xml.sax.helpers;

import jlibs.xml.sax.SAXProperties;
import jlibs.xml.sax.SAXUtil;
import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * @author Santhosh Kumar T
 */
public class NamespaceSupportReader extends XMLFilterImpl{
    protected MyNamespaceSupport nsSupport = new MyNamespaceSupport();

    public NamespaceSupportReader(boolean nsPrefixes) throws ParserConfigurationException, SAXException{
        this(SAXUtil.newSAXParser(true, nsPrefixes, false).getXMLReader());
    }

    public NamespaceSupportReader(XMLReader parent){
        super(parent);
    }

    public MyNamespaceSupport getNamespaceSupport(){
        return nsSupport;
    }

    @Override
    public void startDocument() throws SAXException{
        nsSupport.startDocument();
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException{
        nsSupport.startPrefixMapping(prefix, uri);
        super.startPrefixMapping(prefix, uri);
    }

    public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes atts) throws SAXException{
        nsSupport.startElement();
        super.startElement(namespaceURI, localName, qualifiedName, atts);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException{
        nsSupport.endElement();
        super.endElement(uri, localName, qName);
    }

    public void setDefaultHandler(DefaultHandler handler) throws SAXNotSupportedException, SAXNotRecognizedException{
        if(handler instanceof SAXHandler)
            ((SAXHandler)handler).nsSupport = nsSupport;

        setContentHandler(handler);
        setEntityResolver(handler);
        setErrorHandler(handler);
        setDTDHandler(handler);
        if(handler instanceof LexicalHandler)
            setProperty(SAXProperties.LEXICAL_HANDLER, handler);
    }

    /*-------------------------------------------------[ Parsing ]---------------------------------------------------*/

    public void parse(InputSource is, DefaultHandler handler) throws IOException, SAXException{
        setDefaultHandler(handler);
        parse(is);
    }

    public void parse(String systemId, DefaultHandler handler) throws IOException, SAXException{
        setDefaultHandler(handler);
        parse(systemId);
    }
}
