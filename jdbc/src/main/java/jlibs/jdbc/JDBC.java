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
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class JDBC{
    public final DataSource dataSource;

    public JDBC(DataSource dataSource){
        this.dataSource = dataSource;
    }

    public <T> T processFirst(ResultSet rs, RowMapper<T> rowMapper) throws SQLException{
        try{
            if(rs.next())
                return rowMapper.newRecord(rs);
            else
                return null;
        }finally{
            rs.close();
        }
    }

    public <T> List<T> processAll(ResultSet rs, RowMapper<T> rowMapper) throws SQLException{
        List<T> result = new ArrayList<T>();
        try{
            while(rs.next())
                result.add(rowMapper.newRecord(rs));
            return result;
        }finally{
            rs.close();
        }
    }

    public <T> T selectFirst(final String query, final RowMapper<T> rowMapper, final Object... params) throws DAOException{
        return TransactionManager.run(dataSource, new SingleStatementTransaction<T>(){
            @Override
            public T run(Connection con) throws SQLException{
                System.out.println("SQL["+con.getAutoCommit()+"]: "+query);
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setMaxRows(1);
                setParams(stmt, params);
                try{
                    return processFirst(stmt.executeQuery(), rowMapper);
                }finally{
                    stmt.close();
                }
            }
        });
    }

    private void setParams(PreparedStatement stmt, Object[] params) throws SQLException{
        for(int i=0; i<params.length; i++){
            if(params[i] instanceof SQLType)
                stmt.setNull(i+1, ((SQLType)params[i]).type);
            else
                stmt.setObject(i+1, params[i]);
        }
    }

    public <T> List<T> selectTop(final String query, final int maxRows, final RowMapper<T> rowMapper, final Object... params) throws DAOException{
        return TransactionManager.run(dataSource, new SingleStatementTransaction<List<T>>(){
            @Override
            public List<T> run(Connection con) throws SQLException{
                System.out.println("SQL["+con.getAutoCommit()+"]: "+query);
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setMaxRows(maxRows);
                for(int i=0; i<params.length; i++)
                    stmt.setObject(i+1, params[i]);
                try{
                    return processAll(stmt.executeQuery(), rowMapper);
                }finally{
                    stmt.close();
                }
            }
        });
    }

    public <T> List<T> selectAll(final String query, final RowMapper<T> rowMapper, final Object... params) throws DAOException{
        return selectTop(query, 0, rowMapper, params);
    }

    public int executeUpdate(final String query, final Object... params) throws DAOException{
        return TransactionManager.run(dataSource, new SingleStatementTransaction<Integer>(){
            @Override
            public Integer run(Connection con) throws SQLException{
                System.out.println("SQL["+con.getAutoCommit()+"]: "+query);
                PreparedStatement stmt = null;
                try{
                    stmt = con.prepareStatement(query);
                    setParams(stmt, params);
                    return stmt.executeUpdate();
                }finally{
                    if(stmt!=null)
                        stmt.close();
                }
            }
        });
    }

    public <T> T executeUpdate(final String query, final RowMapper<T> generatedKeysMapper, final Object... params) throws DAOException{
        return TransactionManager.run(dataSource, new SingleStatementTransaction<T>(){
            @Override
            public T run(Connection con) throws SQLException{
                System.out.println("SQL["+con.getAutoCommit()+"]: "+query);
                PreparedStatement stmt = null;
                try{
                    stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                    setParams(stmt, params);
                    stmt.executeUpdate();
                    return processFirst(stmt.getGeneratedKeys(), generatedKeysMapper);
                }finally{
                    if(stmt!=null)
                        stmt.close();
                }
            }
        });
    }
}
