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
