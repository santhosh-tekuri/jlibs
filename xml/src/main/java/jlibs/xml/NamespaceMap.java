/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
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
            put(XMLConstants.DEFAULT_NS_PREFIX, XMLConstants.NULL_NS_URI);
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
