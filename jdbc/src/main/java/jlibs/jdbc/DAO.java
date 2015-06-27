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

package jlibs.jdbc;

import jlibs.core.util.CollectionUtil;

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

    public DAO(JDBC jdbc, TableMetaData table){
        this.jdbc = jdbc;
        this.table = table;
        buildQueries();
    }

    @SuppressWarnings({"unchecked"})
    public static <T> DAO<T> create(Class<T> clazz, JDBC jdbc){
        try{
            String qname = "${package}._${class}DAO".replace("${package}", clazz.getPackage()!=null?clazz.getPackage().getName():"")
                    .replace("${class}", clazz.getSimpleName());
            if(qname.startsWith(".")) // default package
                qname = qname.substring(1);
            Class tableClass = clazz.getClassLoader().loadClass(qname);
            return (DAO<T>)tableClass.getConstructor(JDBC.class).newInstance(jdbc);
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
    public abstract T newRecord(ResultSet rs) throws SQLException;
    public abstract Object getAutoColumnValue(ResultSet rs) throws SQLException;

    /*-------------------------------------------------[ Helpers ]---------------------------------------------------*/

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
        StringBuilder query = new StringBuilder("SELECT ");
        for(int i=0; i<table.columns.length; i++){
            if(i>0)
                query.append(',');
            query.append(table.columns[i].name);
        }
        query.append(" FROM ").append(table.name).append(" ");
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
        query.append(") VALUES(");
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
        args.clear();
        for(int i=0; i<table.columns.length; i++){
            if(!table.columns[i].primary){
                if(args.size()==0)
                    query.append(" SET ");
                else
                    query.append(", ");
                query.append(table.columns[i].name).append("=?");
                args.add(i);
            }
        }
        int size = args.size();
        for(int i=0; i<table.columns.length; i++){
            if(table.columns[i].primary){
                if(args.size()>size)
                    query.append(" AND ");
                else
                    query.append(" WHERE ");
                query.append(table.columns[i].name).append("=?");
                args.add(i);
            }
        }
        updateQuery = query.toString();
        updateOrder = CollectionUtil.toIntArray(args);

        // DELETE Query
        query.setLength(0);
        args.clear();
        for(int i=0; i<table.columns.length; i++){
            if(table.columns[i].primary){
                if(args.size()==0)
                    query.append(" WHERE ");
                else
                    query.append(" AND ");
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

    public List<T> all() throws DAOException{
        return all(null);
    }

    public List<T> all(String condition, Object... args) throws DAOException{
        return top(0, condition, args);
    }

    public List<T> top(int max, String condition, Object... args) throws DAOException{
        return jdbc.selectTop(max, selectQuery(condition), this, args);
    }

    public T first() throws DAOException{
        return first(null);
    }

    public T first(String condition, Object... args) throws DAOException{
        return jdbc.selectFirst(selectQuery(condition), this, args);
    }

    /*-------------------------------------------------[ Count ]---------------------------------------------------*/

    protected int integer(String functionCall, String condition, Object... args) throws DAOException{
        if(condition==null)
            condition = "";

        return jdbc.selectFirst("SELECT "+functionCall+" FROM "+table.name+' '+condition, new RowMapper<Integer>(){
            @Override
            public Integer newRecord(ResultSet rs) throws SQLException{
                return rs.getInt(1);
            }
        }, args);
    }

    public int count(String condition, Object... args) throws DAOException{
        return integer("count(*)", condition, args);
    }

    /*-------------------------------------------------[ Insert ]---------------------------------------------------*/

    private final RowMapper<Object> generaedKeyMapper = new RowMapper<Object>(){
        @Override
        public Object newRecord(ResultSet rs) throws SQLException{
            return getAutoColumnValue(rs);
        }
    };

    public Object insert(String query, Object... args) throws DAOException{
        if(query==null)
            query = "";
        if(table.autoColumn==-1){
            jdbc.executeUpdate("INSERT INTO "+table.name+" "+query, args);
            return null;
        }else
            return jdbc.executeUpdate("INSERT INTO "+table.name+" "+query, generaedKeyMapper, args);
    }

    public void insert(T record) throws DAOException{
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

    public int update(String query, Object... args) throws DAOException{
        if(query==null)
            query = "";
        return jdbc.executeUpdate("UPDATE "+table.name+" "+query, args);
    }

    public int update(T record) throws DAOException{
        return update(updateQuery, values(record, updateOrder));
    }

    /*-------------------------------------------------[ Upsert ]---------------------------------------------------*/

    public void upsert(T record) throws DAOException{
        if(update(record)==0)
            insert(record);
    }

    /*-------------------------------------------------[ Delete ]---------------------------------------------------*/

    public int delete(String query, Object... args) throws DAOException{
        if(query==null)
            query = "";
        return jdbc.executeUpdate("DELETE FROM "+table.name+" "+query, args);
    }

    public int delete() throws DAOException{
        return delete(null, new Object[0]);
    }

    public boolean delete(T record) throws DAOException{
        return delete(deleteQuery, values(record, deleteOrder))==1;
    }
}
