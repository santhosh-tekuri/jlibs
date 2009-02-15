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

/**
 * @author Santhosh Kumar T
 */
public class NodeItem implements NodeTypes, Comparable<NodeItem>{
    private int order;
    public int type;
    public String location;
    public String value;

    public NodeItem(int order, int type, String location, String value){
        this.order = order;
        this.type = type;
        this.location = location;
        this.value = value;
    }

    @Override
    public int compareTo(NodeItem that){
        return this.order - that.order;
    }

    @Override
    public String toString(){
        return value!=null ? location+'='+value : location;
    }
}
