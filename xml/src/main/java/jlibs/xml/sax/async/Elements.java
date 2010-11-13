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
    private final AsyncXMLReader reader;
    private final SAXDelegate handler;
    private final Namespaces namespaces;
    private final Attributes attributes;

    public Elements(AsyncXMLReader reader, SAXDelegate handler, Namespaces namespaces, Attributes attributes){
        this.reader = reader;
        this.handler = handler;
        this.namespaces = namespaces;
        this.attributes = attributes;
    }

    private QName qnameStack[] = new QName[15];
    private String uriStack[] = new String[15];
    int free = 0;
    int freeLock = 0;

    public void reset(){
        free = freeLock = 0;
    }

    public char[] currentElementNameAsCharArray(){
        return qnameStack[free-1].chars;
    }
    
    public String currentElementName(){
        return qnameStack[free-1].name;
    }

    public void push1(QName qname){
        if(free== qnameStack.length){
            qnameStack = Arrays.copyOf(qnameStack, free*2);
            uriStack = Arrays.copyOf(uriStack, free*2);
        }
        qnameStack[free++] = qname;

        namespaces.push();
        attributes.reset();
    }

    public void push2() throws SAXException{
        QName bucket = qnameStack[free-1];
        String error = attributes.fixAttributes(bucket.name);
        if(error!=null)
            reader.fatalError(error);

        String uri = namespaces.getNamespaceURI(bucket.prefix);
        if(uri==null)
            reader.fatalError("Unbound prefix: "+bucket.prefix);
        uriStack[free-1] = uri;

        handler.startElement(uri, bucket.localName, bucket.name, attributes.get());
    }

    public boolean pop() throws SAXException{
        if(free==freeLock)
            reader.fatalError("The element \""+currentElementName()+"\" must start and end within the same entity");
        QName qname = qnameStack[free-1];
        handler.endElement(uriStack[free-1], qname.localName, qname.name);
        free--;
        namespaces.pop();
        return free==0;
    }
}
