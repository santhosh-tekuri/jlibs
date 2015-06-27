/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
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
