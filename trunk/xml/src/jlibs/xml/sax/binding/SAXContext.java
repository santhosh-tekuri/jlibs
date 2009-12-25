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
import org.xml.sax.Attributes;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public abstract class SAXContext<T>{
    public T object;

    public final NamespaceMap namespaceMap;
    public final QName element;
    protected SAXContext(NamespaceMap namespaceMap, QName element){
        this.namespaceMap = namespaceMap;
        this.element = element;
    }

    public Map<QName, Object> temp;

    public Map<QName, Object> temp(){
        if(temp==null)
            temp = new HashMap<QName, Object>();
        return temp;
    }

    /*-------------------------------------------------[ Storing ]---------------------------------------------------*/

    public void put(QName qname, Object value){
        if(value!=null)
            temp().put(qname, value);
    }

    @SuppressWarnings({"unchecked"})
    public void add(QName qname, Object value){
        if(value!=null){
            temp();
            List<Object> list = (List<Object>)temp.get(qname);
            if(list==null)
                temp.put(qname, list=new ArrayList<Object>());
            list.add(value);
        }
    }

    /*-------------------------------------------------[ Storing Attributes ]---------------------------------------------------*/
    
    public void putAttributes(Attributes attributes){
        int attrCount = attributes.getLength();
        if(attrCount>0){
            temp();
            for(int i=0; i<attrCount; i++){
                QName qname = new QName(attributes.getURI(i), attributes.getLocalName(i));
                temp.put(qname, attributes.getValue(i));
            }
        }
    }

    public void putAttribute(Attributes attributes, QName qname){
        String value = attributes.getValue(qname.getNamespaceURI(), qname.getLocalPart());
        if(value!=null)
            put(qname, value);
    }

    /*-------------------------------------------------[ Retriving ]---------------------------------------------------*/
    
    public <X> X get(QName qname){
        return get(qname, (X)null);
    }

    @SuppressWarnings({"unchecked"})
    public <X> X get(QName qname, X defaultValue){
        if(temp==null)
            return defaultValue;
        X value = (X)temp.get(qname);
        return value!=null ? value : defaultValue;
    }
}
