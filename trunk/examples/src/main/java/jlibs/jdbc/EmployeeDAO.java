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
import java.sql.SQLException;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public abstract class EmployeeDAO extends DAO<Employee>{
    public EmployeeDAO(DataSource dataSource, String tableName, String[] columnNames, boolean[] primaries){
        super(dataSource, tableName, columnNames, primaries);
    }

    public List<Employee> findOlderEmployees(int id) throws SQLException{
        return all("where "+Employee.COL_ID+" > ?", id);
    }
}
