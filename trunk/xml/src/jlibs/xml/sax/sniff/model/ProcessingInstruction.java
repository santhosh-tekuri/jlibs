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

package jlibs.xml.sax.sniff.model;

/**
 * @author Santhosh Kumar T
 */
public class ProcessingInstruction extends Node{
    public String name;

    public ProcessingInstruction(Node parent, String name){
        super(parent);
        this.name = name;
    }

    @Override
    public boolean matchesProcessingInstruction(String name){
        return this.name==null || this.name.equals(name);
    }

    @Override
    public String toString(){
        return "comment()";
    }
}