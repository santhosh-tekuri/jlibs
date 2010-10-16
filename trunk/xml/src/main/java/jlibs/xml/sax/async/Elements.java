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

import java.util.ArrayDeque;

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

    private final ArrayDeque<String> uris = new ArrayDeque<String>();
    private final ArrayDeque<String> localNames = new ArrayDeque<String>();
    private final ArrayDeque<String> names = new ArrayDeque<String>();

    public void reset(){
        uris.clear();
        localNames.clear();
        names.clear();
    }

    public String currentElementName(){
        return names.peekFirst();
    }
    
    public void push1(QName qname){
        uris.addFirst(qname.prefix); // replaced with actual uri in push2()
        localNames.addFirst(qname.localName);
        names.addFirst(qname.name);

        namespaces.push();
        attributes.reset();
    }

    public String push2() throws SAXException{
        String name = names.peekFirst();
        String error = attributes.fixAttributes(name);
        if(error!=null)
            return error;

        String prefix = uris.pollFirst();
        String uri = namespaces.getNamespaceURI(prefix);
        if(uri==null)
            return "Unbound prefix: "+prefix;
        uris.addFirst(uri);
        
        handler.startElement(uri, localNames.peekFirst(), name, attributes.get());

        return null;
    }

    public void pop() throws SAXException{
        handler.endElement(uris.pollFirst(), localNames.pollFirst(), names.pollFirst());
        namespaces.pop();
    }
}
