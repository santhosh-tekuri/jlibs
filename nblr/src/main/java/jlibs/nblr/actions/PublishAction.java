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

package jlibs.nblr.actions;

/**
 * @author Santhosh Kumar T
 */
public class PublishAction implements Action{
    public static final String DISCARD = "discard";

    public final String name;
    public final int begin;
    public final int end;

    public PublishAction(String name, int begin, int end){
        this.name = name;
        this.begin = begin;
        this.end = end;
    }

    @Override
    public String javaCode(){
        String methodName = name.startsWith("#") ? name.substring(1) : name;
        if(methodName.equals(DISCARD) && begin==0 && end==0)
            return "buffer.pop(0, 0)";
        else
            return "handler."+methodName+"(buffer.pop("+begin+", "+end+"))";
    }

    @Override
    public String toString(){
        if(begin==0 && end==0)
            return name+"(data)";
        else
            return name+"(data["+begin+", "+-end+"])";
    }
}
