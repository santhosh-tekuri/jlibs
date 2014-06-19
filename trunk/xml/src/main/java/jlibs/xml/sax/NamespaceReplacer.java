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

package jlibs.xml.sax;

import jlibs.xml.Namespaces;
import jlibs.xml.sax.helpers.MyNamespaceSupport;
import jlibs.xml.xsl.TransformerUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.Attributes2;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * SAXDelegate that can replace namespaces specified in map
 *
 * @author Santhosh Kumar T
 */
public class NamespaceReplacer extends SAXDelegate{
    protected Map<String, String> old2new = new HashMap<String, String>();
    protected Map<String, String> new2old = new HashMap<String, String>();

    private static Properties SUGGESTED = new Properties();
    protected final MyNamespaceSupport oldNSSupport;
    protected final MyNamespaceSupport newNSSupport;

    public NamespaceReplacer(Map<String, String> old2new){
        this.old2new = old2new;
        for(Map.Entry<String, String> entry: old2new.entrySet())
            new2old.put(entry.getValue(), entry.getKey());
        if(old2new.containsKey("") || new2old.containsKey("")){
            oldNSSupport = new MyNamespaceSupport(SUGGESTED);
            newNSSupport = new MyNamespaceSupport(SUGGESTED);
        }else
            oldNSSupport = newNSSupport = null;
    }

    protected String translate(String namespace, Map<String, String> map){
        String value = map.get(namespace);
        return value==null ? namespace : value;
    }

    protected String translateAttribute(String namespace, Map<String, String> map){
        return namespace.isEmpty() ? namespace : translate(namespace, map);
    }
    
    private AttributeReplacer attributeReplacer = new AttributeReplacer();
    private Attribute2Replacer attribute2Replacer = new Attribute2Replacer();
    private Attributes replace(Attributes attrs){
        if(attrs instanceof Attributes2){
            attribute2Replacer.setDelegate((Attributes2)attrs);
            return attribute2Replacer;
        }else{
            attributeReplacer.setDelegate(attrs);
            return attributeReplacer;
        }
    }

    @Override
    public void startDocument() throws SAXException{
        if(oldNSSupport!=null){
            oldNSSupport.startDocument();
            newNSSupport.startDocument();
        }
        super.startDocument();
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException{
        if(oldNSSupport!=null)
            oldNSSupport.startPrefixMapping(prefix, uri);
        uri = translate(uri, old2new);
        if(newNSSupport!=null){
            newNSSupport.setSuggestPrefix(prefix.isEmpty() ? "ns" : prefix);
            prefix = newNSSupport.declarePrefix(uri);
        }
        super.startPrefixMapping(prefix, uri);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException{
        if(oldNSSupport!=null){
            oldNSSupport.startElement();
            newNSSupport.startElement();
        }
        uri = translate(uri, old2new);
        if(newNSSupport!=null)
            qName = newNSSupport.toQName(uri, localName);
        super.startElement(uri, localName, qName, replace(atts));
    }

    protected String translateAttribute(String qName, MyNamespaceSupport from, MyNamespaceSupport to){
        if(from==null)
            return qName;
        int colon = qName.indexOf(':');
        if(colon==-1)
            return qName;
        String prefix = qName.substring(0, colon);
        String localName = qName.substring(colon+1);
        String uri = from.findURI(prefix);
        return to.toQName(uri, localName);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException{
        uri = translate(uri, old2new);
        if(newNSSupport!=null)
            qName = newNSSupport.toQName(uri, localName);
        super.endElement(uri, localName, qName);
        if(oldNSSupport!=null){
            oldNSSupport.endElement();
            assert newNSSupport!=null;
            newNSSupport.endElement();
        }
    }

    private class AttributeReplacer implements Attributes{
        protected Attributes delegate;
        public void setDelegate(Attributes delegate){
            this.delegate = delegate;
        }

        @Override
        public int getLength(){
            return delegate.getLength();
        }

        @Override
        public String getURI(int index){
            return translateAttribute(delegate.getURI(index), old2new);
        }

        @Override
        public String getLocalName(int index){
            return delegate.getLocalName(index);
        }

        @Override
        public String getQName(int index){
            return translateAttribute(delegate.getQName(index), oldNSSupport, newNSSupport);
        }

        @Override
        public String getType(int index){
            return delegate.getType(index);
        }

        @Override
        public String getValue(int index){
            return delegate.getValue(index);
        }

        @Override
        public int getIndex(String uri, String localName){
            return delegate.getIndex(translateAttribute(uri, new2old), localName);
        }

        @Override
        public int getIndex(String qName){
            return delegate.getIndex(translateAttribute(qName, newNSSupport, oldNSSupport));
        }

        @Override
        public String getType(String uri, String localName){
            return delegate.getType(translateAttribute(uri, new2old), localName);
        }

        @Override
        public String getType(String qName){
            return delegate.getType(qName);
        }

        @Override
        public String getValue(String uri, String localName){
            return delegate.getValue(translateAttribute(uri, new2old), localName);
        }

        @Override
        public String getValue(String qName){
            return delegate.getValue(translateAttribute(qName, newNSSupport, oldNSSupport));
        }
    }

    private class Attribute2Replacer extends AttributeReplacer implements Attributes2{
        protected Attributes2 delegate;
        public void setDelegate(Attributes2 delegate){
            super.setDelegate(delegate);
            this.delegate = delegate;
        }

        @Override
        public boolean isDeclared(int index){
            return delegate.isDeclared(index);
        }

        @Override
        public boolean isDeclared(String qName){
            return delegate.isDeclared(qName);
        }

        @Override
        public boolean isDeclared(String uri, String localName){
            return delegate.isDeclared(translateAttribute(uri, new2old), localName);
        }

        @Override
        public boolean isSpecified(int index){
            return delegate.isSpecified(index);
        }

        @Override
        public boolean isSpecified(String uri, String localName){
            return delegate.isSpecified(translateAttribute(uri, new2old), localName);
        }

        @Override
        public boolean isSpecified(String qName){
            return delegate.isSpecified(translateAttribute(qName, newNSSupport, oldNSSupport));
        }
    }

    public static void main(String[] args) throws Exception{
        String inFile = "/Users/santhosh/Desktop/old.xml";

        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put(Namespaces.URI_SOAPENV, Namespaces.URI_SOAP12ENV);
        NamespaceReplacer replacer = new NamespaceReplacer(namespaces);

        XMLReader reader = SAXUtil.newSAXParser(true, false, false).getXMLReader();
        SAXUtil.setHandler(reader, replacer);

        TransformerHandler handler = TransformerUtil.newTransformerHandler(null, true, 0, null);
        replacer.setHandler(handler);
        StringWriter writer = new StringWriter();
        handler.setResult(new StreamResult(writer));

        reader.parse(new InputSource(inFile));

        String xml = writer.toString();
        System.out.println(xml);
        SAXUtil.newSAXParser(true, false, false).parse(new InputSource(new StringReader(xml)), new DefaultHandler());
    }
}
