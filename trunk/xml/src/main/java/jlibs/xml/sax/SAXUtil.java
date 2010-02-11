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

import org.xml.sax.*;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * @author Santhosh Kumar T
 */
public class SAXUtil{
    public static SAXParserFactory newSAXFactory(boolean namespaces, boolean nsPrefixes, boolean validating) throws ParserConfigurationException, SAXException{
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(namespaces);
        if(nsPrefixes)
            factory.setFeature(SAXFeatures.NAMESPACE_PREFIXES, true);
        factory.setValidating(validating);
        return factory;
    }

    public static SAXParser newSAXParser(boolean namespaces, boolean nsPrefixes, boolean validating) throws ParserConfigurationException, SAXException{
        return newSAXFactory(namespaces, nsPrefixes, validating).newSAXParser();
    }

    /**
     * Registers all sax hander interfaces implemented by <code>handler</code> to the
     * specified <code>reader</reader>
     */
    public static void setHandler(XMLReader reader, Object handler) throws SAXNotSupportedException, SAXNotRecognizedException{
        if(handler instanceof ContentHandler)
            reader.setContentHandler((ContentHandler)handler);
        if(handler instanceof EntityResolver)
            reader.setEntityResolver((EntityResolver)handler);
        if(handler instanceof ErrorHandler)
            reader.setErrorHandler((ErrorHandler)handler);
        if(handler instanceof DTDHandler)
            reader.setDTDHandler((DTDHandler)handler);
        if(handler instanceof LexicalHandler){
            try{
                reader.setProperty(SAXProperties.LEXICAL_HANDLER, handler);
            }catch(SAXException ex){
                reader.setProperty(SAXProperties.LEXICAL_HANDLER_ALT, handler);
            }
        }
        if(handler instanceof DeclHandler){
            try{
                reader.setProperty(SAXProperties.DECL_HANDLER, handler);
            }catch(SAXException ex){
                reader.setProperty(SAXProperties.DECL_HANDLER_ALT, handler);
            }
        }
    }
}
