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

package jlibs.xml;

import javax.xml.namespace.QName;

/**
 * @author Santhosh Kumar T
 */
public class QNameFake{
    public String namespaceURI;
    public String localPart;

    public QNameFake set(String namespaceURI, String localPart){
        this.namespaceURI = namespaceURI;
        this.localPart = localPart;
        return this;
    }

    @Override
    public int hashCode(){
        return namespaceURI.hashCode() ^ localPart.hashCode();
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof QName){
            QName that = (QName)obj;
            return this.namespaceURI.equals(that.getNamespaceURI()) && this.localPart.equals(that.getLocalPart());
        }else if(obj instanceof QNameFake){
            QNameFake that = (QNameFake)obj;
            return this.namespaceURI.equals(that.namespaceURI) && this.localPart.equals(that.localPart);
        }
        return false;
    }
}
