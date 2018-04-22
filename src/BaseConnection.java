import java.sql.*;
import java.sql.ResultSet;

import org.h2.jdbcx.JdbcConnectionPool;

/**
 * Created by User on 15.04.2018.
 */
public class BaseConnection {

    private Connection conn;

    public BaseConnection() {
        try {
            JdbcConnectionPool cp = JdbcConnectionPool.create("jdbc:h2:file:D:\\Masters Degree\\bd", "sa", "");
            conn = cp.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void closeConnection() throws SQLException {
        conn.close();
    }

    public ResultSet dbData(String query) throws SQLException {
        Statement statement = conn.createStatement();
        ResultSet resultSet = statement.executeQuery(query);
        return resultSet;
    }


    public void dbDataQuery(String query) throws SQLException {
        Statement statement = conn.createStatement();
        statement.execute(query);
    }

    public Connection getConn() {
        return conn;
    }
}
