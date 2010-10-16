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

import jlibs.xml.NamespaceMap;
import jlibs.xml.sax.SAXDelegate;
import org.xml.sax.SAXException;

/**
 * @author Santhosh Kumar T
 */
class Namespaces{
    private NamespaceMap namespaces = new NamespaceMap();
    private NamespaceMap freeNamespaces;

    private SAXDelegate handler;

    public Namespaces(SAXDelegate handler){
        this.handler = handler;
    }

    public void reset(){
        if(namespaces.parent()!=null)
            namespaces = new NamespaceMap();
    }

    public String getNamespaceURI(String prefix){
        return namespaces.getNamespaceURI(prefix);
    }
    
    public void push(){
        if(freeNamespaces!=null){
            NamespaceMap newOne = freeNamespaces;
            freeNamespaces = freeNamespaces.parent();
            newOne.setParent(namespaces);
            namespaces = newOne;
        }else
            namespaces = new NamespaceMap(namespaces);
    }

    public void add(String prefix, String namespace) throws SAXException{
        namespaces.put(prefix, namespace);
        handler.startPrefixMapping(prefix, namespace);
    }

    public void pop() throws SAXException{
        if(namespaces.map()!=null){
            for(String nsPrefix: namespaces.map().keySet())
                handler.endPrefixMapping(nsPrefix);
            namespaces.clear();
        }

        NamespaceMap current = namespaces;
        namespaces = namespaces.parent();

        current.setParent(freeNamespaces);
        freeNamespaces = current;
    }
}
