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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class TransactionManager extends ThreadLocal<Map<DataSource, Connection>>{
    private static final boolean debug = Boolean.getBoolean("jlibs.jdbc.debug");

    private static final TransactionManager INSTANCE = new TransactionManager();
    private TransactionManager(){}

    @Override
    protected Map<DataSource, Connection> initialValue(){
        return new IdentityHashMap<DataSource, Connection>();
    }

    private static Connection find(DataSource ds){
        Map<DataSource, Connection> transactions = INSTANCE.get();
        return transactions.get(ds);
    }

    private static Connection start(DataSource ds) throws SQLException{
        Map<DataSource, Connection> transactions = INSTANCE.get();
        Connection con = ds.getConnection();
        try{
            con.setAutoCommit(false);
            if(transactions.put(ds, con)!=null)
                throw new IllegalStateException("transaction is already in progress");
        }catch(SQLException ex){
            try{
                con.close();
            }catch(SQLException ignore){
                ignore.printStackTrace();
            }
            throw new SQLException(ex);
        } catch(IllegalStateException ex){
            try{
                con.close();
            }catch(SQLException ignore){
                ignore.printStackTrace();
            }
            throw ex;
        }
        return con;
    }

    private static Connection get(DataSource ds) throws SQLException{
        Map<DataSource, Connection> transactions = INSTANCE.get();
        Connection con = transactions.get(ds);
        return con!=null ? con : start(ds);
    }

    private static void commit(DataSource ds) throws SQLException{
        Map<DataSource, Connection> transactions = INSTANCE.get();
        Connection con = transactions.remove(ds);
        con.commit();
        con.setAutoCommit(true);
        if(debug)
            System.out.println("committed");
    }

    private static void rollback(DataSource ds) throws SQLException{
        Map<DataSource, Connection> transactions = INSTANCE.get();
        Connection con = transactions.remove(ds);
        con.rollback();
        con.setAutoCommit(true);
        if(debug)
            System.out.println("rolledback");
    }

    public static <R> R run(DataSource ds, Transaction<R> transaction) throws DAOException{
        boolean single = transaction instanceof SingleStatementTransaction;
        
        Connection con = find(ds);
        boolean noTransaction = con==null;
        if(con==null){
            try{
                con = single ? ds.getConnection() : start(ds);
                if(debug)
                    System.out.println(single ? "newConnection" : "newTransaction");
            }catch(SQLException ex){
                throw new DAOException(ex);
            }
        }

        Exception ex = null;
        R result = null;
        try{
            result = transaction.run(con);
        }catch(Exception e){
            ex = e;
        }finally{
            if(noTransaction){
                if(!single){
                    try{
                        Map<DataSource, Batch> batches = BatchManager.INSTANCE.get();
                        Batch batch = batches.remove(ds);
                        if(batch!=null){
                            if(ex==null)
                                batch.commit();
                            else
                                batch.rollback();
                        }
                    }catch(SQLException e){
                        if(ex==null)
                            ex = e;
                        else
                            ex.printStackTrace();
                    }
                    try{
                        if(ex==null)
                            commit(ds);
                        else
                            rollback(ds);
                    }catch(SQLException e){
                        if(ex==null)
                            ex = e;
                        else
                            ex.printStackTrace();
                    }
                }
                try{
                    con.close();
                    if(debug)
                        System.out.println("closed");
                }catch(SQLException e){
                    if(ex==null)
                        ex = e;
                    else
                        ex.printStackTrace();
                }
            }
        }

        if(ex==null)
            return result;
        else{
            if(ex instanceof SQLException)
                throw new DAOException(ex);
            else
                throw (RuntimeException)ex;
        }
    }
}
