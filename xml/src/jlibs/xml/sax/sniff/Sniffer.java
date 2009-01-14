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
import jlibs.xml.sax.SAXUtil;
import jlibs.xml.sax.SAXProperties;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.Root;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayDeque;

/**
 * @author Santhosh Kumar T
 */
public class Sniffer extends DefaultHandler2 implements Debuggable{
    private Root root;

    public Sniffer(Root root){
        this.root = root;
        elementStack = new ElementStack(root);
        if(debug)
            root.print();
    }

    private StringContent contents = new StringContent();
    private Contexts contexts = new Contexts();
    private PositionStack positionStack = new PositionStack();
    private ElementStack elementStack;
    private XPathResults results;

    /*-------------------------------------------------[ Events ]---------------------------------------------------*/
    
    @Override
    public void startDocument() throws SAXException{
        if(debug)
            System.out.println("-----------------------------------------------------------------");

        contents.reset();
        contexts.reset(new Context(root));
        positionStack.reset();
        elementStack.reset();

        if(debug){
            contexts.printCurrent("Contexts");
            System.out.println("-----------------------------------------------------------------");
        }
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException{
        for(Context context: contexts)
            context.matchText(contents);
        contents.reset();
    }

    @Override
    public void comment(char[] ch, int start, int length) throws SAXException{
        for(Context context: contexts)
            context.matchText(contents);
        contents.reset();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException{
        if(debug)
            System.out.println();
        
        int pos = positionStack.push(uri, localName);
        elementStack.push(uri, localName, pos);

        for(Context context: contexts){
            context.matchText(contents);
            context.startElement(uri, localName, pos);
        }
        contents.reset();
        contexts.update();

        // match attributes
        if(contexts.hasAttributeChild){
            for(Context newContext: contexts)
                newContext.matchAttributes(attrs);
        }

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

        ArrayDeque<Context> stack = new ArrayDeque<Context>();
        for(int i=contexts.current.size()-1; i>=0; i--){
            Context context = contexts.current.get(i);
            context.matchText(contents);
            stack.push(context.endElement());
        }
        while(!stack.isEmpty())
            contexts.addUnique(stack.pop());

        contents.reset();
        contexts.update();

        if(debug)
            System.out.println("-----------------------------------------------------------------");
    }

    /*-------------------------------------------------[ Contexts ]---------------------------------------------------*/

    static class Contexts implements Iterable<Context>{
        private List<Context> current = new ArrayList<Context>();
        private List<Context> next = new ArrayList<Context>();

        public void reset(Context context){
            current.clear();
            next.clear();
            current.add(context);
        }

        ResettableIterator<Context> iter = new ResettableIterator<Context>(current);
        @Override
        public Iterator<Context> iterator(){
            return iter.reset(current);
        }

        public boolean hasAttributeChild;
        public boolean nextHasAttributeChild;
        public void add(Context context){
            nextHasAttributeChild |= context.node.hasAttibuteChild && context.depth<=0;
            next.add(context);
        }

        public void addUnique(Context context){
            if(!next.contains(context))
                add(context);
        }

        public void update(){
            List<Context> temp = current;
            current = next;
            next = temp;
            next.clear();

            hasAttributeChild = nextHasAttributeChild;
            nextHasAttributeChild = false;

            if(debug)
                printCurrent("newContext");
        }

        private int mark;

        public void mark(){
            mark = next.size();
        }

        public int markSize(){
            return next.size()-mark;
        }

        /*-------------------------------------------------[ Debug ]---------------------------------------------------*/

        private void println(String message, List<Context> contexts, int from){
            System.out.println(message+" ->");
            if(contexts.size()==from)
                return;
            Iterator iter = contexts.listIterator(from);
            while(iter.hasNext())
                System.out.println("     "+iter.next());
            System.out.println();
        }

        public void printCurrent(String message){
            println(message, current, 0);
        }

        public void printNext(String message){
            println(message, next, mark);
        }
    }

    static int maxInstCount;
    static int instCount;
    class Context{
        Context parent;
        Node node;
        int depth;
//        int parentDepths[];

        private Context(){
            instCount++;
            maxInstCount = Math.max(maxInstCount, instCount);
        }

        Context(Root root){
            this();
//            parentDepths = new int[0];
            node = root;
        }

        public Context childContext(Node child){
            Context context = new Context();
            context.node = child;
            context.parent = this;

//            context.parentDepths = new int[parentDepths.length+1];
//            System.arraycopy(parentDepths, 0, context.parentDepths, 0, parentDepths.length);
//            context.parentDepths[context.parentDepths.length-1] = depth;

            return context;
        }

        public Context parentContext(){
            instCount--;
            return parent;

//            Context parent = new Context();
//            parent.node = node.parent;
//            parent.depth = parentDepths[parentDepths.length-1];
//
//            parent.parentDepths = new int[parentDepths.length-1];
//            System.arraycopy(parentDepths, 0, parent.parentDepths, 0, parent.parentDepths.length);
//            return parent;
        }

        public void matchText(StringContent contents){
            if(!contents.isEmpty() && depth<=0){
                for(Node child: node.children){
                    if(child.matchesText(contents))
                        results.hit(depth, child, contents);
                    checkConstraints(child, contents);
                }
            }
        }

        private void checkConstraints(Node child, String uri, String name, int pos){
            for(Node constraint: child.constraints){
                if(constraint.matchesElement(uri, name, pos)){
                    if(results.hit(depth, constraint, elementStack)){
                        contexts.add(childContext(constraint));
                        checkConstraints(constraint, uri, name, pos);
                    }
                }
            }
        }

        private void checkConstraints(Node child, StringContent contents){
            for(Node constraint: child.constraints){
                if(constraint.matchesText(contents)){
                    results.hit(depth, constraint, contents);
                    checkConstraints(constraint, contents);
                }
            }
        }

        public void startElement(String uri, String name, int pos){
            String message = toString();

            contexts.mark();

            if(depth>0){
                depth++;
                contexts.add(this);
            }else{
                for(Node child: node.children){
                    if(child.matchesElement(uri, name, pos)){
                        if(results.hit(depth, child, elementStack)){
                            contexts.add(childContext(child));
                            checkConstraints(child, uri, name, pos);
                        }
                    }
                }
                if(node.consumable()){
                    if(results.hit(depth, node, elementStack)){
                        depth--;
                        contexts.add(this);
                        checkConstraints(node, uri, name, pos);
                    }
                }else{
                    if(contexts.markSize()==0){
                        depth++;
                        contexts.add(this);
                    }
                }
            }

            if(debug)
                contexts.printNext(message);
        }

        public void matchAttributes(Attributes attrs){
            if(depth<=0){
                for(int i=0; i<attrs.getLength(); i++){
                    String uri = attrs.getURI(i);
                    String name = attrs.getLocalName(i);
                    String value = attrs.getValue(i);

                    for(Node child: node.children){
                        if(child.matchesAttribute(uri, name, value)){
                            results.hit(depth, child, value);
                            for(Node constraint: child.constraints){
                                if(constraint.matchesAttribute(uri, name, value))
                                    results.hit(depth, constraint, value);
                            }
                        }
                    }
                }
            }
        }

        public Context endElement(){
            if(depth==0){
                results.clearHitCounts(depth, node);
                results.clearPredicateCache(depth, node);
                return parentContext();
            }else{
                if(depth>0){
//                    results.clearHitCounts(depth, node);
                    depth--;
                }else{
                    results.clearHitCounts(depth, node);
                    depth++;
                }

                return this;
            }
        }

        @Override
        public int hashCode(){
            return depth + node.hashCode();
        }

        @Override
        public boolean equals(Object obj){
            if(obj instanceof Context){
                Context that = (Context)obj;
                return this.depth==that.depth && this.node==that.node;
            }else
                return false;
        }

        @Override
        public String toString(){
            return "["+depth+"] "+node;
        }
    }

    /*-------------------------------------------------[ Sniffing ]---------------------------------------------------*/

    public XPathResults sniff(InputSource source, int minHits) throws ParserConfigurationException, SAXException, IOException{
        try{
            results = new XPathResults(minHits);
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
            System.out.println("max contexts: "+maxInstCount);
        return results;
    }
}
