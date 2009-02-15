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

    public DefaultNamespaceContext getNsContext(){
        return nsContext;
    }

    public void reset(){
        nsContext = new DefaultNamespaceContext();
        stack.clear();
        stack.push(new Info());
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

        if(!stack.isEmpty())
            info.elemntPos = stack.getLast().updateElementPosition(info.elem);

        if(lang==null)
            lang = language();
        info.lang = lang;

        stack.addLast(info);
    }

    public void addText(){
        stack.getLast().textCount++;
    }
    
    public void addPI(String target){
        stack.getLast().updatePIPosition(target);
    }

    public void addComment(){
        stack.getLast().commentCount++;
    }

    public void popElement(){
        stack.removeLast();
    }
    
    private static class Info{
        private String elem;
        private String lang;

        private int elemntPos = 1;

        private static int updatePosition(Map<String, Integer> map, String key){
            Integer position = map.get(key);
            if(position==null)
                position = 1;
            else
                position++;
            map.put(key, position);

            return position;
        }

        private Map<String, Integer> elemMap;
        public int updateElementPosition(String qname){
            if(elemMap==null)
                elemMap = new HashMap<String, Integer>();
            return updatePosition(elemMap, qname);
        }

        private Map<String, Integer> piMap = new HashMap<String, Integer>();
        public int updatePIPosition(String target){
            if(piMap==null)
                piMap = new HashMap<String, Integer>();
            return updatePosition(piMap, target);
        }

        private int textCount;
        private int commentCount;
    }

    /*-------------------------------------------------[ Queries ]---------------------------------------------------*/
    
    public String element(){
        StringBuilder buff = new StringBuilder();
        for(Info info: stack){
            if(info.elem!=null){
                buff.append('/');
                buff.append(info.elem).append('[').append(info.elemntPos).append(']');
            }
        }
        return buff.toString();
    }

    public String attribute(String uri, String name){
        return String.format("%s/@%s", element(), qname(uri, name));
    }

    public String namespace(String prefix){
        return String.format("%s/namespace::%s", element(), prefix);
    }

    public String text(){
        return String.format("%s/text()[%d]", element(), stack.peekLast().textCount);
    }

    public String processingInstruction(String target){
        return String.format("%s/processing-instruction('%s')[%d]", element(), target, stack.peekLast().piMap.get(target));
    }

    public String comment(){
        return String.format("%s/comment()[%d]", element(), stack.peekLast().commentCount);
    }

    public String language(){
        return stack.isEmpty() ? "" : stack.getLast().lang;
    }

    @Override
    public String toString(){
        return element();
    }
}
