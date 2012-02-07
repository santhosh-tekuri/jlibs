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

package jlibs.wadl.runtime;

import jlibs.wadl.model.Resource;
import org.apache.xerces.xs.XSModel;

import java.util.*;

/**
 * @author Santhosh Kumar T
 */
public class Path{
    public final Path parent;
    public final List<Path> children = new ArrayList<Path>();

    public final String name;
    public String value;
    public Resource resource;
    public XSModel schema;

    public Path(Path parent, String name){
        this.parent = parent;
        if(parent!=null)
            parent.children.add(this);
        this.name = name;
    }

    public String variable(){
        if(name.startsWith("{") && name.endsWith("}"))
            return name.substring(1, name.length()-1);
        else
            return null;
    }
    
    public String resolve(){
        return value!=null ? value : name;
    }
    
    public String toString(Path from){
        Deque<Path> queue = new ArrayDeque<Path>();
        Path path = this;
        while(path!=from){
            queue.push(path);
            path = path.parent;
        }
        StringBuilder buff = new StringBuilder();
        while(!queue.isEmpty()){
            path = queue.pop();
            if(path.name!=null){
                if(buff.length()>0)
                    buff.append('/');
                buff.append(path.name);
            }
        }
        return buff.length()==0 ? "/" : buff.toString();
    }

    @Override
    public String toString(){
        return toString(null);
    }

    public Path add(String pathString){
        Path path = this;
        StringTokenizer stok = new StringTokenizer(pathString, "/");
        while(stok.hasMoreTokens()){
            String token = stok.nextToken();
            Path childPath = null;
            if(token.startsWith("{") && token.endsWith("}")){
                for(Path child: path.children){
                    if(child.variable()!=null){
                        childPath = child;
                        break;
                    }
                }
                if(childPath==null)
                    childPath = new Path(path, token);
            }else{
                for(Path child: path.children){
                    if(child.variable()==null && child.name.equals(token)){
                        childPath = child;
                        break;
                    }
                }
                if(childPath==null)
                    childPath = new Path(path, token);
            }
            path = childPath;
        }
        return path;
    }
    
    public Path get(String pathString){
        Path path = this;
        StringTokenizer stok = new StringTokenizer(pathString, "/");
        while(stok.hasMoreTokens()){
            String token = stok.nextToken();
            Path p = null;
            if(token.equals("."))
                p = path;
            else if(token.equals(".."))
                p = path.parent;
            else{
                for(Path child: path.children){
                    if(child.variable()!=null || child.name.equals(token)){
                        p = child;
                        break;
                    }
                }
            }
            if(p==null)
                return null;
            path = p;
        }
        return path;
    }
    
    public Path getRoot(){
        Path path = this;
        while(path.parent!=null)
            path = path.parent;
        return path;
    }
    
    public Deque<Path> getStack(){
        Deque<Path> stack = new ArrayDeque<Path>();
        Path path = this;
        while(path!=null){
            stack.push(path);
            path = path.parent;
        }
        return stack;
    }
    
    public XSModel getSchema(){
        Path path = this;
        while(path!=null){
            if(path.schema!=null)
                return path.schema;
            path = path.parent;
        }
        return null;
    }
}
