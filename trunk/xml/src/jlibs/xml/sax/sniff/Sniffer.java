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

import jlibs.xml.sax.SAXUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Santhosh Kumar T
 */
class Sniffer extends DefaultHandler{
    private Root root;

    public Sniffer(Root root){
        this.root = root;
    }

    private Set<Node> context = new HashSet<Node>();
    protected CharArrayWriter contents = new CharArrayWriter();
    private StringContent text = new StringContent(contents);

    @Override
    public void startDocument() throws SAXException{
        context.clear();

        root.reset();
        context.add(root);
        Descendant desc = root.findDescendant(true);
        if(desc!=null)
            context.add(desc);

        contents.reset();
        text.reset();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
        //System.out.println("<"+localName+">");
        Set<Node> newContext = new HashSet<Node>();

        text.reset();
        for(Node current: context){
            current.matchText(text);
            List<Node> list = current.matchStartElement(uri, localName);
            for(Node node: list)
                node.matchAttributes(attributes);
            newContext.addAll(list);
        }
        text.reset();
        contents.reset();
        context = newContext;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException{
        contents.write(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException{
        //System.out.println("</"+localName+">");
        Set<Node> newContext = new HashSet<Node>();
        text.reset();
        for(Node current: context){
            current.matchText(text);
            newContext.add(current.matchEndElement());
        }
        text.reset();
        contents.reset();
        context = newContext;
    }

    public void sniff(InputSource source) throws ParserConfigurationException, SAXException, IOException{
        try{
            SAXUtil.newSAXParser(true, false).parse(source, this);
        }catch(RuntimeException ex){
            if(ex!=Root.EVALUATION_FINISHED)
                throw ex;
        }
    }
}
