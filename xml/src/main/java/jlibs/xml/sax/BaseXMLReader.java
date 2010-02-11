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
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.ext.DeclHandler;
import static jlibs.xml.sax.SAXProperties.*;
import static jlibs.xml.sax.SAXProperties.DECL_HANDLER_ALT;

/**                                                                 x
 * Base class for xmlreader and xmlfilter implementations
 *  
 * @author Santhosh Kumar T
 */
public abstract class BaseXMLReader implements XMLReader{
    protected BaseXMLReader(SAXDelegate handler){
        this.handler = handler;
    }

    protected BaseXMLReader(){
        this(new SAXDelegate());
    }

    /*-------------------------------------------------[ Properties ]---------------------------------------------------*/

    protected boolean _setProperty(String name, Object value) throws SAXNotSupportedException{
        if(LEXICAL_HANDLER.equals(name) || LEXICAL_HANDLER_ALT.equals(name)){
            if(value==null || value instanceof LexicalHandler)
                handler.setLexicalHandler((LexicalHandler)value);
            else
                throw new SAXNotSupportedException("value must implement "+LexicalHandler.class);
        }else if(DECL_HANDLER.equals(name) || DECL_HANDLER_ALT.equals(name)){
            if(value==null || value instanceof DeclHandler)
                handler.setDeclHandler((DeclHandler)value);
            else
                throw new SAXNotSupportedException("value must implement "+DeclHandler.class);
        }
        return false;
    }

    protected Object _getProperty(String name){
        if(LEXICAL_HANDLER.equals(name) || LEXICAL_HANDLER_ALT.equals(name))
            return handler.getLexicalHandler();
        else if(DECL_HANDLER.equals(name) || DECL_HANDLER_ALT.equals(name))
            return handler.getDeclHandler();
        return null;
    }

    /*-------------------------------------------------[ Handlers ]---------------------------------------------------*/

    protected final SAXDelegate handler;

    @Override
    public void setEntityResolver(EntityResolver resolver){
        handler.setEntityResolver(resolver);
    }

    @Override
    public EntityResolver getEntityResolver(){
        return handler.getEntityResolver();
    }

    @Override
    public void setDTDHandler(DTDHandler dtdHandler){
        handler.setDTDHandler(dtdHandler);
    }

    @Override
    public DTDHandler getDTDHandler(){
        return handler.getDTDHandler();
    }

    @Override
    public void setContentHandler(ContentHandler contentHandler){
        handler.setContentHandler(contentHandler);
    }

    @Override
    public ContentHandler getContentHandler(){
        return handler.getContentHandler();
    }

    @Override
    public void setErrorHandler(ErrorHandler errorHandler){
        handler.setErrorHandler(errorHandler);

    }

    @Override
    public ErrorHandler getErrorHandler(){
        return handler.getErrorHandler();
    }
}
