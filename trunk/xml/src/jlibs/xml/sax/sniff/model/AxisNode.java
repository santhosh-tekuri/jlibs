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

import jlibs.xml.sax.sniff.model.axis.*;
import org.jaxen.saxpath.Axis;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public abstract class AxisNode extends Node{
    public int type; 

    protected AxisNode(int type){
        this.type = type;
    }

    @Override
    public boolean equivalent(Node node){
        return node.getClass()==getClass();
    }

    @Override
    public String toString(){
        return Axis.lookup(type)+"::_"+depth;
    }

    public static AxisNode newInstance(int type){
        switch(type){
            case Axis.CHILD:
                return new Child();
            case Axis.ATTRIBUTE:
                return new Attribute();
            case Axis.DESCENDANT:
            case Axis.DESCENDANT_OR_SELF:
                return new Descendant(type);
            case Axis.NAMESPACE:
                return new Namespace();
            default:
                throw new UnsupportedOperationException("unsupported axis: "+Axis.lookup(type));
        }
    }

    public List<Following> descendantFollowings = new ArrayList<Following>();
}
