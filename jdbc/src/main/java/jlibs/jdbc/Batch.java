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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Santhosh Kumar T
 */
class Batch{
    PreparedStatement pstmt;
    String query;

    int flushInterval;
    int count;

    Batch(int flushInterval){
        this.flushInterval = flushInterval;
    }

    PreparedStatement prepareStatement(Connection con, String query) throws SQLException {
        if(pstmt!=null){
            if(query.equals(this.query))
                return pstmt;
            else{
                try{
                    pstmt.executeBatch();
                }finally{
                    pstmt.close();
                }
            }
        }
        pstmt = con.prepareStatement(query);
        this.query = query;
        count = 0;
        return pstmt;
    }

    int executeUpdate() throws SQLException{
        pstmt.addBatch();
        count++;
        if(count>=flushInterval){
            pstmt.executeBatch();
            count = 0;
        }
        return 0;
    }
    
    void finish(boolean commit) throws SQLException{
        if(pstmt!=null){
            try{
                if(commit && count>0)
                    pstmt.executeBatch();
            }finally{
                pstmt.close();
            }
        }
    }

    void rollback() throws SQLException{
        if(pstmt!=null)
            pstmt.close();
    }
}
