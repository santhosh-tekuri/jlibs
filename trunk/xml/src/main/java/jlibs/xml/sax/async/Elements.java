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

import jlibs.xml.sax.SAXDelegate;
import org.xml.sax.SAXException;

import java.util.Arrays;

/**
 * @author Santhosh Kumar T
 */
class Elements{
    private final Namespaces namespaces;
    private final Attributes attributes;
    private final SAXDelegate handler;

    public Elements(SAXDelegate handler, Namespaces namespaces, Attributes attributes){
        this.handler = handler;
        this.namespaces = namespaces;
        this.attributes = attributes;
    }

    private String stack[] = new String[15*3];
    private int free = 0;

    public void reset(){
        free = 0;
    }

    public String currentElementName(){
        return stack[free-1];
    }
    
    public void push1(QName qname){
        free += 3;
        if(free>stack.length)
            stack = Arrays.copyOf(stack, free*2);
        stack[free-3] = qname.prefix; // replaced with actual uri in push2()
        stack[free-2] = qname.localName;
        stack[free-1] = qname.name;

        namespaces.push();
        attributes.reset();
    }

    public String push2() throws SAXException{
        String name = stack[free-1];
        String error = attributes.fixAttributes(name);
        if(error!=null)
            return error;

        String prefix = stack[free-3];
        String uri = namespaces.getNamespaceURI(prefix);
        if(uri==null)
            return "Unbound prefix: "+prefix;
        stack[free-3] = uri;

        handler.startElement(uri, stack[free-2], name, attributes.get());

        return null;
    }

    public void pop() throws SAXException{
        handler.endElement(stack[free-3], stack[free-2], stack[free-1]);
        free -= 3;
        namespaces.pop();
    }
}
