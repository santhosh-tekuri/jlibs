/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package jlibs.jdbc.paging;

import jlibs.jdbc.DAO;
import jlibs.jdbc.Order;

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

    public Page<T> createPage(int pageSize){
        return new Page<T>(this, pageSize);
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