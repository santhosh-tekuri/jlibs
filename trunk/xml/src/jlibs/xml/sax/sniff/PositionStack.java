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

package jlibs.xml.sax.sniff;

import jlibs.xml.ClarkName;

import java.util.Map;
import java.util.ArrayDeque;
import java.util.HashMap;

/**
 * @author Santhosh Kumar T
 */
public class PositionStack{
    private ArrayDeque<Map<String, Integer>> stack = new ArrayDeque<Map<String, Integer>>();

    PositionStack(){
        reset();
    }

    public void reset(){
        stack.clear();
        stack.push(new HashMap<String, Integer>());
    }

    public int push(String uri, String localName){
        Map<String, Integer> map = stack.peekFirst();
        String clarkName = ClarkName.valueOf(uri, localName);
        Integer predicate = map.get(clarkName);
        if(predicate==null)
            predicate = 1;
        else
            predicate++;
        map.put(clarkName, predicate);

        stack.push(new HashMap<String, Integer>());

        return predicate;
    }

    public void pop(){
        stack.pop();
    }
}
