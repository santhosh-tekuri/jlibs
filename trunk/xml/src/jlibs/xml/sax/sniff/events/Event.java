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

/**
 * @author Santhosh Kumar T
 */
public abstract class Event{
    public static final int START = -1;
    public static final int DOCUMENT = 0;
    public static final int ELEMENT = 1;
    public static final int TEXT = 2;
    public static final int ATTRIBUTE = 3;
    public static final int COMMENT = 4;
    public static final int PI = 5;

    protected Event(DocumentOrder documentOrder){
        this.documentOrder = documentOrder;
    }

    public int order(){
        return documentOrder.get();
    }
    
    public abstract int type();

    public boolean hasChildren(){
        return false;
    }

    private Object resultWrapper;
    public String getResult(){
        return resultWrapper.toString();
    }

    private final DocumentOrder documentOrder;
    public void setResultWrapper(Object resultWrapper){
        this.resultWrapper = resultWrapper;
        documentOrder.increment();
    }
}
