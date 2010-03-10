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

package jlibs.xml;

import jlibs.core.net.URLUtil;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import java.util.*;

/**
 * This is an implementation of {@link NamespaceContext}.
 * <p>
 * Example Usage:
 * <pre class="prettyprint">
 * import jlibs.core.lang.StringUtil;
 * import jlibs.xml.DefaultNamespaceContext;
 * import jlibs.xml.Namespaces;
 *
 * DefaultNamespaceContext nsContext = new DefaultNamespaceContext();
 * nsContext.declarePrefix("xsd", Namespaces.URI_XSD)
 * nsContext.declarePrefix("jlibs", "http://code.google.com/p/jlibs");
 * nsContext.declarePrefix("tns", "http://code.google.com/p/jlibs");
 *
 * System.out.println(nsContext.getPrefix("http://code.google.com/p/jlibs")); // prints "jlibs"
 * System.out.println(nsContext.getNamespaceURI("jlibs"); // prints "http://code.google.com/p/jlibs"
 * System.out.println(StringUtil.{@link jlibs.core.lang.StringUtil#join(java.util.Iterator) join}(nsContext.getPrefixes("http://code.google.com/p/jlibs"))); // prints "jlibs, tns"
 * </pre>
 *
 * <b>Prefix Suggestions:</b>
 * <p>
 * You can speficy the suggested {@code prefix} for a {@code uri} by passing {@link java.util.Properties} to the {@link #DefaultNamespaceContext(java.util.Properties) constructor}<br>
 * If {@link #DefaultNamespaceContext() no-arg constructor} is used, then it uses {@link Namespaces#getSuggested()} as suggestions.
 * <p>
 * Thus you can declare a {@code uri} without specified {@code prefix} manually. It finds the {@code prefix} automatically and returns the {@code prefix} it used.
 * <pre class="prettyprint">
 * System.out.println(nsContext.{@link #declarePrefix(String) declarePrefix}("http://www.google.com")); // returns "google"
 * </pre> 
 *
 * This class also provides a handy method to compute {@code javax.xml.namespace.QName}:
 * <pre class="prettyprint">
 * javax.xml.namespace.QName qname = nsContext.{@link #toQName(String) toQName}("tns:myElement");
 * System.out.println(qname); // prints "{http://code.google.com/p/jlibs}myElement"
 * </pre>
 * @author Santhosh Kumar T
 */
public class DefaultNamespaceContext implements NamespaceContext{
    private Properties suggested;
    private Map<String, String> prefix2uriMap = new HashMap<String, String>();
    private Map<String, String> uri2prefixMap = new HashMap<String, String>();
    private String defaultURI = XMLConstants.NULL_NS_URI;

    /**
     * Creates new instance of DefaultNamespaceContext with suggested prefixes
     * {@link Namespaces#getSuggested()}
     *
     * @see #declarePrefix(String)
     */
    public DefaultNamespaceContext(){
        this(Namespaces.getSuggested());
    }

    /**
     * Creates new instance of DefaultNamespaceContext with specified suggested prefixes
     *
     * @param suggested suggested prefixes, where key is {@code URI} and value is suggested {@code prefix}
     *
     * @see #declarePrefix(String)
     */
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

    /**
     * Binds specified {@code prefix} to the given {@code uri}
     *
     * @param prefix the prefix to be bound
     * @param uri the uri to which specified {@code prefix} to be bound
     */
    public void declarePrefix(String prefix, String uri){
        if(prefix.length()==0)
            defaultURI = uri;
        prefix2uriMap.put(prefix, uri);
        uri2prefixMap.put(uri, prefix);
    }

    /**
     * Declared the specified {@code uri} in this namespaceContext and returns
     * the prefix to which it is bound.
     * <p>
     * the prefix is guessed from the suggested namespaces specified at construction
     * or derived from the specified {@code uri}
     *
     * @param uri uri to be declared
     *
     * @return the prefix to which {@code uri} is bound
     *
     * @see jlibs.core.net.URLUtil#suggestPrefix(java.util.Properties, String) 
     */
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

    /**
     * Constructs {@link javax.xml.namespace.QName} for the specified {@code qname}.
     * 
     * @param qname the qualified name
     * @return {@link javax.xml.namespace.QName} object constructed.
     *
     * @throws IllegalArgumentException if the prefix in {@code qname} is undeclared.
     */
    public QName toQName(String qname){
        String prefix = "";
        String localName = qname;

        int colon = qname.indexOf(':');
        if(colon!=-1){
            prefix = qname.substring(0, colon);
            localName = qname.substring(colon+1);
        }
        String ns = getNamespaceURI(prefix);
        if(ns==null)
            throw new IllegalArgumentException("undeclared prefix: "+prefix);

        return new QName(ns, localName, prefix);
    }
}
