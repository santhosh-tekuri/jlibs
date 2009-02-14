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

package jlibs.xml.sax.sniff.engine;

import jlibs.xml.Namespaces;
import jlibs.xml.sax.SAXDebugHandler;
import jlibs.xml.sax.SAXProperties;
import jlibs.xml.sax.SAXUtil;
import jlibs.xml.sax.helpers.NamespaceSupportReader;
import jlibs.xml.sax.sniff.Debuggable;
import jlibs.xml.sax.sniff.engine.context.ContextManager;
import jlibs.xml.sax.sniff.engine.data.LocationStack;
import jlibs.xml.sax.sniff.engine.data.StringContent;
import jlibs.xml.sax.sniff.events.*;
import jlibs.xml.sax.sniff.model.HitManager;
import jlibs.xml.sax.sniff.model.Root;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import java.io.IOException;

/**
 * @author Santhosh Kumar T
 */
public class Sniffer extends DefaultHandler2 implements Debuggable{
    private Root root;

    public Sniffer(Root root){
        this.root = root;
        locationStack = new LocationStack(root.nsContext);
        element = new Element(documentOrder, locationStack);
    }

    private NamespaceSupportReader nsSupportReader = new NamespaceSupportReader(null);
    private StringContent contents = new StringContent();
    private ContextManager contextManager = new ContextManager();
    private LocationStack locationStack;

    // events
    private DocumentOrder documentOrder = new DocumentOrder();
    private Start start = new Start(documentOrder);
    private Document document = new Document(documentOrder);
    private Element element;
    private Attribute attribute = new Attribute(documentOrder);
    private Namespace namespace = new Namespace(documentOrder);
    private Text text = new Text(documentOrder, contents);
    private Comment comment = new Comment(documentOrder);
    private PI pi = new PI(documentOrder);

    private void matchText(){
        if(!contents.isEmpty()){
            text.setData();
            contextManager.match(text);
            contents.reset();
        }
    }
    
    /*-------------------------------------------------[ Events ]---------------------------------------------------*/
    
    @Override
    public void startDocument() throws SAXException{
        nsSupportReader.startDocument();
        if(debug)
            System.out.println("-----------------------------------------------------------------");

        document.setData();
        contextManager.match(document);

        if(debug)
            System.out.println("-----------------------------------------------------------------");
    }


    @Override
    public void processingInstruction(String target, String data) throws SAXException{
        matchText();

        pi.setData(target, data);
        contextManager.match(pi);
    }

    @Override
    public void comment(char[] ch, int start, int length) throws SAXException{
        matchText();
        
        comment.setData(ch, start, length);
        contextManager.match(comment);
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException{
        nsSupportReader.startPrefixMapping(prefix, uri);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException{
        nsSupportReader.startElement(uri, localName, qName, attrs);
        
        if(debug)
            System.out.println();
        
        locationStack.pushElement(uri, localName, attrs.getValue(Namespaces.URI_XML, "lang"));

        matchText();

        element.setData(uri, localName, qName);
        contextManager.match(element);

        contextManager.matchNamespaces(namespace, nsSupportReader.getNamespaceSupport());
        contextManager.matchAttributes(attribute, attrs);

        if(debug)
            System.out.println("-----------------------------------------------------------------");
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException{
        contents.write(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException{
        nsSupportReader.endElement(uri, localName, qName);
        
        if(debug)
            System.out.println();
        
        locationStack.popElement();

        matchText();
        contextManager.elementEnded();

        if(debug)
            System.out.println("-----------------------------------------------------------------");
    }

    @Override
    public void endDocument() throws SAXException{
        contextManager.documentEnded();
    }

    /*-------------------------------------------------[ Sniffing ]---------------------------------------------------*/

    private void reset(){
        if(debug)
            System.out.println("-----------------------------------------------------------------");

        documentOrder.reset();
        contents.reset();
        contextManager.reset(root, start);
        locationStack.reset();
    }

    public void sniff(InputSource source) throws ParserConfigurationException, SAXException, IOException{
        try{
            reset();
            DefaultHandler handler = this;
            if(debug)
                handler = new SAXDebugHandler(handler);
            SAXParser parser = SAXUtil.newSAXParser(true, false);
            parser.getXMLReader().setProperty(SAXProperties.LEXICAL_HANDLER, handler);
            parser.parse(source, handler);
        }catch(RuntimeException ex){
            if(ex!=HitManager.STOP_PARSING)
                throw ex;
            if(debug)
                System.out.println("COMPLETE DOCUMENT IS NOT PARSED !!!");
        }
    }
}
