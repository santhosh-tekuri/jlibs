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

import jlibs.core.lang.ArrayUtil;

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
    BLOB        (Blob.class,        SQLType.BLOB                                                ),
    OTHER       (Object.class,      SQLType.OTHER                                               );

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

    public static JavaType valueOf(SQLType sqlType){
        for(JavaType value: values()){
            if(ArrayUtil.contains(value.sqlTypes, sqlType))
                return value;
        }
        return null;
    }

    private static JavaType compatible[][]={
        { BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, BIG_DECIMAL },
        { FLOAT, DOUBLE, BIG_DECIMAL },
        { TIME, DATE },
        { TIMESTAMP, DATE },
    };

    public static boolean isCompatible(JavaType javaType, SQLType sqlType){
        if(ArrayUtil.contains(javaType.sqlTypes, sqlType))
            return true;

        JavaType candidate = valueOf(sqlType);
        for(JavaType javaTypes[]: compatible){
            int index = ArrayUtil.indexOf(javaTypes, candidate);
            if(index!=-1){
                for(;index<javaTypes.length; index++){
                    if(javaTypes[index]==javaType)
                        return true;
                }
            }
        }
        return false;
    }
}
