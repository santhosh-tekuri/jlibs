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

import jlibs.jdbc.annotations.Insert;
import jlibs.jdbc.annotations.Update;
import jlibs.jdbc.annotations.Delete;
import jlibs.jdbc.annotations.Upsert;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @author Santhosh Kumar T
 */
public abstract class EmployeeDAO extends DAO<Employee>{
    public EmployeeDAO(DataSource dataSource, String tableName, String[] columnNames, boolean[] primaries){
        super(dataSource, tableName, columnNames, primaries);
    }

    @Insert
    public abstract int insert(String firstName, String lastName) throws SQLException;

    @Insert
    public abstract int insert(String firstName, int age);
    
    @Update
    public abstract int update(int iD, int age, String where_firstName, String where_lastName);

    @Upsert
    public abstract int upsert1(int iD, int age, String where_firstName, String where_lastName);

    @Upsert
    public abstract void upsert2(int iD, int age, String where_firstName, String where_lastName);

    @Delete
    public abstract int delete(String firstName, String lastName) throws SQLException;

    @Delete
    public abstract int delete(String firstName, int age);
}
