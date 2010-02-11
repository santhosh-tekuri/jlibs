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

package jlibs.xml.sax.binding.impl;

import jlibs.core.lang.StringUtil;
import jlibs.xml.NamespaceMap;
import jlibs.xml.QNameFake;
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
        if(context!=null)
            context.onText();
        context = newContext(uri, localName, attributes);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException{
        content.write(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException{
        if(populateNamespaces)
            nsHandler.endElement();
        context.onText();
        context = context.pop();
    }

    public Object getObject(){
        return rootObject;
    }

    protected void onUnresolvedElement(SAXContext context) throws SAXException{
        // do nothing
    }
    
    /*-------------------------------------------------[ Context ]---------------------------------------------------*/
    
    private QNameFake qnameFake = new QNameFake();
    private BindingContext context;
    private BindingContext cache;

    public BindingContext newContext(String namespaceURI, String localPart, Attributes attributes) throws SAXException{
        if(cache==null)
            return new BindingContext(namespaceURI, localPart, context, attributes);
        else{
            BindingContext reusingContext = cache;
            cache = cache.parent;
            reusingContext.init(namespaceURI, localPart, context, attributes);
            return reusingContext;
        }
    }

    @SuppressWarnings({"unchecked"})
    private class BindingContext extends SAXContext{
        private BindingRelation bindingRelation;
        private BindingContext parent;

        public BindingContext(String namespaceURI, String localPart, BindingContext parent, Attributes attributes) throws SAXException{
            init(namespaceURI, localPart, parent, attributes);
        }
        
        private void init(String namespaceURI, String localPart, BindingContext parent, Attributes attributes) throws SAXException{
            this.qnameFake = Handler.this.qnameFake;
            namespaceMap = nsHandler.namespaceMap;
            this.parent = parent;
            bindingRelation = (parent!=null?parent.bindingRelation.binding.registry:docRegistry).get(qnameFake.set(namespaceURI, localPart));
            boolean unresolvedElement = bindingRelation==null;
            if(unresolvedElement)
                bindingRelation = BindingRelation.DO_NOTHING;
            this.element = bindingRelation.qname;
            if(element.getNamespaceURI().equals("*") || element.getLocalPart().equals("*"))
                this.element = new QName(namespaceURI, localPart);
            if(parent!=null)
                object = parent.object;
            if(unresolvedElement)
                onUnresolvedElement(this);
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
            qnameFake = null;
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

        public Locator locator(){
            return locator;
        }
    }
}
