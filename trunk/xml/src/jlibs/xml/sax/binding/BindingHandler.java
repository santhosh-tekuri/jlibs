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

package jlibs.xml.sax.binding;

import jlibs.xml.NamespaceMap;
import jlibs.xml.sax.SAXUtil;
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
public class BindingHandler extends DefaultHandler{
    public final BindingRegistry docRegistry;
    private final boolean ignoreUnresolved;

    public BindingHandler(BindingRegistry docRegistry){
        this(docRegistry, true);
    }
    
    public BindingHandler(BindingRegistry docRegistry, boolean ignoreUnresolved){
        this.docRegistry = docRegistry;
        this.ignoreUnresolved = ignoreUnresolved;
    }

    public Object parse(InputSource is) throws ParserConfigurationException, SAXException, IOException{
        SAXUtil.newSAXParser(true, false, false).parse(is, this);
        return reset();
    }

    private BindingContext context;
    private Object rootObject;
    private CharArrayWriter content = new CharArrayWriter();
    private NamespaceMap.Handler nsHandler = new NamespaceMap.Handler();

    private Object reset(){
        Object obj = rootObject;
        context = null;
        rootObject = null;
        content.reset();
        return obj;
    }

    private Locator locator;
    @Override
    public void setDocumentLocator(Locator locator){
        this.locator = locator;
    }

    @Override
    public void startDocument() throws SAXException{
        nsHandler.startDocument();
        reset();
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException{
        nsHandler.startPrefixMapping(prefix, uri);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
        nsHandler.startElement();
        if(context!=null && content.size()>0)
            context.onText();
        QName element = new QName(uri, localName);
        context = new BindingContext(element, context, attributes);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException{
        content.write(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException{
        nsHandler.endElement();
        if(content.size()>0)
            context.onText();
        context = context.pop();
    }

    public Object getObject(){
        return rootObject;
    }

    @SuppressWarnings({"unchecked"})
    private class BindingContext extends SAXContext{
        private BindingRelation bindingRelation;
        private BindingContext parent;

        public BindingContext(QName element, BindingContext parent, Attributes attributes) throws SAXException{
            super(nsHandler.namespaceMap, element);
            this.parent = parent;
            bindingRelation = (parent!=null?parent.bindingRelation.binding.registry:docRegistry).get(element);
            if(bindingRelation==null){
                if(ignoreUnresolved)
                    bindingRelation = BindingRelation.DO_NOTHING;
                else
                    throw new SAXException(String.format("can't find binding for <%s>(line:%d col:%d)", element, locator.getLineNumber(), locator.getColumnNumber()));
            }
            if(parent!=null)
                object = parent.object;
            bindingRelation.binding.start(this, attributes);
            if(parent!=null)
                bindingRelation.relation.started(parent, this);
        }

        public void onText() throws SAXException{
            bindingRelation.binding.text(this, content.toString());
            content.reset();
        }

        public BindingContext pop() throws SAXException{
            bindingRelation.binding.finish(this);
            if(parent!=null)
                bindingRelation.relation.finished(parent, this);
            if(parent==null)
                rootObject = object;
            return parent;
        }
    }
}
