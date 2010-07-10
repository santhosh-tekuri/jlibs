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

package jlibs.jdbc.paging;

import jlibs.jdbc.DAO;

import java.util.ArrayList;

/**
 * @author Santhosh Kumar T
 */
public class Paging<T>{
    public final DAO<T> dao;
    public final String condition;
    public final Object args[];

    public Paging(DAO<T> dao, String condition, Object... args){
        this.dao = dao;
        this.condition = condition;
        this.args = args;
    }

    public int getTotalRowCount(){
        return dao.count(condition, args);
    }

    public final ArrayList<PagingColumn> orderBy = new ArrayList<PagingColumn>();
    public void addOrderBy(String propertyName, Order order){
        orderBy.add(new PagingColumn(dao.table.getColumnIndex(propertyName), order));
    }

    String orderBy(boolean reverse){
        StringBuilder buff = new StringBuilder();
        for(PagingColumn col: orderBy){
            Order order = col.order;
            if(reverse)
                order = order.reverse();
            if(buff.length()>0)
                buff.append(", ");
            buff.append(dao.table.columns[col.index].name).append(' ').append(order.keyword);
        }
        return "ORDER BY "+buff;
    }

    private String where(int index, boolean reverse) {
        StringBuffer buff = new StringBuffer("(");
        for(int i=0; i<index; ++i){
            buff.append(dao.table.columns[orderBy.get(i).index].name);
            buff.append("=? AND ");
        }

        PagingColumn col = orderBy.get(index);
        Order order = col.order;
        if(reverse)
            order = order.reverse();
        buff.append(dao.table.columns[col.index].name).append(order.operand).append('?');
        buff.append(")");
        return buff.toString();

    }

    String where(boolean reverse){
        StringBuffer buff = new StringBuffer();
        for(int i=0; i<orderBy.size(); i++){
            if(buff.length()>0)
                buff.append(" OR ");
            buff.append(where(i, reverse));
        }
        return buff.toString();
    }
}