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

import jlibs.core.lang.Util;
import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.events.PI;

/**
 * @author Santhosh Kumar T
 */
public class ProcessingInstruction extends Node{
    public String name;

    public ProcessingInstruction(String name){
        this.name = name;
    }

    @Override
    public boolean equivalent(Node node){
        if(node.getClass()==getClass()){
            ProcessingInstruction that = (ProcessingInstruction)node;
            return Util.equals(this.name,  that.name);
        }else
            return false;
    }

    @Override
    public boolean matches(Event event){
        if(event.type()==Event.PI){
            PI pi = (PI)event;
            return this.name==null || this.name.equals(pi.target);
        }else
            return false;
    }

    @Override
    public String toString(){
        return name==null ? "processing-instruction()" : "processing-instruction('"+name+"')";
    }
}