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

import jlibs.core.lang.ImpossibleException;

/**
 * @author Santhosh Kumar T
 */
public class QNameNode extends Node{
    public String uri;
    public String name;

    public QNameNode(Node parent, String uri, String name){
        super(parent);
        this.uri = uri;
        this.name = name;
        if(uri==null && name!=null)
            throw new IllegalArgumentException();
    }

    @SuppressWarnings({"SimplifiableIfStatement"})
    protected boolean matchesQName(String uri, String name){
        if(this.uri==null && this.name==null)
            return true;
        else if(this.uri!=null && this.name!=null)
            return this.uri.equals(uri) && this.name.equals(name);
        else if(this.uri!=null)
            return this.uri.equals(uri);
        else
            throw new ImpossibleException();
    }

    @Override
    public boolean matchesElement(String uri, String name, int position){
        return matchesQName(uri, name);
    }

    @Override
    public boolean matchesAttribute(String uri, String name, String value){
        return matchesQName(uri, name);
    }

    @Override
    public String toString(){
        if(uri==null && name==null)
            return "*";
        else if(uri!=null && name!=null){
            String prefix = root.nsContext.getPrefix(uri);
            if(prefix.length()>0)
                return String.format("%s:%s", prefix, name);
            else
                return name;
        }else if(this.uri!=null){
            String prefix = root.nsContext.getPrefix(uri);
            return String.format("%s:*", prefix);
        }else
            throw new ImpossibleException();
    }
}
