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

package jlibs.jdbc;

import jlibs.core.lang.model.ModelUtil;
import jlibs.core.util.CollectionUtil;
import jlibs.jdbc.annotations.processor.TableAnnotationProcessor;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public abstract class DAO<T> implements RowMapper<T>{
    public final JDBC jdbc;
    public final TableMetaData table;

    public DAO(DataSource dataSource, TableMetaData table){
        jdbc = new JDBC(dataSource);
        this.table = table;
        buildQueries();
    }

    @SuppressWarnings({"unchecked"})
    public static <T> DAO<T> create(Class<T> clazz, DataSource dataSource){
        try{
            Class tableClass = ModelUtil.findClass(clazz, TableAnnotationProcessor.FORMAT);
            return (DAO<T>)tableClass.getConstructor(DataSource.class).newInstance(dataSource);
        }catch(ClassNotFoundException ex){
            throw new RuntimeException(ex);
        } catch(InstantiationException ex){
            throw new RuntimeException(ex);
        } catch(IllegalAccessException ex){
            throw new RuntimeException(ex);
        } catch(NoSuchMethodException e){
            throw new RuntimeException(e);
        } catch(InvocationTargetException e){
            throw new RuntimeException(e);
        }
    }

    /*-------------------------------------------------[ Abstract Methods ]---------------------------------------------------*/
    
    public abstract T newRow();
    public abstract Object getColumnValue(int i, T record);
    public abstract void setColumnValue(int i, T record, Object value);

    /*-------------------------------------------------[ Helpers ]---------------------------------------------------*/
    
    @Override
    public T newRecord(ResultSet rs) throws SQLException{
        T record = newRow();
        for(int i=0; i<table.columns.length; i++)
            setColumnValue(i, record, rs.getObject(i+1));
        return record;
    }

    private Object[] values(T record, int order[]){
        Object args[] = new Object[order.length];
        for(int i=0; i<args.length; i++)
            args[i] = getColumnValue(order[i], record);
        return args;
    }

    /*-------------------------------------------------[ Queries ]---------------------------------------------------*/

    private String selectQuery;
    private String insertQuery;
    private int insertOrder[];
    private String updateQuery;
    private int updateOrder[];
    private String deleteQuery;
    private int deleteOrder[];

    private void buildQueries(){
        // SELECT Query
        StringBuilder query = new StringBuilder("select ");
        for(int i=0; i<table.columns.length; i++){
            if(i>0)
                query.append(',');
            query.append(table.columns[i].name);
        }
        query.append(" from ").append(table.name).append(" ");
        selectQuery = query.toString();
        
        // INSERT Query
        query.setLength(0);
        query.append('(');
        boolean first = true;
        for(ColumnMetaData column: table.columns){
            if(!column.auto){
                if(first)
                    first = false;
                else
                    query.append(',');
                query.append(column.name);
            }
        }
        query.append(") values(");
        first = true;
        List<Integer> args = new ArrayList<Integer>();
        for(int i=0; i<table.columns.length; i++){
            if(!table.columns[i].auto){
                if(first)
                    first = false;
                else
                    query.append(',');
                query.append('?');
                args.add(i);
            }
        }
        query.append(')');
        insertQuery = query.toString();
        if(table.autoColumn!=-1)
            insertOrder = CollectionUtil.toIntArray(args);

        // UPDATE Query
        query.setLength(0);
        query.append("set ");
        args.clear();
        for(int i=0; i<table.columns.length; i++){
            if(!table.columns[i].primary){
                if(args.size()>0)
                    query.append(", ");
                query.append(table.columns[i].name).append("=?");
                args.add(i);
            }
        }
        query.append(" where ");
        int size = args.size();
        for(int i=0; i<table.columns.length; i++){
            if(table.columns[i].primary){
                if(args.size()>size)
                    query.append(" and ");
                query.append(table.columns[i].name).append("=?");
                args.add(i);
            }
        }
        updateQuery = query.toString();
        updateOrder = CollectionUtil.toIntArray(args);

        // DELETE Query
        query.setLength(0);
        query.append("where ");
        args.clear();
        for(int i=0; i<table.columns.length; i++){
            if(table.columns[i].primary){
                if(args.size()>0)
                    query.append(" and ");
                query.append(table.columns[i].name).append("=?");
                args.add(i);
            }
        }
        deleteQuery = query.toString();
        deleteOrder = CollectionUtil.toIntArray(args);
    }
    
    /*-------------------------------------------------[ Select ]---------------------------------------------------*/
    
    private String selectQuery(String condition){
        return condition==null ? selectQuery : selectQuery+condition;
    }

    public List<T> all() throws SQLException{
        return all(null);
    }
    
    public List<T> all(String condition, Object... args) throws SQLException{
        return jdbc.selectAll(selectQuery(condition), this, args);
    }

    public T first() throws SQLException{
        return first(null);
    }

    public T first(String condition, Object... args) throws SQLException{
        return jdbc.selectFirst(selectQuery(condition), this, args);
    }
    
    /*-------------------------------------------------[ Count ]---------------------------------------------------*/

    protected int integer(String functionCall, String condition, Object... args) throws SQLException{
        if(condition==null)
            condition = "";

        return jdbc.selectFirst("select "+functionCall+" from "+table.name+' '+condition, new RowMapper<Integer>(){
            @Override
            public Integer newRecord(ResultSet rs) throws SQLException{
                return rs.getInt(1);
            }
        }, args);
    }

    public int count(String condition, Object... args) throws SQLException{
        return integer("count(*)", condition, args);
    }

    /*-------------------------------------------------[ Insert ]---------------------------------------------------*/
    
    private static final RowMapper<Object> generaedKeyMapper = new RowMapper<Object>(){
        @Override
        public Object newRecord(ResultSet rs) throws SQLException{
            return rs.getObject(1);
        }
    };

    public Object insert(String query, Object... args) throws SQLException{
        if(query==null)
            query = "";
        if(table.autoColumn==-1){
            jdbc.executeUpdate("insert into "+table.name+" "+query, args);
            return null;
        }else
            return jdbc.executeUpdate("insert into "+table.name+" "+query, generaedKeyMapper, args);
    }
    
    public void insert(T record) throws SQLException{
        if(table.autoColumn==-1){
            Object args[] = new Object[table.columns.length];
            for(int i=0; i<table.columns.length; i++)
                args[i] = getColumnValue(i, record);
            insert(insertQuery, args);
        }else{
            Object generatedKey = insert(insertQuery, values(record, insertOrder));
            setColumnValue(table.autoColumn, record, generatedKey);
        }
    }

    /*-------------------------------------------------[ Update ]---------------------------------------------------*/
    
    public int update(String query, Object... args) throws SQLException{
        if(query==null)
            query = "";
        return jdbc.executeUpdate("update "+table.name+" "+query, args);
    }

    public int update(T record) throws SQLException{
        return update(updateQuery, values(record, updateOrder));
    }

    /*-------------------------------------------------[ Upsert ]---------------------------------------------------*/
    
    public void upsert(T record) throws SQLException{
        if(update(record)==0)
            insert(record);
    }

    /*-------------------------------------------------[ Delete ]---------------------------------------------------*/

    public int delete(String query, Object... args) throws SQLException{
        if(query==null)
            query = "";
        return jdbc.executeUpdate("delete from "+table.name+" "+query, args);
    }

    public int delete() throws SQLException{
        return delete(null, new Object[0]);
    }

    public int delete(T record) throws SQLException{
        return delete(deleteQuery, values(record, deleteOrder));
    }
}
