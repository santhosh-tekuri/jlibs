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

import java.math.BigDecimal;
import java.sql.*;

/**
 * @author Santhosh Kumar T
 */
public enum JavaType{
    STRING      (String.class,      SQLType.VARCHAR,    SQLType.LONGVARCHAR,    SQLType.CHAR    ),
    BIG_DECIMAL (BigDecimal.class,  SQLType.NUMERIC                                             ),
    BOOLEAN     (boolean.class,     SQLType.BIT                                                 ),
    BYTE        (byte.class,        SQLType.TINYINT                                             ),
    SHORT       (short.class,       SQLType.SMALLINT                                            ),
    INT         (int.class,         SQLType.INTEGER                                             ),
    LONG        (long.class,        SQLType.BIGINT                                              ),
    FLOAT       (float.class,       SQLType.REAL                                                ),
    DOUBLE      (double.class,      SQLType.DOUBLE                                              ),
    BINARY      (byte[].class,      SQLType.VARBINARY,  SQLType.LONGVARBINARY,  SQLType.BINARY  ),
    DATE        (Date.class,        SQLType.DATE                                                ),
    TIME        (Time.class,        SQLType.TIME                                                ),
    TIMESTAMP   (Timestamp.class,   SQLType.TIMESTAMP                                           ),
    CLOB        (Clob.class,        SQLType.CLOB                                                ),
    BLOB        (Blob.class,        SQLType.BLOB                                                );

    public final Class clazz;
    public final SQLType[] sqlTypes;
    JavaType(Class clazz, SQLType... sqlTypes){
        this.clazz = clazz;
        this.sqlTypes = sqlTypes;
    }

    public static JavaType valueOf(Class clazz){
        for(JavaType javaType : values()){
            if(javaType.clazz==clazz)
                return javaType;
        }
        return null;
    }
}
