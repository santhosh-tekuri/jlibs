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
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;

import java.io.IOException;

/**
 * This class implements all sax handler interfaces and delegates them
 * to the registered handlers
 *
 * @author Santhosh Kumar T
 */
public class SAXDelegate implements EntityResolver, DTDHandler, ContentHandler, ErrorHandler, LexicalHandler, DeclHandler{
    public SAXDelegate(){}

    public SAXDelegate(Object delegate){
        setHandler(delegate);
    }

    /**
     * Registers given handler with all its implementing interfaces.
     * This would be handy if you want to register to all interfaces
     * implemented by given handler object
     *
     * @param handler Object implementing one or more sax handler interfaces
     */
    public void setHandler(Object handler){
        if(handler instanceof ContentHandler)
            setContentHandler((ContentHandler)handler);
        if(handler instanceof EntityResolver)
            setEntityResolver((EntityResolver)handler);
        if(handler instanceof ErrorHandler)
            setErrorHandler((ErrorHandler)handler);
        if(handler instanceof DTDHandler)
            setDTDHandler((DTDHandler)handler);
        if(handler instanceof LexicalHandler)
            setLexicalHandler((LexicalHandler)handler);
        if(handler instanceof DeclHandler)
            setDeclHandler((DeclHandler)handler);
    }

    /*-------------------------------------------------[ ContentHandler ]---------------------------------------------------*/

    private ContentHandler contentHandler;

    public ContentHandler getContentHandler(){
        return contentHandler;
    }

    public void setContentHandler(ContentHandler handler){
	    contentHandler = handler;
    }

    @Override
    public void setDocumentLocator(Locator locator){
        if(contentHandler != null)
            contentHandler.setDocumentLocator(locator);
    }

    @Override
    public void startDocument() throws SAXException{
        if(contentHandler != null)
            contentHandler.startDocument();
    }

    @Override
    public void endDocument() throws SAXException{
        if(contentHandler != null)
            contentHandler.endDocument();
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException{
        if(contentHandler != null)
            contentHandler.startPrefixMapping(prefix, uri);
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException{
        if(contentHandler != null)
            contentHandler.endPrefixMapping(prefix);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException{
        if(contentHandler != null)
            contentHandler.startElement(uri, localName, qName, atts);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException{
        if(contentHandler != null)
            contentHandler.endElement(uri, localName, qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException{
        if(contentHandler != null)
            contentHandler.characters(ch, start, length);
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException{
        if(contentHandler != null)
            contentHandler.ignorableWhitespace(ch, start, length);
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException{
        if(contentHandler != null)
            contentHandler.processingInstruction(target, data);
    }

    @Override
    public void skippedEntity(String name) throws SAXException{
        if(contentHandler != null)
            contentHandler.skippedEntity(name);
    }

    /*-------------------------------------------------[ ErrorHandler ]---------------------------------------------------*/

    private ErrorHandler errorHandler;

    public ErrorHandler getErrorHandler(){
        return errorHandler;
    }

    public void setErrorHandler(ErrorHandler handler){
        this.errorHandler = handler;
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException{
        if(errorHandler!=null)
            errorHandler.warning(exception);
    }

    @Override
    public void error(SAXParseException exception) throws SAXException{
        if(errorHandler!=null)
            errorHandler.error(exception);
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException{
        if(errorHandler!=null)
            errorHandler.fatalError(exception);
    }

    /*-------------------------------------------------[ EntityResolver ]---------------------------------------------------*/

    private EntityResolver entityResolver;

    public EntityResolver getEntityResolver(){
        return entityResolver;
    }

    public void setEntityResolver(EntityResolver entityResolver){
        this.entityResolver = entityResolver;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException{
        if(entityResolver!=null)
            return entityResolver.resolveEntity(publicId, systemId);
        else
            return null;
    }

    /*-------------------------------------------------[ DTDHandler ]---------------------------------------------------*/

    private DTDHandler dtdHandler;

    public DTDHandler getDTDHandler(){
        return dtdHandler;
    }

    public void setDTDHandler(DTDHandler dtdHandler){
        this.dtdHandler = dtdHandler;
    }

    @Override
    public void notationDecl(String name, String publicId, String systemId) throws SAXException{
        if(dtdHandler!=null)
            dtdHandler.notationDecl(name, publicId, systemId);
    }

    @Override
    public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException{
        if(dtdHandler!=null)
            dtdHandler.unparsedEntityDecl(name, publicId, systemId, notationName);
    }

    /*-------------------------------------------------[ LexicalHandler ]---------------------------------------------------*/

    private LexicalHandler lexicalHandler;

    public LexicalHandler getLexicalHandler(){
        return lexicalHandler;
    }

    public void setLexicalHandler(LexicalHandler lexicalHandler){
        this.lexicalHandler = lexicalHandler;
    }

    @Override
    public void startDTD(String name, String publicId, String systemId) throws SAXException{
        if(lexicalHandler!=null)
            lexicalHandler.startDTD(name, publicId, systemId);
    }

    @Override
    public void endDTD() throws SAXException{
        if(lexicalHandler!=null)
            lexicalHandler.endDTD();
    }

    @Override
    public void startEntity(String name) throws SAXException{
        if(lexicalHandler!=null)
            lexicalHandler.startEntity(name);
    }

    @Override
    public void endEntity(String name) throws SAXException{
        if(lexicalHandler!=null)
            lexicalHandler.endEntity(name);
    }

    @Override
    public void startCDATA() throws SAXException{
        if(lexicalHandler!=null)
            lexicalHandler.startCDATA();
    }

    @Override
    public void endCDATA() throws SAXException{
        if(lexicalHandler!=null)
            lexicalHandler.endCDATA();
    }

    @Override
    public void comment(char[] ch, int start, int length) throws SAXException{
        if(lexicalHandler!=null)
            lexicalHandler.comment(ch, start, length);
    }

    /*-------------------------------------------------[ DeclHandler ]---------------------------------------------------*/

    private DeclHandler declHandler;

    public DeclHandler getDeclHandler(){
        return declHandler;
    }

    public void setDeclHandler(DeclHandler declHandler){
        this.declHandler = declHandler;
    }

    @Override
    public void elementDecl(String name, String model) throws SAXException{
        if(declHandler!=null)
            declHandler.elementDecl(name, model);
    }

    @Override
    public void attributeDecl(String eName, String aName, String type, String mode, String value) throws SAXException{
        if(declHandler!=null)
            declHandler.attributeDecl(eName, aName, type, mode, value);
    }

    @Override
    public void internalEntityDecl(String name, String value) throws SAXException{
        if(declHandler!=null)
            declHandler.internalEntityDecl(name, value);
    }

    @Override
    public void externalEntityDecl(String name, String publicId, String systemId) throws SAXException{
        if(declHandler!=null)
            declHandler.externalEntityDecl(name, publicId, systemId);
    }
}
