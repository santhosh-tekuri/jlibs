/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T <santhosh.tekuri@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.core.graph.sequences;

/**
 * @author Santhosh Kumar T
 */
public class Element<E>{
    private int index;
    private E item;

    public Element(){
        this(-1, null);
    }
    
    public Element(int index, E item){
        set(index, item);
    }

    public void set(int index, E item){
        this.index = index;
        this.item = item;
    }

    public void set(E item){
        this.item = item;
        index++;
    }

    public E get(){
        return item;
    }

    public int index(){
        return index;
    }

    public boolean finished(){
        return index>=0 && item==null;
    }
    
    public void reset(){
        index = -1;
        item = null;
    }
}

