/*
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

package jlibs.xml.sax.binding.impl;

import jlibs.core.lang.StringUtil;
import jlibs.xml.NamespaceMap;
import jlibs.xml.sax.SAXUtil;
import jlibs.xml.sax.binding.SAXContext;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import java.io.CharArrayWriter;
import java.io.IOException;

/**
 * @author Santhosh Kumar T
 */
public class Handler extends DefaultHandler{
    public final Registry docRegistry;

    public Handler(Registry docRegistry){
        this.docRegistry = docRegistry;
    }

    /*-------------------------------------------------[ ignoreUnresolved ]---------------------------------------------------*/

    private boolean ignoreUnresolved = true;

    public boolean isIgnoreUnresolved(){
        return ignoreUnresolved;
    }

    public void setIgnoreUnresolved(boolean ignoreUnresolved){
        this.ignoreUnresolved = ignoreUnresolved;
    }

    /*-------------------------------------------------[ populateNamespaces ]---------------------------------------------------*/

    private boolean populateNamespaces = false;

    public boolean isPopulateNamespaces(){
        return populateNamespaces;
    }

    public void setPopulateNamespaces(boolean populateNamespaces){
        this.populateNamespaces = populateNamespaces;
    }

    /*-------------------------------------------------[ Parsing ]---------------------------------------------------*/
    
    public Object parse(InputSource is) throws ParserConfigurationException, SAXException, IOException{
        SAXUtil.newSAXParser(true, false, false).parse(is, this);
        return reset();
    }

    private Object rootObject;
    private CharArrayWriter content = new CharArrayWriter();
    private NamespaceMap.Handler nsHandler = new NamespaceMap.Handler();

    private Object reset(){
        Object obj = rootObject;
        context = null;
        rootObject = null;
        content.reset();
        nsHandler.namespaceMap = null;
        return obj;
    }

    private Locator locator;
    @Override
    public void setDocumentLocator(Locator locator){
        this.locator = locator;
    }

    @Override
    public void startDocument() throws SAXException{
        if(populateNamespaces)
            nsHandler.startDocument();
        reset();
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException{
        if(populateNamespaces)
            nsHandler.startPrefixMapping(prefix, uri);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
        if(populateNamespaces)
            nsHandler.startElement();
        if(context!=null && content.size()>0)
            context.onText();
        QName element = new QName(uri, localName);
        context = newContext(element, attributes);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException{
        content.write(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException{
        if(populateNamespaces)
            nsHandler.endElement();
        if(content.size()>0)
            context.onText();
        context = context.pop();
    }

    public Object getObject(){
        return rootObject;
    }


    /*-------------------------------------------------[ Context ]---------------------------------------------------*/
    
    private BindingContext context;
    private BindingContext cache;

    public BindingContext newContext(QName element, Attributes attributes) throws SAXException{
        if(cache==null)
            return new BindingContext(element, context, attributes);
        else{
            BindingContext reusingContext = cache;
            cache = cache.parent;
            reusingContext.init(element, context, attributes);
            return reusingContext;
        }
    }

    @SuppressWarnings({"unchecked"})
    private class BindingContext extends SAXContext{
        private BindingRelation bindingRelation;
        private BindingContext parent;

        public BindingContext(QName element, BindingContext parent, Attributes attributes) throws SAXException{
            init(element, parent, attributes);
        }
        
        private void init(QName element, BindingContext parent, Attributes attributes) throws SAXException{
            namespaceMap = nsHandler.namespaceMap;
            this.element = element;
            this.parent = parent;
            bindingRelation = (parent!=null?parent.bindingRelation.binding.registry:docRegistry).get(element);
            if(bindingRelation==null){
                if(ignoreUnresolved)
                    bindingRelation = BindingRelation.DO_NOTHING;
                else
                    throw new SAXException(String.format("can't find binding for %s", this));
            }
            if(parent!=null)
                object = parent.object;
            bindingRelation.binding.startElement(bindingRelation.bindingState, this, attributes);
            if(parent!=null)
                bindingRelation.relation.startRelation(bindingRelation.relationState, parent, this);
        }

        public void onText() throws SAXException{
            bindingRelation.binding.text(bindingRelation.bindingState, this, content.toString());
            content.reset();
        }

        public BindingContext pop() throws SAXException{
            bindingRelation.binding.endElement(bindingRelation.bindingState, this);
            if(parent!=null)
                bindingRelation.relation.endRelation(bindingRelation.relationState, parent, this);
            if(parent==null)
                rootObject = object;

            // reset and add to cache
            BindingContext _parent = parent;
            namespaceMap = null;
            element = null;
            object = null;
            if(temp!=null)
                temp.clear();
            bindingRelation = null;
            parent = cache;
            cache = this;

            return _parent;
        }

        @Override
        public String xpath(){
            StringBuilder buff = new StringBuilder("/");
            BindingContext context = this;
            while(context!=null){
                if(buff.length()>1)
                    buff.insert(1, '/');

                String qname;
                if(namespaceMap==null)
                    qname = context.element.toString();
                else{
                    String prefix = namespaceMap.getPrefix(context.element.getNamespaceURI());
                    qname = StringUtil.isEmpty(prefix) ? context.element.getLocalPart() : (prefix+":"+context.element.getLocalPart());
                }
                buff.insert(1, qname);
                context = context.parent;
            }
            return buff.toString();
        }

        @Override
        public int line(){
            return locator.getLineNumber();
        }

        @Override
        public int column(){
            return locator.getColumnNumber();
        }
    }
}
