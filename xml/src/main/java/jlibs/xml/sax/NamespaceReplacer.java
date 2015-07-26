/**
 * Copyright 2015 Santhosh Kumar Tekuri
 *
 * The JLibs authors license this file to you under the Apache License,
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

package jlibs.xml.sax;

import jlibs.xml.sax.helpers.MyNamespaceSupport;
import org.xml.sax.*;
import org.xml.sax.ext.Attributes2;
import org.xml.sax.helpers.XMLFilterImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * SAXDelegate that can replace namespaces specified in map
 *
 * @author Santhosh Kumar T
 */
public class NamespaceReplacer extends XMLFilterImpl{
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

    public NamespaceReplacer(XMLReader xmlReader, Map<String, String> old2new){
        this(old2new);
        setParent(xmlReader);
    }

    @Override
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException{
        if(SAXFeatures.NAMESPACE_PREFIXES.equals(name) && value)
            throw new SAXNotSupportedException("Feature '"+SAXFeatures.NAMESPACE_PREFIXES+"' is not supported");
        if(SAXFeatures.NAMESPACES.equals(name) && !value)
            throw new SAXNotSupportedException("Feature '"+SAXFeatures.NAMESPACES+"' is required");
        super.setFeature(name, value);
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

        String uri = translate("", old2new);
        if(uri!=null)
            startPrefixMapping("", "");
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
}
