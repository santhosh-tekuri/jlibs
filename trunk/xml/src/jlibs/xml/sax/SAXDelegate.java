package jlibs.xml.sax;

import org.jetbrains.annotations.Nullable;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;

/**
 * @author Santhosh Kumar T
 */
public class SAXDelegate extends DefaultHandler{

    /*-------------------------------------------------[ ContentHandler ]---------------------------------------------------*/
    
    private @Nullable ContentHandler contentHandler;

    @Nullable
    public ContentHandler getContentHandler(){
        return contentHandler;
    }

    public void setContentHandler(@Nullable ContentHandler handler){
	    contentHandler = handler;
    }

    public void setDocumentLocator(Locator locator){
        if(contentHandler != null)
            contentHandler.setDocumentLocator(locator);
    }

    public void startDocument() throws SAXException{
        if(contentHandler != null)
            contentHandler.startDocument();
    }

    public void endDocument() throws SAXException{
        if(contentHandler != null)
            contentHandler.endDocument();
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException{
        if(contentHandler != null)
            contentHandler.startPrefixMapping(prefix, uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException{
        if(contentHandler != null)
            contentHandler.endPrefixMapping(prefix);
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException{
        if(contentHandler != null)
            contentHandler.startElement(uri, localName, qName, atts);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException{
        if(contentHandler != null)
            contentHandler.endElement(uri, localName, qName);
    }

    public void characters(char[] ch, int start, int length) throws SAXException{
        if(contentHandler != null)
            contentHandler.characters(ch, start, length);
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException{
        if(contentHandler != null)
            contentHandler.ignorableWhitespace(ch, start, length);
    }

    public void processingInstruction(String target, String data) throws SAXException{
        if(contentHandler != null)
            contentHandler.processingInstruction(target, data);
    }

    public void skippedEntity(String name) throws SAXException{
        if(contentHandler != null)
            contentHandler.skippedEntity(name);
    }

    /*-------------------------------------------------[ ErrorHandler ]---------------------------------------------------*/

    private @Nullable ErrorHandler errorHandler;

    @Nullable
    public ErrorHandler getErrorHandler(){
        return errorHandler;
    }

    public void setErrorHandler(@Nullable ErrorHandler handler){
        this.errorHandler = handler;
    }

    public void warning(SAXParseException exception) throws SAXException{
        if(errorHandler!=null)
            errorHandler.warning(exception);
    }

    public void error(SAXParseException exception) throws SAXException{
        if(errorHandler!=null)
            errorHandler.error(exception);
    }

    public void fatalError(SAXParseException exception) throws SAXException{
        if(errorHandler!=null)
            errorHandler.fatalError(exception);
    }

    /*-------------------------------------------------[ EntityResolver ]---------------------------------------------------*/

    private @Nullable EntityResolver entityResolver;

    @Nullable
    public EntityResolver getEntityResolver(){
        return entityResolver;
    }

    public void setEntityResolver(@Nullable EntityResolver entityResolver){
        this.entityResolver = entityResolver;
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException{
        if(entityResolver!=null)
            return entityResolver.resolveEntity(publicId, systemId);
        else
            return null;
    }

    /*-------------------------------------------------[ DTDHandler ]---------------------------------------------------*/

    private @Nullable DTDHandler dtdHandler;

    @Nullable
    public DTDHandler getDTDHandler(){
        return dtdHandler;
    }

    public void setDTDHandler(@Nullable DTDHandler dtdHandler){
        this.dtdHandler = dtdHandler;
    }

    public void notationDecl(String name, String publicId, String systemId) throws SAXException{
        if(dtdHandler!=null)
            dtdHandler.notationDecl(name, publicId, systemId);
    }

    public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException{
        if(dtdHandler!=null)
            dtdHandler.unparsedEntityDecl(name, publicId, systemId, notationName);
    }

    /*-------------------------------------------------[ Helpers ]---------------------------------------------------*/

    public void setDefaultHandler(DefaultHandler handler){
        setContentHandler(handler);
        setEntityResolver(handler);
        setErrorHandler(handler);
        setDTDHandler(handler);
    }
}
