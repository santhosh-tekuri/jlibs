/**
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

package jlibs.xml.sax.sniff;

import javax.xml.namespace.QName;

/**
 * @author Santhosh Kumar T
 */
class Element extends Node{
    private QName qname;

    protected Element(Node parent, QName qname){
        super(parent);
        this.qname = qname;
    }

    /*-------------------------------------------------[ Matching ]---------------------------------------------------*/

    @Override
    public boolean matchesElement(String uri, String name){
        return qname.getNamespaceURI().equals(uri) && qname.getLocalPart().equals(name);
    }

    @Override
    protected String getStep(){
        if(qname.getPrefix().length()>0)
            return qname.getPrefix()+':'+qname.getLocalPart();
        else
            return qname.getLocalPart();
    }
}
