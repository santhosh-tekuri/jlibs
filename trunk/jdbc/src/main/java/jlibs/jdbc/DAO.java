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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public abstract class DAO<T>{
    public final DataSource dataSource;
    public final TableMetaData table;

    public DAO(DataSource dataSource, TableMetaData table){
        this.dataSource = dataSource;
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
    
    private T newRecord(ResultSet rs) throws SQLException{
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
        int i = 0;
        for(; i<table.columns.length; i++){
            if(i>0)
                query.append(',');
            query.append(table.columns[i].name);
        }
        query.append(") values(");
        for(int j=0; j<i; j++){
            if(j>0)
                query.append(',');
            query.append('?');
        }
        query.append(')');
        insertQuery = query.toString();

        // UPDATE Query
        query.setLength(0);
        query.append("set ");
        List<Integer> args = new ArrayList<Integer>();
        for(i=0; i<table.columns.length; i++){
            if(!table.columns[i].primary){
                if(args.size()>0)
                    query.append(", ");
                query.append(table.columns[i].name).append("=?");
                args.add(i);
            }
        }
        query.append(" where ");
        int size = args.size();
        for(i=0; i<table.columns.length; i++){
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
        for(i=0; i<table.columns.length; i++){
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
    
    /*-------------------------------------------------[ JDBC ]---------------------------------------------------*/
    
    private T selectFirst(final String query, final Object... params) throws SQLException{
        return TransactionManager.run(dataSource, new SingleStatementTransaction<T>(){
            @Override
            public T run(Connection con) throws SQLException{
                System.out.println("SQL["+con.getAutoCommit()+"]: "+query);
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setMaxRows(1);
                for(int i=0; i<params.length; i++)
                    stmt.setObject(i+1, params[i]);

                ResultSet rs = stmt.executeQuery();
                try{
                    if(rs.next())
                        return newRecord(rs);
                    else
                        return null;
                }finally{
                    stmt.close();
                }
            }
        });
    }

    private List<T> selectAll(final String query, final Object... params) throws SQLException{
        return TransactionManager.run(dataSource, new SingleStatementTransaction<List<T>>(){
            @Override
            public List<T> run(Connection con) throws SQLException{
                System.out.println("SQL["+con.getAutoCommit()+"]: "+query);
                PreparedStatement stmt = con.prepareStatement(query);
                for(int i=0; i<params.length; i++)
                    stmt.setObject(i+1, params[i]);

                ResultSet rs = stmt.executeQuery();
                List<T> result = new ArrayList<T>();
                try{
                    while(rs.next())
                        result.add(newRecord(rs));
                    return result;
                }finally{
                    stmt.close();
                }
            }
        });
    }

    private int executeUpdate(final String query, final Object... params) throws SQLException{
        return TransactionManager.run(dataSource, new SingleStatementTransaction<Integer>(){
            @Override
            public Integer run(Connection con) throws SQLException{
                System.out.println("SQL["+con.getAutoCommit()+"]: "+query);
                PreparedStatement stmt = con.prepareStatement(query);
                try{
                    for(int i=0; i<params.length; i++)
                        stmt.setObject(i+1, params[i]);
                    return stmt.executeUpdate();
                }finally{
                    stmt.close();
                }
            }
        });
    }

    /*-------------------------------------------------[ Select ]---------------------------------------------------*/
    
    private String selectQuery(String condition){
        return condition==null ? selectQuery : selectQuery+condition;
    }

    public List<T> all() throws SQLException{
        return all(null);
    }
    
    public List<T> all(String condition, Object... args) throws SQLException{
        return selectAll(selectQuery(condition), args);
    }

    public T first() throws SQLException{
        return first(null);
    }

    public T first(String condition, Object... args) throws SQLException{
        return selectFirst(selectQuery(condition), args);
    }
    
    /*-------------------------------------------------[ Insert ]---------------------------------------------------*/
    
    public int insert(String query, Object... args) throws SQLException{
        if(query==null)
            query = "";        
        return executeUpdate("insert into "+table.name+" "+query, args);
    }
    
    public int insert(T record) throws SQLException{
        Object args[] = new Object[table.columns.length];
        for(int i=0; i<table.columns.length; i++)
            args[i] = getColumnValue(i, record);
        return insert(insertQuery, args);
    }

    /*-------------------------------------------------[ Update ]---------------------------------------------------*/
    
    public int update(String query, Object... args) throws SQLException{
        if(query==null)
            query = "";        
        return executeUpdate("update "+table.name+" "+query, args);
    }

    public int update(T record) throws SQLException{
        return update(updateQuery, values(record, updateOrder));
    }

    /*-------------------------------------------------[ Upsert ]---------------------------------------------------*/
    
    public int upsert(T record) throws SQLException{
        int count = update(record);
        return count==0 ? insert(record) : count;
    }

    /*-------------------------------------------------[ Delete ]---------------------------------------------------*/

    public int delete(String query, Object... args) throws SQLException{
        if(query==null)
            query = "";
        return executeUpdate("delete from "+table.name+" "+query, args);
    }

    public int delete() throws SQLException{
        return delete(null, new Object[0]);
    }

    public int delete(T record) throws SQLException{
        return delete(deleteQuery, values(record, deleteOrder));
    }
}
