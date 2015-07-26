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
