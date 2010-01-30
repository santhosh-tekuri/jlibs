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

package jlibs.xml.sax;

import org.xml.sax.SAXException;

import javax.xml.namespace.QName;

/**
 * This interface tells how to convert this object to xml
 * 
 * @author Santhosh Kumar T
 */
public interface SAXProducer{
    /**
     * Serialize this object to xml
     *
     * @param rootElement   can be null, in case it should use its default root element
     * @param xml           xml document into which serialization to be done
     */
    public void serializeTo(QName rootElement, XMLDocument xml) throws SAXException;
}
