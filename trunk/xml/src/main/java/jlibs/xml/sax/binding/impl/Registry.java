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

package jlibs.xml.sax.binding.impl;

import jlibs.xml.QNameFake;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class Registry{
    public static final String STAR = "*";
    public static final QName ANY = new QName(STAR, STAR);
    
    public Map<QName, BindingRelation> registry;

    public Registry register(QName qname, int bindingState, Binding binding, int relationState, Relation relation){
        if(registry ==null)
            registry = new HashMap<QName, BindingRelation>();
        BindingRelation bindingRelation = new BindingRelation(qname, bindingState, binding, relationState, relation);
        registry.put(qname, bindingRelation);
        return bindingRelation.binding.registry;
    }

    public void register(QName qname, int bindingState, Binding binding){
        register(qname, bindingState, binding, 0, TempRelation.PUT);
    }

    public void register(QName qname){
        register(qname, 0, TextBinding.INSTANCE);
    }

    public void register(QName qname, int relationState, Relation relation){
        register(qname, 0, TextBinding.INSTANCE, relationState, relation);
    }

    @SuppressWarnings({"SuspiciousMethodCalls"})
    public BindingRelation get(QNameFake qname){
        if(registry==null)
            return null;
        else{
            BindingRelation br = registry.get(qname);
            String namespaceURI = qname.namespaceURI;
            String localPart = qname.localPart;
            if(br==null)
                br = registry.get(qname.set(STAR, localPart));
            if(br==null)
                br = registry.get(qname.set(namespaceURI, STAR));
            if(br==null)
                br = registry.get(ANY);
            return br;
        }
    }
}
