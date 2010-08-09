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

package jlibs.jdbc.annotations.processor;

import jlibs.core.annotation.processing.AnnotationError;
import jlibs.core.lang.NotImplementedException;
import jlibs.core.lang.StringUtil;
import jlibs.core.lang.model.ModelUtil;
import jlibs.jdbc.JavaType;
import jlibs.jdbc.SQLType;
import jlibs.jdbc.annotations.Column;
import jlibs.jdbc.annotations.Database;
import jlibs.jdbc.annotations.Table;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ConnectionInfo{
    static Map<String, ConnectionInfo> ALL = new HashMap<String, ConnectionInfo>();

    Connection con;
    boolean failOnMissingColumns;

    ConnectionInfo(Connection con, boolean failOnMissingColumns){
        this.con = con;
        this.failOnMissingColumns = failOnMissingColumns;
    }

    public void validate(Columns columns) throws SQLException{
        ResultSet rs = con.getMetaData().getTables(null, null, columns.tableName, null);
        if(!rs.next())
            throw new AnnotationError(columns.tableClass, Table.class, "name", columns.tableName+" table doesn't exist in database");
        rs.close();

        List<String> columnNames = new ArrayList<String>();
        rs = con.getMetaData().getColumns(null, null, columns.tableName, null);
        while(rs.next())
            columnNames.add(rs.getString(4));

        for(ColumnProperty column: columns){
            columnNames.remove(column.columnName());
            rs = con.getMetaData().getColumns(null, null, columns.tableName, column.columnName());
            if(!rs.next())
                throw new AnnotationError(column.element, Column.class, "name", column.columnName()+" column doesn't exist in "+columns.tableName+" table");
            int type = rs.getInt(5);
            SQLType sqlType = SQLType.valueOf(type);
            if(sqlType==null)
                throw new NotImplementedException("SQLType is not defined for "+type);
            if(!JavaType.isCompatible(column.javaType(), sqlType)){
                Class suggested = JavaType.valueOf(sqlType).clazz;
                throw new AnnotationError(column.element, Column.class, "name", column.columnName()+" has incompatible java type. "+suggested.getName()+" is suggested");
            }
            rs.close();
        }

        if(columnNames.size()>0){
            AnnotationError error = new AnnotationError(columns.tableClass, "column properties are missing for columns " + StringUtil.join(columnNames.iterator(), ", "));
            if(failOnMissingColumns)
                throw error;
            else
                error.warn();
        }
    }

    public static void add(PackageElement pakage) throws Exception{
        TypeElement tableClass = (TypeElement)((DeclaredType)ModelUtil.getAnnotationValue(pakage, Database.class, "driver")).asElement();
        Class.forName(tableClass.getQualifiedName().toString());
        String url = ModelUtil.getAnnotationValue(pakage, Database.class, "url");
        String user = ModelUtil.getAnnotationValue(pakage, Database.class, "user");
        String password = ModelUtil.getAnnotationValue(pakage, Database.class, "password");
        Connection con;
        if(StringUtil.isEmpty(user) && StringUtil.isEmpty(password))
            con = DriverManager.getConnection(url);
        else
            con = DriverManager.getConnection(url, user, password);
        boolean failOnMissingColumns = (Boolean)ModelUtil.getAnnotationValue(pakage, Database.class, "failOnMissingColumns");
        ALL.put(ModelUtil.getPackage(pakage), new ConnectionInfo(con, failOnMissingColumns));
    }

    public static ConnectionInfo get(Columns columns){
        String pakage = ModelUtil.getPackage(columns.tableClass);
        ConnectionInfo info;
        while(true){
            info = ALL.get(pakage);
            if(info!=null)
                break;
            int dot = pakage.lastIndexOf('.');
            if(dot==-1)
                break;
            pakage = pakage.substring(0, dot);
        }
        return info;
    }
}
