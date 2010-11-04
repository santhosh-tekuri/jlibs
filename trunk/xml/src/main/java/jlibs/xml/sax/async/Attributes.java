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

import static javax.xml.XMLConstants.*;

/**
 * @author Santhosh Kumar T
 */
class Attributes{
    private AttributesImpl attrs = new AttributesImpl();
    private Namespaces namespaces;
    DTD dtd;

    public Attributes(Namespaces namespaces){
        this.namespaces = namespaces;
    }

    public void reset(){
        attrs.clear();
    }

    public String addAttribute(String elemName, QName attrQName, StringBuilder value) throws SAXException{
        String attrName = attrQName.name;
        AttributeType type = dtd==null ? AttributeType.CDATA : dtd.attributeType(elemName, attrName);
        String attrValue = type.normalize(value.toString());

        String attrLocalName = attrQName.localName;
        if(attrName.startsWith("xmlns")){
            if(attrName.length()==5){
                namespaces.add("", attrValue);
            }else if(attrName.charAt(5)==':'){
                if(attrLocalName.equals(XML_NS_PREFIX)){
                    if(!attrValue.equals(XML_NS_URI))
                        return "prefix "+ XML_NS_PREFIX+" must refer to "+ XML_NS_URI;
                }else if(attrLocalName.equals(XMLNS_ATTRIBUTE))
                    return "prefix "+ XMLNS_ATTRIBUTE+" must not be declared";
                else{
                    if(attrValue.equals(XML_NS_URI))
                        return XML_NS_URI+" must be bound to "+ XML_NS_PREFIX;
                    else if(attrValue.equals(XMLNS_ATTRIBUTE_NS_URI))
                        return XMLNS_ATTRIBUTE_NS_URI+" must be bound to "+ XMLNS_ATTRIBUTE;
                    else{
                        if(attrValue.length()==0)
                            return "No Prefix Undeclaring: "+attrLocalName;
                        namespaces.add(attrLocalName, attrValue);
                    }
                }
            }
        }else
            attrs.addAttribute(attrQName.prefix, attrLocalName, attrName, type.name(), attrValue);

        return null;
    }

    public String fixAttributes(String elemName) throws SAXException{
        int attrCount = attrs.getLength();
        if(attrCount>0){
            for(int i=0; i<attrCount; i++){
                String prefix = attrs.getURI(i);
                if(prefix.length()>0){
                    String uri = namespaces.getNamespaceURI(prefix);
                    if(uri==null)
                        return "Unbound prefix: "+prefix;
                    attrs.setURI(i, uri);
                }
            }
            if(attrCount>1){
                for(int i=1; i<attrCount; i++){
                    if(attrs.getIndex(attrs.getURI(i), attrs.getLocalName(i))<i)
                        return "Attribute \""+ClarkName.valueOf(attrs.getURI(i), attrs.getLocalName(i))+"\" was already specified for element \""+elemName+"\"";
                }
            }
        }
        if(dtd!=null)
            dtd.addMissingAttributes(elemName, attrs);

        return null;
    }

    public AttributesImpl get(){
        return attrs;
    }
}
