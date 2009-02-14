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

/**
 * @author Santhosh Kumar T
 */
public class Element extends Event{
    private LocationStack locationStack;

    public Element(DocumentOrder documentOrder, LocationStack locationStack){
        super(documentOrder);
        this.locationStack = locationStack;
    }

    @Override
    public int type(){
        return ELEMENT;
    }

    @Override
    public boolean hasChildren(){
        return true;
    }

    public String uri;
    public String name;
    public String qname;

    public void setData(String uri, String name, String qname){
        this.uri = uri;
        this.name = name;
        this.qname = qname;
        setResultWrapper(locationStack);
    }

    public String getLanguage(){
        return locationStack.language();
    }

    @Override
    public String toString(){
        return String.format("<%s>", qname);
    }
}
