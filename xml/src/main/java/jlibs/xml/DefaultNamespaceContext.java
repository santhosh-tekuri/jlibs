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

package jlibs.xml;

import jlibs.core.net.URLUtil;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import java.util.*;

/**
 * @author Santhosh Kumar T
 */
public class DefaultNamespaceContext implements NamespaceContext{
    private Properties suggested;
    private Map<String, String> prefix2uriMap = new HashMap<String, String>();
    private Map<String, String> uri2prefixMap = new HashMap<String, String>();
    private String defaultURI = XMLConstants.NULL_NS_URI;

    public DefaultNamespaceContext(){
        this(Namespaces.getSuggested());
    }

    public DefaultNamespaceContext(Properties suggested){
        this.suggested = suggested;
        declarePrefix(XMLConstants.DEFAULT_NS_PREFIX, XMLConstants.NULL_NS_URI);
        declarePrefix(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
        declarePrefix(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
    }

    @Override
    public String getNamespaceURI(String prefix){
        if(prefix.length()==0)
            return defaultURI;
        return prefix2uriMap.get(prefix);
    }

    @Override
    public String getPrefix(String namespaceURI){
        if(defaultURI.equals(namespaceURI))
            return XMLConstants.DEFAULT_NS_PREFIX;
        return uri2prefixMap.get(namespaceURI);
    }

    @Override
    public Iterator getPrefixes(String namespaceURI){
        List<String> list = new ArrayList<String>(prefix2uriMap.size());
        for(Map.Entry<String, String> entry: prefix2uriMap.entrySet()){
            if(entry.getValue().equals(namespaceURI))
                list.add(entry.getKey());
        }
        return list.iterator();
    }

    public void declarePrefix(String prefix, String uri){
        if(prefix.length()==0)
            defaultURI = uri;
        prefix2uriMap.put(prefix, uri);
        uri2prefixMap.put(uri, prefix);
    }

    public String declarePrefix(String uri){
        String prefix = getPrefix(uri);
        if(prefix==null){
            prefix = URLUtil.suggestPrefix(suggested, uri);
            if(getNamespaceURI(prefix)!=null){
                int i = 1;
                String _prefix;
                while(true){
                    _prefix = prefix + i;
                    if(getNamespaceURI(_prefix)==null){
                        prefix = _prefix;
                        break;
                    }
                    i++;
                }
            }
            declarePrefix(prefix, uri);
        }
        return prefix;
    }

    public QName toQName(String qname){
        String prefix = "";
        String localName = qname;

        int colon = qname.indexOf(':');
        if(colon!=-1){
            prefix = qname.substring(0, colon);
            localName = qname.substring(colon+1);
        }

        return new QName(getNamespaceURI(prefix), localName, prefix);
    }
}
