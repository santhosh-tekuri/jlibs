/**
 * Copyright 2015 Santhosh Kumar Tekuri
 *
 * The JLibs authors license this file to you under the Apache License,
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
import java.sql.*;
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

    private String identifier(String identifier) throws SQLException{
        DatabaseMetaData metadata = con.getMetaData();
        if(!metadata.supportsMixedCaseIdentifiers()){
            if(metadata.storesUpperCaseIdentifiers())
                return identifier.toUpperCase();
            else if(metadata.storesLowerCaseIdentifiers())
                return identifier.toLowerCase();
        }
        return identifier;
    }

    public void validate(Columns columns) throws SQLException{
        try{
            ResultSet rs = con.getMetaData().getTables(null, null, identifier(columns.tableName), null);
            if(!rs.next())
                throw new AnnotationError(columns.tableClass, Table.class, "name", columns.tableName+" table doesn't exist in database");
            rs.close();

            List<String> columnNames = new ArrayList<String>();
            rs = con.getMetaData().getColumns(null, null, columns.tableName, null);
            while(rs.next())
                columnNames.add(rs.getString(4));

            for(ColumnProperty column: columns){
                columnNames.remove(column.columnName());
                rs = con.getMetaData().getColumns(null, null, identifier(columns.tableName), identifier(column.columnName()));
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
        }finally{
            con.close();
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
