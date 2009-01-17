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

package jlibs.xml.sax.sniff.model.functions;

import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.events.PI;
import org.jaxen.saxpath.Axis;

/**
 * @author Santhosh Kumar T
 */
public class StringFunction extends Function{
    @Override
    public String getName(){
        return "string";
    }

    @Override
    public boolean singleHit(){
        return true;
    }

    @Override
    public boolean consumable(Event event){
        return true;
    }

    @Override
    public String evaluate(Event event, String lastResult){
        switch(event.type()){
            case Event.TEXT:
            case Event.COMMENT:
                return lastResult!=null ? lastResult+event.getResult() : event.getResult();
            case Event.ATTRIBUTE:
                return axis==Axis.ATTRIBUTE ? event.getResult() : lastResult;
            case Event.PI:
                PI pi = (PI)event;
                return lastResult!=null ? lastResult+pi.data : pi.data;
            default:
                return lastResult;
        }
    }
}