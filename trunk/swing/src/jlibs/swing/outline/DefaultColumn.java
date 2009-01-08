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

package jlibs.swing.outline;

import jlibs.core.graph.Visitor;

/**
 * @author Santhosh Kumar T
 */
public class DefaultColumn implements Column{
    private String name;
    private Class clazz;
    private Visitor visitor;

    public DefaultColumn(String name, Class clazz, Visitor visitor){
        this.name = name;
        this.clazz = clazz;
        this.visitor = visitor;
    }

    @Override
    public String getColumnName(){
        return name;
    }

    @Override
    public Class getColumnClass(){
        return clazz;
    }

    @Override    
    @SuppressWarnings({"unchecked"})
    public Object getValueFor(Object obj){
        return visitor.visit(obj);
    }

    @Override
    public boolean isCellEditable(Object obj){
        return false;
    }

    @Override
    public void setValueFor(Object obj, Object value){
        throw new UnsupportedOperationException();
    }
}
