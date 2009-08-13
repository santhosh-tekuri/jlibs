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

package jlibs.xml.sax;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * @author Santhosh Kumar T
 */
public class SAXUtil{
    public static SAXParserFactory newSAXFactory(boolean namespaces, boolean nsPrefixes) throws ParserConfigurationException, SAXException{
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(namespaces);
        if(nsPrefixes)
            factory.setFeature(SAXFeatures.NAMESPACE_PREFIXES, true);
        return factory;
    }

    public static SAXParser newSAXParser(boolean namespaces, boolean nsPrefixes) throws ParserConfigurationException, SAXException{
        return newSAXFactory(namespaces, nsPrefixes).newSAXParser();
    }
}
