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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author Santhosh Kumar T
 */
public abstract class Binding<T>{
    public final BindingRegistry<T> registry;

    public Binding(){
        this(new BindingRegistry<T>());
    }

    public Binding(BindingRegistry<T> registry){
        this.registry = registry;
    }

    public void start(SAXContext<T> current, Attributes attributes) throws SAXException{}
    public void text(SAXContext<T> current, String text) throws SAXException{}
    public void finish(SAXContext<T> current) throws SAXException{}

    /*-------------------------------------------------[ DO NOTHING ]---------------------------------------------------*/
    
    private static final Binding<?> DO_NOTHING = new Binding(){};
    @SuppressWarnings({"unchecked"})
    public static <T> Binding<T> doNothing(){
        return (Binding<T>)DO_NOTHING;
    }
}
