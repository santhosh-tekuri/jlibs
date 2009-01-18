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

package jlibs.xml.sax.sniff;

import jlibs.xml.sax.SAXDebugHandler;
import jlibs.xml.sax.SAXProperties;
import jlibs.xml.sax.SAXUtil;
import jlibs.xml.sax.sniff.events.*;
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
        elementStack = new ElementStack(root);
        element = new Element(documentOrder, elementStack);

        if(debug)
            root.print();
    }

    private StringContent contents = new StringContent();
    private ContextManager contextManager = new ContextManager();
    private ElementStack elementStack;

    // events
    private DocumentOrder documentOrder = new DocumentOrder();
    private Document document = new Document(documentOrder);
    private Element element;
    private Attribute attribute = new Attribute(documentOrder);
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
        if(debug)
            System.out.println("-----------------------------------------------------------------");

        document.setData();
        contextManager.match(document);
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
    public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException{
        if(debug)
            System.out.println();
        
        elementStack.push(uri, localName);

        matchText();

        element.setData(uri, localName, qName);
        contextManager.match(element);

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
        if(debug)
            System.out.println();
        
        elementStack.pop();

        matchText();
        contextManager.elementEnded();

        if(debug)
            System.out.println("-----------------------------------------------------------------");
    }

    /*-------------------------------------------------[ Sniffing ]---------------------------------------------------*/

    private void reset(XPathResults results){
        if(debug)
            System.out.println("-----------------------------------------------------------------");

        documentOrder.reset();
        contents.reset();
        contextManager.reset(root, results);
        elementStack.reset();
    }

    public XPathResults sniff(InputSource source, int minHits) throws ParserConfigurationException, SAXException, IOException{
        XPathResults results = new XPathResults(root, documentOrder, minHits);
        reset(results);
        try{
            DefaultHandler handler = this;
            if(debug)
                handler = new SAXDebugHandler(handler);
            SAXParser parser = SAXUtil.newSAXParser(true, false);
            parser.getXMLReader().setProperty(SAXProperties.LEXICAL_HANDLER, handler);
            parser.parse(source, handler);
        }catch(RuntimeException ex){
            if(ex!=XPathResults.STOP_PARSING)
                throw ex;
            if(debug)
                System.out.println("COMPLETE DOCUMENT IS NOT PARSED !!!");
        }
        
        return results;
    }
}
