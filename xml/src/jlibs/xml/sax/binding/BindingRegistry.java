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

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class BindingRegistry<T>{
    public static final QName ANY = new QName("*", "*");
    
    private Map<QName, BindingRelation<T, ?>> registry;

    public <C> void register(QName qname, Binding<C> binding, Relation<T, C> relation){
        if(registry ==null)
            registry = new HashMap<QName, BindingRelation<T, ?>>();
        registry.put(qname, new BindingRelation<T, C>(binding, relation));
    }

    public <C> void register(QName qname, Binding<C> binding){
        register(qname, binding, TempRelation.<T, C>put());
    }

    public void register(QName qname){
        register(qname, TextBinding.INSTANCE);
    }

    public void register(QName qname, Relation<T, String> relation){
        register(qname, TextBinding.INSTANCE, relation);
    }

    public BindingRelation<T, ?> get(QName qname){
        if(registry==null)
            return null;
        else{
            BindingRelation<T, ?> br = registry.get(qname);
            if(br==null)
                br = registry.get(new QName("*", qname.getLocalPart()));
            if(br==null)
                br = registry.get(ANY);
            return br;
        }
    }
}
