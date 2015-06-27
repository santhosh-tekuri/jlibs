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

import java.io.IOException;

/**
 * XMLFilter implementation using SAXDelegate
 *
 * @author Santhosh Kumar T
 */
public class MyXMLFilter extends BaseXMLReader implements XMLFilter{
    public MyXMLFilter(SAXDelegate handler){
        super(handler);
    }

    public MyXMLFilter(SAXDelegate handler, XMLReader parent){
        super(handler);
        setParent(parent);
    }

    /*-------------------------------------------------[ Features ]---------------------------------------------------*/

    @Override
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException{
        return parent.getFeature(name);
    }

    @Override
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException{
        parent.setFeature(name, value);
    }

    /*-------------------------------------------------[ Properties ]---------------------------------------------------*/

    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException{
        Object value = _getProperty(name);
        return value==null ? parent.getProperty(name) : value;
    }

    @Override
    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException{
        if(!_setProperty(name, value))
            parent.setProperty(name, value);
    }

    /*-------------------------------------------------[ Parent ]---------------------------------------------------*/

    private XMLReader parent;

    @Override
    public void setParent(XMLReader parent){
        this.parent = parent;
    }

    @Override
    public XMLReader getParent(){
        return parent;
    }

    /*-------------------------------------------------[ Parsing ]---------------------------------------------------*/

    private void setupParsing() throws SAXException{
        parent.setEntityResolver(handler);
        parent.setDTDHandler(handler);
        parent.setContentHandler(handler);
        parent.setErrorHandler(handler);
        parent.setProperty(SAXProperties.LEXICAL_HANDLER, handler);
        parent.setProperty(SAXProperties.DECL_HANDLER_ALT, handler);
    }

    @Override
    public void parse(InputSource input) throws IOException, SAXException{
        setupParsing();
        parent.parse(input);
    }

    @Override
    public void parse(String systemId) throws IOException, SAXException{
        setupParsing();
        parent.parse(systemId);
    }
}
