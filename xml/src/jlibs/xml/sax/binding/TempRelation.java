/*
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

package jlibs.xml.sax.binding;

/**
 * This relation puts child object in parent's temp
 * 
 * @author Santhosh Kumar T
 */
public class TempRelation<P, C> extends Relation<P, C>{
    private boolean put;

    private TempRelation(boolean put){
        this.put = put;
    }

    @Override
    public void finished(SAXContext<P> parent, SAXContext<C> current){
        if(put)
            parent.put(current.element, current.object);
        else
            parent.add(current.element, current.object);
    }

    private static final TempRelation<?, ?> PUT = new TempRelation<Object, Object>(true);
    @SuppressWarnings({"unchecked"})
    public static <P, C> TempRelation<P, C> put(){
        return (TempRelation<P, C>)PUT;
    }

    private static final TempRelation<?, ?> ADD = new TempRelation<Object, Object>(false);
    @SuppressWarnings({"unchecked"})
    public static <P, C> TempRelation<P, C> add(){
        return (TempRelation<P, C>)ADD;
    }
}
