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

package jlibs.xml.sax.crawl;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;

/**
 * @author Santhosh Kumar T
 */
public class AttributeLink extends Link{
    protected QName attribute;

    public AttributeLink(QName attribute, String... extensions){
        super(extensions);
        this.attribute = attribute;
    }

    public AttributeLink(String attributeName, String... extensions){
        this(new QName(attributeName), extensions);
    }

    @Override
    public AttributeLink pushElement(String uri, String name){
        return (AttributeLink)super.pushElement(uri, name);
    }

    public String resolve(Attributes atts) throws MalformedURLException{
        return atts.getValue(attribute.getNamespaceURI(), attribute.getLocalPart());
    }

    public void repair(AttributesImpl atts, String newLocation){
        int index = atts.getIndex(attribute.getNamespaceURI(), attribute.getLocalPart());
        atts.setValue(index, newLocation);
    }
}
