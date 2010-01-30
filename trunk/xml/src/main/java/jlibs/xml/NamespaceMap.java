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

package jlibs.xml;

import jlibs.core.util.ContextMap;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * NamespaceContext implementation using ContextMap
 * 
 * @author Santhosh Kumar T
 */
public class NamespaceMap extends ContextMap<String, String> implements NamespaceContext{
    public NamespaceMap(){
        this(null);
    }

    public NamespaceMap(ContextMap<String, String> parent){
        super(parent);
        if(parent==null){
            put(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
            put(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
        }
    }

    @Override
    public NamespaceMap parent(){
        return (NamespaceMap)super.parent();
    }

    /*-------------------------------------------------[ NamespaceContext ]---------------------------------------------------*/

    @Override
    public String getNamespaceURI(String prefix){
        if(prefix==null)
            throw new IllegalArgumentException("prefix is null");

        return get(prefix);
    }

    @Override
    public String getPrefix(String namespaceURI){
        if(namespaceURI==null)
            throw new IllegalArgumentException("namespaceURI is null");
        
        Iterator<String> keys = keys();
        while(keys.hasNext()){
            String prefix = keys.next();
            String uri = getNamespaceURI(prefix);
            if(namespaceURI.equals(uri))
                return prefix;
        }
        return null;
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI){
        if(namespaceURI==null)
            throw new IllegalArgumentException("namespaceURI is null");

        List<String> prefixes = new ArrayList<String>();
        Iterator<String> keys = keys();
        while(keys.hasNext()){
            String prefix = keys.next();
            String uri = getNamespaceURI(prefix);
            if(namespaceURI.equals(uri))
                prefixes.add(prefix);
        }
        return prefixes.iterator();
    }

    /*-------------------------------------------------[ SAX Population ]---------------------------------------------------*/
    
    public static class Handler{
        public NamespaceMap namespaceMap;

        private boolean needNewContext;

        public void startDocument(){
            namespaceMap = new NamespaceMap();
            needNewContext = true;
        }

        public void startPrefixMapping(String prefix, String uri){
            if(needNewContext){
                namespaceMap = new NamespaceMap(namespaceMap);
                needNewContext = false;
            }
            namespaceMap.put(prefix, uri);
        }

        public void startElement(){
            if(needNewContext)
                namespaceMap = new NamespaceMap(namespaceMap);
            needNewContext = true;
        }

        public void endElement(){
            namespaceMap = namespaceMap.parent();
        }
    }
}
