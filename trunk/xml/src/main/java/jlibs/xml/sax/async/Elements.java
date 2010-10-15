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
import java.util.Deque;

/**
 * @author Santhosh Kumar T
 */
class Elements{
    private final Namespaces namespaces;
    private final Attributes attributes;
    private SAXDelegate handler;

    public Elements(SAXDelegate handler, Namespaces namespaces, Attributes attributes){
        this.handler = handler;
        this.namespaces = namespaces;
        this.attributes = attributes;
    }

    private Deque<String> uris = new ArrayDeque<String>();
    private Deque<String> localNames = new ArrayDeque<String>();
    private Deque<String> names = new ArrayDeque<String>();

    public void reset(){
        uris.clear();
        localNames.clear();
        names.clear();
    }

    public String currentElementName(){
        return names.peek();
    }
    
    public void push1(QName qname){
        uris.push(qname.prefix); // replaced with actual uri in push2()
        localNames.push(qname.localName);
        names.push(qname.name);

        namespaces.push();
        attributes.reset();
    }

    public String push2() throws SAXException{
        String name = names.peek();
        String error = attributes.fixAttributes(name);
        if(error!=null)
            return error;

        String prefix = uris.pop();
        String uri = namespaces.getNamespaceURI(prefix);
        if(uri==null)
            return "Unbound prefix: "+prefix;
        uris.push(uri);
        handler.startElement(uri, localNames.peek(), name, attributes.get());

        return null;
    }

    public String pop(String elemName) throws SAXException{
        if(elemName==null)
            elemName = names.pop();
        else{
            String startName = names.pop();
            if(!startName.equals(elemName))
                return "expected </"+startName+">";
        }

        handler.endElement(uris.pop(), localNames.pop(), elemName);
        namespaces.pop();

        return null;
    }
}
