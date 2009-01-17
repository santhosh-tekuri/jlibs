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

import jlibs.core.lang.NotImplementedException;
import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.model.Node;

/**
 * @author Santhosh Kumar T
 */
public abstract class Function extends Node{
    public abstract String getName();
    public abstract boolean singleHit();
    public abstract String evaluate(Event event, String lastResult);
    public abstract String defaultResult();
    
    @Override
    public boolean matches(Event event){
        return true;
    }

    @Override
    public boolean equivalent(Node node){
        return node.getClass()==getClass();
    }

    @Override
    public String toString(){
        return getName()+"()";
    }

    public static Function newInstance(String name){
        if("name".equals(name))
            return new Name();
        else if("count".equals(name))
            return new Count();
        else if("string".equals(name))
            return new StringFunction();
        else
            throw new NotImplementedException("function "+name+"() is not supported");
    }
}
