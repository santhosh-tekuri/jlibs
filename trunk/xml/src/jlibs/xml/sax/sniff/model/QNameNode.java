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
import jlibs.core.lang.Util;
import jlibs.xml.sax.sniff.engine.context.Context;
import jlibs.xml.sax.sniff.events.Attribute;
import jlibs.xml.sax.sniff.events.Element;
import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.events.Namespace;

/**
 * @author Santhosh Kumar T
 */
public class QNameNode extends Node{
    public String uri;
    public String name;

    public QNameNode(String uri, String name){
        this.uri = uri;
        this.name = name;
        if(uri==null && name!=null)
            throw new IllegalArgumentException();
    }

    @Override
    public boolean canBeContext(){
        return getConstraintRoot().canBeContext();
    }

    @Override
    public boolean equivalent(Node node){
        if(node.getClass()==getClass()){
            QNameNode that=  (QNameNode)node;
            return Util.equals(this.uri, that.uri) && Util.equals(this.name, that.name);
        }
        return false;
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
    public boolean matches(Context context, Event event){
        switch(event.type()){
            case Event.ELEMENT:
                Element elem = (Element)event;
                return matchesQName(elem.uri, elem.name);
            case Event.ATTRIBUTE:
                Attribute attr = (Attribute)event;
                return matchesQName(attr.uri, attr.name);
            case Event.NAMESPACE:
                Namespace namespace = (Namespace)event;
                return this.name==null || this.name.equals(namespace.prefix);
            default:
                return false;
        }
    }

    @Override
    public String toString(){
        if(uri==null && name==null)
            return "*_"+depth;
        else if(uri!=null && name!=null){
            String prefix = root.nsContext.getPrefix(uri);
            if(prefix.length()>0)
                return String.format("%s:%s_%d", prefix, name, depth);
            else
                return name+'_'+depth;
        }else if(this.uri!=null){
            String prefix = root.nsContext.getPrefix(uri);
            return String.format("%s:*_%d", prefix, depth);
        }else
            throw new ImpossibleException();
    }
}
