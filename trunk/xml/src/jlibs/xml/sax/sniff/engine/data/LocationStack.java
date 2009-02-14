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

import jlibs.xml.DefaultNamespaceContext;

import javax.xml.namespace.NamespaceContext;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class LocationStack{
    private DefaultNamespaceContext nsContext;
    private NamespaceContext givenNSContext;

    public LocationStack(NamespaceContext givenNSContext){
        this.givenNSContext = givenNSContext;
    }

    public void reset(){
        nsContext = new DefaultNamespaceContext();
        stack.clear();
    }
    
    private String getPrefix(String uri){
        String prefix = nsContext.getPrefix(uri);
        if(prefix==null){
            prefix = givenNSContext.getPrefix(uri);
            if(prefix!=null)
                nsContext.declarePrefix(prefix, uri);
            else
                prefix = nsContext.declarePrefix(uri);
        }
        return prefix;
    }

    private String qname(String uri, String name){
        String prefix = getPrefix(uri);
        return prefix.length()==0 ? name : prefix+':'+name;
    }

    private ArrayDeque<Info> stack = new ArrayDeque<Info>();

    public void pushElement(String uri, String name, String lang){
        Info info = new Info();
        info.elem = qname(uri, name);

        if(!stack.isEmpty()){
            Info peekInfo = stack.getLast();
            Integer position = peekInfo.positionMap.get(info.elem);
            if(position==null)
                position = 1;
            else
                position++;
            peekInfo.positionMap.put(info.elem, position);
            info.elemntPos = position;
        }

        if(lang==null)
            lang = language();
        info.lang = lang;

        stack.addLast(info);
    }

    public void popElement(){
        stack.removeLast();
    }
    
    private static class Info{
        String elem;
        String lang;

        Map<String, Integer> positionMap = new HashMap<String, Integer>();
        int elemntPos = 1;
    }

    /*-------------------------------------------------[ Queries ]---------------------------------------------------*/
    
    public String element(){
        StringBuilder buff = new StringBuilder();
        for(Info info: stack){
            buff.append('/');
            buff.append(info.elem).append('[').append(info.elemntPos).append(']');
        }
        return buff.toString();
    }

    public String language(){
        return stack.isEmpty() ? "" : stack.getLast().lang;
    }

    @Override
    public String toString(){
        return element();
    }
}
