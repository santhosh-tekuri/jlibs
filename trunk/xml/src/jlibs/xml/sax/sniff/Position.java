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
public class Position extends Node implements Match{
    private int pos;
    
    protected Position(Node parent, int pos){
        super(parent);
        this.pos = pos;
    }

    @Override
    public boolean matchesStartElement(String uri, String name, int pos){
        return this.pos==pos;
    }

    @Override
    protected String getStep(){
        return "["+pos+"]";
    }

    @Override
    protected boolean canMerge(Node node){
        if(node.getClass()==getClass()){
            Position that = (Position)node;
            return this.pos==that.pos;
        }
        return false;
    }
}
