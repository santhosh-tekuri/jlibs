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
