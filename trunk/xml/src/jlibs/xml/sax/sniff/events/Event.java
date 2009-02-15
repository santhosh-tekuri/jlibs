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

import jlibs.xml.sax.sniff.NodeItem;
import jlibs.xml.sax.sniff.NodeTypes;
import jlibs.xml.sax.sniff.engine.data.LocationStack;

/**
 * @author Santhosh Kumar T
 */
public abstract class Event implements NodeTypes{
    public static final int START = -2;

    protected LocationStack locationStack;
    
    protected Event(DocumentOrder documentOrder, LocationStack locationStack){
        this.documentOrder = documentOrder;
        this.locationStack = locationStack;
    }

    public int order(){
        return documentOrder.get();
    }
    
    public abstract int type();

    public boolean hasChildren(){
        return false;
    }

    public abstract String location();
    protected abstract String value();

    public abstract String localName();
    public abstract String namespaceURI();
    public abstract String qualifiedName();

    public String getValue(){
        if(value==null)
            value = value();
        return value;
    }

    private NodeItem result;
    private String value;
    public NodeItem getResult(){
        if(result==null)
            result = new NodeItem(this);
        return result;
    }

    private final DocumentOrder documentOrder;
    protected void hit(){
        result = null;
        value = null;
        documentOrder.increment();
    }
}
