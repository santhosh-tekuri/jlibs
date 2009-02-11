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

package jlibs.xml.sax.sniff.engine.data;

import java.util.ArrayDeque;

/**
 * @author Santhosh Kumar T
 */
public class LangStack{
    private ArrayDeque<String> stack = new ArrayDeque<String>();

    public void push(String lang){
        if(lang==null)
            lang = stack.peek();
        if(lang==null)
            lang = "";

        stack.push(lang);
    }

    public void pop(){
        stack.pop();
    }

    public String getLanguage(){
        return stack.peek();
    }
}
