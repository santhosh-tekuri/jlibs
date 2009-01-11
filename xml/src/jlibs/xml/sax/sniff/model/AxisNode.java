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

import org.jaxen.saxpath.Axis;
import jlibs.xml.sax.sniff.model.axis.Child;
import jlibs.xml.sax.sniff.model.axis.Attribute;
import jlibs.xml.sax.sniff.model.axis.Descendant;

/**
 * @author Santhosh Kumar T
 */
public abstract class AxisNode extends Node{
    public int type; 
    protected AxisNode(Node parent, int type){
        super(parent);
        this.type = type;
    }

    @Override
    public String toString(){
        return Axis.lookup(type)+"::";
    }

    public static AxisNode newInstance(Node parent, int type){
        switch(type){
            case Axis.CHILD:
                return new Child(parent);
            case Axis.ATTRIBUTE:
                return new Attribute(parent);
            case Axis.DESCENDANT:
                return new Descendant(parent);
            default:
                throw new UnsupportedOperationException("unsupported axis: "+Axis.lookup(type));
        }
    }
}
