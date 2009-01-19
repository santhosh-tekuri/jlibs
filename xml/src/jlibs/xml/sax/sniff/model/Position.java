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

import jlibs.xml.sax.sniff.Context;
import jlibs.xml.sax.sniff.events.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class Position extends Node{
    public int pos;

    public Position(int pos){
        this.pos = pos;
    }

    @Override
    public boolean equivalent(Node node){
        if(node.getClass()==getClass()){
            Position that = (Position)node;
            return this.pos==that.pos;
        }else
            return false;
    }

    @Override
    public boolean matches(Event event){
        return event.type()==Event.ELEMENT;
    }

    @Override
    public String toString(){
        return "["+pos+"]";
    }

    /*-------------------------------------------------[ Tracking ]---------------------------------------------------*/

    private Map<Object, Integer> map = new HashMap<Object, Integer>();

    public boolean hit(Context context){
        if(parent!=context.node)
            context = context.parent;

        Integer pos = map.get(context.identity());
        if(pos==null)
            pos = 1;
        else
            pos++;
        map.put(context.identity(), pos);
        return this.pos == pos;
    }

    public void clearHitCount(Context context){
        map.remove(context.identity());
    }

    public int getHitCount(Context context){
        Integer pos = map.get(context.identity());
        if(pos==null)
            pos = 0;
        return pos;
    }

    /*-------------------------------------------------[ Reset ]---------------------------------------------------*/

    @Override
    public void reset(){
        map.clear();
        super.reset();
    }
}
