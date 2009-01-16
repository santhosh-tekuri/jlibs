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
import jlibs.xml.sax.sniff.model.Root;
import jlibs.xml.sax.sniff.events.*;
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
        element = new Element(elementStack);
        if(debug)
            root.print();
    }

    private StringContent contents = new StringContent();
    private ContextManager contextManager;
    private PositionStack positionStack = new PositionStack();
    private ElementStack elementStack;

    // events
    private Element element;
    private Text text = new Text(contents);
    private Comment comment = new Comment();
    private PI pi = new PI();

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

        contents.reset();
        contextManager.reset(root);
        positionStack.reset();
        elementStack.reset();
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
        
        int pos = positionStack.push(uri, localName);
        elementStack.push(uri, localName, pos);

        matchText();

        element.setData(uri, localName);
        contextManager.match(element);

        contextManager.matchAttributes(attrs);

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
        
        positionStack.pop();
        elementStack.pop();

        matchText();
        contextManager.elementEnded();

        if(debug)
            System.out.println("-----------------------------------------------------------------");
    }

    /*-------------------------------------------------[ Sniffing ]---------------------------------------------------*/

    public XPathResults sniff(InputSource source, int minHits) throws ParserConfigurationException, SAXException, IOException{
        try{
            contextManager = new ContextManager(minHits);
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
        if(debug)
            System.out.println("max contexts: "+contextManager.maxInstCount);
        return contextManager.results;
    }
}
