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

import jlibs.core.lang.ImpossibleException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * @author Santhosh Kumar T
 */
public class SAXUtil{
    public static SAXParser newSAXParser(boolean namespaces, boolean nsPrefixes) throws ParserConfigurationException, SAXException{
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(namespaces);
        try{
            if(nsPrefixes)
            factory.setFeature(SAXFeatures.NAMESPACE_PREFIXES, true);
        }catch(Exception ex){
            throw new ImpossibleException(factory+" is not SAX-Compliant as it doesn't support namespace-prefixes feature");
        }
        return factory.newSAXParser();
    }
}
