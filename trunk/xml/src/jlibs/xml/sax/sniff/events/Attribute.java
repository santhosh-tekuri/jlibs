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

package jlibs.xml.sax.sniff.events;

import jlibs.xml.sax.sniff.engine.data.LocationStack;
import org.xml.sax.Attributes;

/**
 * @author Santhosh Kumar T
 */
public class Attribute extends Event{
    public Attribute(DocumentOrder documentOrder, LocationStack locationStack){
        super(documentOrder, locationStack);
    }

    @Override
    public int type(){
        return ATTRIBUTE;
    }

    @Override
    public String location(){
        return locationStack.attribute(uri, name);
    }

    @Override
    protected String value(){
        return value;
    }

    @Override
    public String localName(){
        return name;
    }

    @Override
    public String namespaceURI(){
        return uri;
    }

    @Override
    public String qualifiedName(){
        return qname;
    }

    public String uri;
    public String name;
    public String qname;
    public String value;

    public void setData(Attributes attrs, int index){
        uri = attrs.getURI(index);
        name = attrs.getLocalName(index);
        qname = attrs.getQName(index);
        value = attrs.getValue(index);
        hit();
    }

    @Override
    public String toString(){
        return String.format("@%s", qname);
    }
}
