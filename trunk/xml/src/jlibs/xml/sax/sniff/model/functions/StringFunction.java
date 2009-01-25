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

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;

/**
 * @author Santhosh Kumar T
 */
public class StringFunction extends Function{
    @Override
    public String getName(){
        return "string";
    }

    @Override
    public QName resultType(){
        return XPathConstants.STRING;
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
            case Event.DOCUMENT:
            case Event.ELEMENT:
                return lastResult==null ? "" : lastResult;

            case Event.TEXT:
                return lastResult!=null ? lastResult+event.getResult() : event.getResult();

            case Event.COMMENT:
            case Event.ATTRIBUTE:
                return lastResult==null ? event.getResult() : lastResult;
            
            case Event.PI:
                return lastResult==null ? ((PI)event).data : lastResult;
            
            default:
                return lastResult;
        }
    }

    @Override
    public String defaultResult(){
        return "";
    }
}