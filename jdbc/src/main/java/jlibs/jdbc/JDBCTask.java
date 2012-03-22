package jlibs.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Santhosh Kumar T
 */
public interface JDBCTask<R>{
    public R run(Connection con) throws SQLException;
}
