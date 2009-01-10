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
class Attribute extends QNameNode{

    public Attribute(Node parent, QName qname, String namespace){
        super(parent, qname, namespace);
    }

    @Override
    public boolean matchesAttribute(String uri, String name){
        return matchesQName(uri, name);
    }

    @Override
    protected String getStep(){
        return '@'+super.getStep();
    }

    protected void println(){
        System.out.print(getStep());
        super.println();
    }
}
