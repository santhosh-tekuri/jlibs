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

package jlibs.xml.sax.sniff.model.axis;

import jlibs.xml.sax.sniff.StringContent;
import jlibs.xml.sax.sniff.model.AxisNode;
import org.jaxen.saxpath.Axis;

/**
 * @author Santhosh Kumar T
 */
public class Child extends AxisNode{
    public Child(){
        super(Axis.CHILD);
    }

    @Override
    public boolean matchesElement(String uri, String name, int position){
        return true;
    }

    @Override
    public boolean matchesText(StringContent content){
        return true;
    }

    @Override
    public boolean matchesComment(String content){
        return true;
    }

    @Override
    public boolean matchesProcessingInstruction(String name){
        return true;
    }

    @Override
    public String toString(){
        return "child::";
    }
}
