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
