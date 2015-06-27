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

package jlibs.xml.sax.binding;

import jlibs.xml.sax.binding.impl.Registry;
import jlibs.xml.sax.binding.impl.Relation;

import javax.xml.namespace.QName;

/**
 * @author Santhosh Kumar T
 */
@SuppressWarnings({"unchecked"})
public class BindingRegistry{
    Registry registry = new Registry();

    public BindingRegistry(Class... classes){
        for(Class clazz: classes)
            register(clazz);
    }

    public BindingRegistry(QName qname, Class clazz){
        register(qname, clazz);
    }

    public void register(Class clazz){
        register(null, clazz);
    }

    public void register(QName qname, Class clazz){
        try{
            String implQName = "${package}.${class}Impl".replace("${package}", clazz.getPackage()!=null?clazz.getPackage().getName():"")
                    .replace("${class}", clazz.getSimpleName());
            if(implQName.startsWith(".")) // default package
                implQName = implQName.substring(1);
            Class implClass = clazz.getClassLoader().loadClass(implQName);
            if(qname==null)
                qname = (QName)implClass.getDeclaredField("ELEMENT").get(null);
            if(qname==null)
                throw new IllegalArgumentException("can't find qname for: "+implClass);

            jlibs.xml.sax.binding.impl.Binding binding = (jlibs.xml.sax.binding.impl.Binding)implClass.getDeclaredField("INSTANCE").get(null);
            registry.register(qname, 0, binding, 0, Relation.DO_NOTHING);
        }catch(ClassNotFoundException ex){
            throw new RuntimeException(ex);
        } catch(NoSuchFieldException ex){
            throw new RuntimeException(ex);
        } catch(IllegalAccessException ex){
            throw new RuntimeException(ex);
        }
    }
}
