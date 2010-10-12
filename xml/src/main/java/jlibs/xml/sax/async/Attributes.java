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

package jlibs.xml.sax.async;

import jlibs.xml.ClarkName;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.util.HashSet;
import java.util.Set;

import static javax.xml.XMLConstants.*;

/**
 * @author Santhosh Kumar T
 */
class Attributes{
    private AttributesImpl attrs = new AttributesImpl();
    private Namespaces namespaces;
    private DTD dtd;

    public Attributes(Namespaces namespaces, DTD dtd){
        this.namespaces = namespaces;
        this.dtd = dtd;
    }

    public void reset(){
        attrs.clear();
    }

    public String addAttribute(String elemName, QName attrQName, StringBuilder value) throws SAXException{
        AttributeType type = dtd==null ? AttributeType.CDATA : dtd.attributeType(elemName, attrQName.name);
        String attrValue = type.normalize(value.toString());

        if(attrQName.name.equals("xmlns")){
            namespaces.add("", attrValue);
        }else if(attrQName.prefix.equals("xmlns")){
            if(attrQName.localName.equals(XML_NS_PREFIX)){
                if(!attrValue.equals(XML_NS_URI)){
                    return "prefix "+ XML_NS_PREFIX+" must refer to "+ XML_NS_URI;
                }
            }else if(attrQName.localName.equals(XMLNS_ATTRIBUTE)){
                return "prefix "+ XMLNS_ATTRIBUTE+" must not be declared";
            }else{
                if(attrValue.equals(XML_NS_URI)){
                    return XML_NS_URI+" must be bound to "+ XML_NS_PREFIX;
                }else if(attrValue.equals(XMLNS_ATTRIBUTE_NS_URI)){
                    return XMLNS_ATTRIBUTE_NS_URI+" must be bound to "+ XMLNS_ATTRIBUTE;
                }else{
                    if(attrValue.length()==0)
                        return "No Prefix Undeclaring: "+attrQName.localName;
                    namespaces.add(attrQName.localName, attrValue);
                }
            }
        }else
            attrs.addAttribute(attrQName.prefix, attrQName.localName, attrQName.name, type.name(), attrValue);

        return null;
    }

    private Set<String> attributeNames = new HashSet<String>();
    public String fixAttributes(String elemName) throws SAXException{
        attributeNames.clear();
        int attrCount = attrs.getLength();
        for(int i=0; i<attrCount; i++){
            String prefix = attrs.getURI(i);
            String uri = "";
            if(prefix.length()>0){
                uri = namespaces.getNamespaceURI(prefix);
                if(uri==null)
                    return "Unbound prefix: "+prefix;
                attrs.setURI(i, uri);
            }

            String clarkName = ClarkName.valueOf(uri, attrs.getLocalName(i));
            if(!attributeNames.add(clarkName))
                return "Attribute \""+clarkName+"\" was already specified for element \""+elemName+"\"";
        }

        if(dtd!=null)
            dtd.addMissingAttributes(elemName, attrs);

        return null;
    }

    public AttributesImpl get(){
        return attrs;
    }
}
