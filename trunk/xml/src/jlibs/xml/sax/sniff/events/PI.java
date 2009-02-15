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
public class PI extends Event{
    public PI(DocumentOrder documentOrder, LocationStack locationStack){
        super(documentOrder, locationStack);
    }

    @Override
    public int type(){
        return PI;
    }

    @Override
    public String location(){
        return locationStack.processingInstruction(target);
    }

    @Override
    protected String value(){
        return data;
    }

    @Override
    public String localName(){
        return target;
    }

    @Override
    public String namespaceURI(){
        return "";
    }

    @Override
    public String qualifiedName(){
        return target;
    }

    public String target;
    public String data;

    public void setData(String target, String data){
        this.target = target;
        this.data = data;
        hit();
    }

    @Override
    public String toString(){
        return String.format("<?%s %s?>", target, data);
    }
}
