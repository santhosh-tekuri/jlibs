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

import org.xml.sax.InputSource;
import org.xml.sax.helpers.AttributesImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Santhosh Kumar T
 */
public class DTD{
    public String root;
    public Map<String, Map<String, DTDAttribute>> attributes = new HashMap<String, Map<String, DTDAttribute>>();
    private Namespaces namespaces;
    public Set<String> nonMixedElements = new HashSet<String>();
    public InputSource externalDTD;

    public DTD(Namespaces namespaces){
        this.namespaces = namespaces;
    }

    public void reset(){
        root = null;
        attributes.clear();
        nonMixedElements.clear();
        externalDTD = null;
    }

    public AttributeType attributeType(String element, String attribute){
        AttributeType type = AttributeType.CDATA;
        Map<String, DTDAttribute> attrList = attributes.get(element);
        if(attrList!=null){
            DTDAttribute dtdAttr = attrList.get(attribute);
            if(dtdAttr!=null)
                type = dtdAttr.type==AttributeType.ENUMERATION ? AttributeType.NMTOKEN : dtdAttr.type;
        }
        return type;
    }

    public void addMissingAttributes(String element, AttributesImpl attributes){
        Map<String, DTDAttribute> attList = this.attributes.get(element);
        if(attList==null)
            return;
        for(DTDAttribute dtdAttr: attList.values()){
            switch(dtdAttr.valueType){
                case DEFAULT:
                case FIXED:
                    if(attributes.getIndex(dtdAttr.name)==-1 && !dtdAttr.isNamespace()){
                        AttributeType type = dtdAttr.type==AttributeType.ENUMERATION ? AttributeType.NMTOKEN : dtdAttr.type;

                        String namespaceURI = "";
                        String localName = dtdAttr.name;
                        String qname = localName;
                        int colon = qname.indexOf(':');
                        if(colon!=-1){
                            localName = qname.substring(colon+1);
                            String prefix = qname.substring(0, colon);
                            if(prefix.length()>0)
                                namespaceURI = namespaces.getNamespaceURI(prefix);
                        }
                        attributes.addAttribute(namespaceURI, localName, qname, type.name(), dtdAttr.value);
                    }
            }
        }
    }
}
