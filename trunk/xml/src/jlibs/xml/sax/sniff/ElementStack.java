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
import jlibs.xml.DefaultNamespaceContext;
import jlibs.xml.sax.sniff.model.Root;

import java.util.ArrayDeque;

/**
 * @author Santhosh Kumar T
 */
public class ElementStack{
    PositionStack positionStack = new PositionStack();
    private ArrayDeque<String> stack = new ArrayDeque<String>();
    private Root root;

    public ElementStack(Root root){
        this.root = root;
    }

    public void reset(){
        positionStack.reset();
        stack.clear();
    }
    
    public void push(String uri, String name){
        int pos = positionStack.push(uri, name);
        stack.addLast(qname(uri, name)+'['+pos+']');
    }

    public void pop(){
        positionStack.pop();
        stack.removeLast();
    }

    private String qname(String uri, String name){
        String prefix;
        if(root.nsContext instanceof DefaultNamespaceContext)
            prefix = ((DefaultNamespaceContext)root.nsContext).declarePrefix(uri);
        else
            prefix = root.nsContext.getPrefix(uri);

        if(prefix!=null)
            return prefix.length()==0 ? name : prefix+':'+name;
        else
            return ClarkName.valueOf(uri, name);
    }

    @Override
    public String toString(){
        StringBuilder buff = new StringBuilder();
        for(String elem: stack){
            buff.append('/');
            buff.append(elem);
        }
        return buff.toString();
    }
}
