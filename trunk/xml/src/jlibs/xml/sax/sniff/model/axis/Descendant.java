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

import jlibs.xml.sax.sniff.engine.context.Context;
import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.model.AxisNode;

/**
 * @author Santhosh Kumar T
 */
public class Descendant extends AxisNode{
    public Descendant(int axis){
        super(axis);
    }

    @Override
    public boolean canBeContext(){
        return true;
    }

    @Override
    public boolean matches(Context context, Event event){
        switch(event.type()){
            case Event.DOCUMENT:
            case Event.ELEMENT:
            case Event.TEXT:
            case Event.COMMENT:
            case Event.PI:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean canConsume(){
        return true;
    }

    @Override
    public boolean consumable(Event event){
        return matches(null, event);
    }
}
