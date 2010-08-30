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

package jlibs.xml.stream;

import jlibs.xml.sax.AbstractXMLReader;
import jlibs.xml.sax.SAXDelegate;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.EntityDeclaration;
import javax.xml.stream.events.NotationDeclaration;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static javax.xml.stream.XMLStreamConstants.*;

/**
 * {@link org.xml.sax.XMLReader} implementation using STAX.
 * <p>
 * This class also provides handy utility method {@link #fire(javax.xml.stream.XMLStreamReader, jlibs.xml.sax.SAXDelegate) fire(...)}
 * to translate STAX events to SAX events.
 * 
 * <pre class="prettyprint">
 * XMLStreamReader reader = ...;
 * SAXDelegate delegate = new SAXDelegate();
 * 
 * // set any handlers you are interested
 * delegate.setContentHandler(myContentHandler);
 * delegate.setErrorHandler(myErrorHandler);
 * 
 * STAXXMLReader.fire(reader, delegate);
 * </pre> 
 * 
 * @author Santhosh Kumar T
 */
public class STAXXMLReader extends AbstractXMLReader{
    private XMLInputFactory factory;

    public STAXXMLReader(XMLInputFactory factory){
        this.factory = factory;
    }

    public STAXXMLReader(){
        this(XMLInputFactory.newInstance());
    }

    @Override
    public void parse(InputSource input) throws IOException, SAXException{
        XMLStreamReader reader = null;
        try{
            if(input.getByteStream()!=null)
                reader = factory.createXMLStreamReader(input.getByteStream(), input.getEncoding());
            else if(input.getCharacterStream()!=null)
                reader = factory.createXMLStreamReader(input.getCharacterStream());
            else
                reader = factory.createXMLStreamReader(input.getSystemId(), (InputStream)null);
            fire(reader, handler);
        }catch(XMLStreamException ex){
            throw new SAXException(ex);
        }finally{
            try{
                if(reader!=null)
                    reader.close();
            }catch(XMLStreamException ex){
                //noinspection ThrowFromFinallyBlock
                throw new SAXException(ex);
            }
        }
    }

    @Override
    public void parse(String systemId) throws IOException, SAXException{
        parse(new InputSource(systemId));
    }

    /**
     * Reads data from specified {@code reader}, and delegates translated SAX Events
     * to {@code handler}.
     * <p>
     * <b>Note:</b> The {@code reader} is not closed by this method.
     * 
     * @param reader    reader to reads data from
     * @param handler   the SAXHandler which receives SAX events
     * 
     * @throws SAXException any {@link XMLStreamException} occured is rethrown as {@link SAXException}
     */
    @SuppressWarnings({"unchecked"})
    public static void fire(XMLStreamReader reader, SAXDelegate handler) throws SAXException{
        Attributes attrs = new STAXAttributes(reader);
        int eventType = reader.getEventType();
        while(true){
            switch(eventType){
                case START_DOCUMENT:
                    handler.setDocumentLocator(new STAXLocator(reader));
                    handler.startDocument();
                    break;
                case START_ELEMENT:{
                    int nsCount = reader.getNamespaceCount();
                    for(int i=0; i<nsCount; i++){
                        String prefix = reader.getNamespacePrefix(i);
                        String uri = reader.getNamespaceURI(i);
                        handler.startPrefixMapping(prefix==null?"":prefix, uri==null?"":uri);
                    }

                    String localName = reader.getLocalName();
                    String prefix = reader.getPrefix();
                    String qname = prefix==null || prefix.length()==0 ? localName : prefix+':'+localName;
                    String uri = reader.getNamespaceURI();
                    handler.startElement(uri==null?"":uri, localName, qname, attrs);
                    break;
                }
                case CHARACTERS:
                    handler.characters(reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
                    break;
                case CDATA:
                    handler.startCDATA();
                    handler.characters(reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
                    handler.endCDATA();
                    break;
                case COMMENT:
                    handler.comment(reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
                    break;
                case PROCESSING_INSTRUCTION:
                    handler.processingInstruction(reader.getPITarget(), reader.getPIData());
                    break;
                case SPACE:
                    handler.ignorableWhitespace(reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
                    break;
                case DTD:
                    for(NotationDeclaration notation: (List<NotationDeclaration>)reader.getProperty("javax.xml.stream.notations"))
                        handler.notationDecl(notation.getName(), notation.getPublicId(), notation.getSystemId());
                    for(EntityDeclaration entity: (List<EntityDeclaration>)reader.getProperty("javax.xml.stream.entities"))
                        handler.unparsedEntityDecl(entity.getName(), entity.getPublicId(), entity.getSystemId(), entity.getNotationName());
                    break;
                case END_ELEMENT:{
                    String localName = reader.getLocalName();
                    String prefix = reader.getPrefix();
                    String qname = prefix==null || prefix.length()==0 ? localName : prefix+':'+localName;
                    String uri = reader.getNamespaceURI();
                    handler.endElement(uri==null?"":uri, localName, qname);

                    int nsCount = reader.getNamespaceCount();
                    for(int i=0; i<nsCount; i++){
                        prefix = reader.getNamespacePrefix(i);
                        handler.endPrefixMapping(prefix==null?"":prefix);
                    }
                    break;
                }
                case END_DOCUMENT:
                    handler.endDocument();
                    return;
            }
            eventType = reader.getEventType();
        }
    }
}
