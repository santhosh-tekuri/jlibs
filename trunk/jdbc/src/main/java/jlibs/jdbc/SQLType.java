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

import java.sql.Types;

/**
 * @author Santhosh Kumar T
 */
public enum SQLType{
	BIT             (Types.BIT),
	TINYINT         (Types.TINYINT),
	SMALLINT        (Types.SMALLINT),
	INTEGER         (Types.INTEGER),
	BIGINT          (Types.BIGINT),
	FLOAT           (Types.FLOAT),
	REAL            (Types.REAL),
	DOUBLE          (Types.DOUBLE),
	NUMERIC         (Types.NUMERIC),
	DECIMAL         (Types.DECIMAL),
	CHAR            (Types.CHAR),
	VARCHAR         (Types.VARCHAR),
	LONGVARCHAR     (Types.LONGNVARCHAR),
	DATE            (Types.DATE),
	TIME            (Types.TIME),
	TIMESTAMP       (Types.TIMESTAMP),
	BINARY          (Types.BINARY),
	VARBINARY       (Types.VARBINARY),
	LONGVARBINARY   (Types.LONGVARBINARY),
    BLOB            (Types.BLOB),
    CLOB            (Types.CLOB),
    BOOLEAN         (Types.BOOLEAN),
    OTHER           (Types.OTHER)

//	NULL		=   0;
//	OTHER		= 1111;
//    JAVA_OBJECT         = 2000;
//    DISTINCT            = 2001;
//    STRUCT              = 2002;
//    ARRAY               = 2003;
//    REF                 = 2006;
//    DATALINK = 70;
//    ROWID = -8;
//    NCHAR = -15;
//    NVARCHAR = -9;
//    LONGNVARCHAR = -16;
//    NCLOB = 2011;
//    SQLXML = 2009;
    ;
    public final int type;

    SQLType(int type){
        this.type = type;
    }

    public static SQLType valueOf(int type){
        for(SQLType value: values()){
            if(value.type==type)
                return value;
        }
        return null;
    }
}
