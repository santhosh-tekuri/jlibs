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

import static jlibs.xml.sax.SAXProperties.*;

/**
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
            if(value==null || value instanceof LexicalHandler){
                handler.setLexicalHandler((LexicalHandler)value);
                return true;
            }else
                throw new SAXNotSupportedException("value must implement "+LexicalHandler.class);
        }else if(DECL_HANDLER.equals(name) || DECL_HANDLER_ALT.equals(name)){
            if(value==null || value instanceof DeclHandler){
                handler.setDeclHandler((DeclHandler)value);
                return true;
            }else
                throw new SAXNotSupportedException("value must implement "+DeclHandler.class);
        }else
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
