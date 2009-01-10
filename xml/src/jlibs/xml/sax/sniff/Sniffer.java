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

import jlibs.xml.ClarkName;
import jlibs.xml.sax.SAXDebugHandler;
import jlibs.xml.sax.SAXUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.*;

/**
 * @author Santhosh Kumar T
 */
class Sniffer extends DefaultHandler{
    private Root root;

    public Sniffer(Root root){
        this.root = root;
    }

    protected CharArrayWriter contents = new CharArrayWriter();
    private StringContent text = new StringContent(contents);

    /*-------------------------------------------------[ Context ]---------------------------------------------------*/

    private Set<Node> context = new HashSet<Node>();
    private Set<Node> newContext = new HashSet<Node>();


    private void updateContext(){
        Set<Node> temp = context;
        context = newContext;
        newContext = temp;
        newContext.clear();
    }

    private void println(String message, Collection<Node> context){
        if(XMLDog.debug){
            System.out.println(message+" ->");
            if(context.size()==0)
                System.out.println("    Empty");
            else{
                for(Node node: context){
                    System.out.print("    ");
                    node.println();
                }
            }
            System.out.println();
        }
    }

    /*-------------------------------------------------[ Predicates ]---------------------------------------------------*/

    private ArrayDeque<Map<String, Integer>> predicateStack = new ArrayDeque<Map<String, Integer>>();

    private int updatePredicate(String uri, String localName){
        Map<String, Integer> map = predicateStack.peekFirst();
        String clarkName = ClarkName.valueOf(uri, localName);
        Integer predicate = map.get(clarkName);
        if(predicate==null)
            predicate = 1;
        else
            predicate += 1;
        map.put(clarkName, predicate);

        predicateStack.addFirst(new HashMap<String, Integer>());
        
        return predicate;
    }

    /*-------------------------------------------------[ Events ]---------------------------------------------------*/
    
    @Override
    public void startDocument() throws SAXException{
        context.clear();
        newContext.clear();
        predicateStack.clear();
        
        root.reset();
        context.add(root);

        for(Node autoMatchNode: root.findAutoMatches())
            context.add(autoMatchNode);

        contents.reset();
        text.reset();

        predicateStack.push(new HashMap<String, Integer>());
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
        int predicate = updatePredicate(uri, localName);

        if(XMLDog.debug)
            System.out.println("\npredicate: "+predicate);
        println("current context", context);
        
        text.reset();
        for(Node current: context){
            if(XMLDog.debug){
                System.out.print("matching -> \n    ");
                current.println();
            }

            current.matchText(text);
            List<Node> list = current.matchStartElement(uri, localName, predicate);

            println("matched", list);

            for(Node node: list)
                node.matchAttributes(attributes);
            newContext.addAll(list);

            if(XMLDog.debug)
                System.out.println();
        }
        println("new context", newContext);
        
        updateContext();
        text.reset();
        contents.reset();
        if(XMLDog.debug)
            System.out.println("-----------------------------------------------------");
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException{
        contents.write(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException{
        predicateStack.removeFirst();
        text.reset();
        println("\ncurrent context", context);
        for(Node current: context){
            if(XMLDog.debug)
                System.out.print("matching -> \n    ");
            current.matchText(text);
            newContext.add(current.matchEndElement());
            println("new context", newContext);
        }
        updateContext();
        text.reset();
        contents.reset();
        if(XMLDog.debug)
            System.out.println("-----------------------------------------------------");
    }

    public void sniff(InputSource source) throws ParserConfigurationException, SAXException, IOException{
        try{
            DefaultHandler handler = this;
            if(XMLDog.debug)
                handler = new SAXDebugHandler(handler);
            SAXUtil.newSAXParser(true, false).parse(source, handler);
        }catch(RuntimeException ex){
            if(ex!=Root.EVALUATION_FINISHED)
                throw ex;
        }
    }
}
