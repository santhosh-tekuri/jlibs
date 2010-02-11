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

package jlibs.swing.outline;

import org.netbeans.swing.outline.RowModel;

/**
 * @author Santhosh Kumar T
 */
public class DefaultRowModel implements RowModel{
    private Column columns[];

    public DefaultRowModel(Column... columns){
        this.columns = columns;
    }

    @Override
    public int getColumnCount(){
        return columns.length;
    }

    @Override
    public Object getValueFor(Object obj, int i){
        return columns[i].getValueFor(obj);
    }

    @Override
    public Class getColumnClass(int i){
        return columns[i].getColumnClass();
    }

    @Override
    public boolean isCellEditable(Object obj, int i){
        return columns[i].isCellEditable(obj);
    }

    @Override
    public void setValueFor(Object obj, int i, Object value){
        columns[i].setValueFor(obj, value);
    }

    @Override
    public String getColumnName(int i){
        return columns[i].getColumnName();
    }
}
