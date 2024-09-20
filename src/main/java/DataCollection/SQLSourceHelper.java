package DataCollection;

import org.apache.flume.Context;
import org.apache.flume.conf.ConfigurationException;
import org.apache.kafka.common.config.ConfigException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SQLSourceHelper {
    private int runQueryDelay;  //两次查询的时间间隔
    private int startFrom;      //开始查询id
    private int currentIndex;   //当前的id
    private int recordSixe = 0;   //查询的条数
    private int maxRow;         //查询的最大条数
    private String table;       //查询的表
    private String columnsToSelect; //需要查询的列
    private String customQuery;  //用户传入的查询语句
    private String query;       //构建的查询语句
    private String defaultCharsetResultSet; //编码集
    private Context context;
    private static final int DEFAULT_QUERY_DELAY = 10000;
    private static final int DEFAULT_START_VALUE = 0;
    private static final int DEFAULT_MAX_ROWS = 2000;
    private static final String DEFAULT_COLUMNS_SELECT = "*";
    private static final String DEFAULT_CHARSET_RESULTSET = "UTF-8";
    private static Connection conn = null;
    private static PreparedStatement ps = null;
    private static String connectionURL;
    private static String connectionUserName;
    private static String connectionPassword;

    //获取jdbc连接
    private static Connection InitConnection(String url, String user, String pw) {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection connection = DriverManager.getConnection(url, user, pw);
            if (connection == null) {
                throw new SQLException();
            }
            return connection;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    SQLSourceHelper(Context context) {
        this.context = context;
        this.columnsToSelect = context.getString("columns.to.select", DEFAULT_COLUMNS_SELECT);
        this.runQueryDelay = context.getInteger("run.query.delay", DEFAULT_QUERY_DELAY);
        this.startFrom = context.getInteger("start.from", DEFAULT_START_VALUE);
        this.defaultCharsetResultSet = context.getString("default.charset.resultset", DEFAULT_CHARSET_RESULTSET);
        this.table = context.getString("table");
        this.customQuery = context.getString("custom.query");
        connectionURL = context.getString("connection.url");
        connectionUserName = context.getString("connection.user");
        connectionPassword = context.getString("connection.password");
        checkMandatoryProperties();
        conn = InitConnection(connectionURL, connectionUserName, connectionPassword);
        currentIndex = getStatusDBIndex(startFrom);
        query = buildQuery();
    }

    private String buildQuery() {
        String sql = "";
        currentIndex = getStatusDBIndex(startFrom);
        if (customQuery == null) {
            sql = "select " + columnsToSelect + " from " + table;
        } else {
            sql = customQuery;
        }
        StringBuffer execsql = new StringBuffer(sql);
        if (!sql.contains("where")) {
            execsql.append(" where");
            execsql.append(" em_id ").append(" > ").append(currentIndex);
            return execsql.toString();
        } else {
            int length = execsql.toString().length();
            return execsql.toString().substring(0, length - String.valueOf(currentIndex).length()) + currentIndex;
        }
    }

    private void checkMandatoryProperties() {
        if (table == null) { throw new ConfigurationException("table is not set"); }
        if (connectionURL == null) { throw new ConfigurationException("url is not set"); }
        if (connectionUserName == null) { throw new ConfigurationException("name is not set"); }
        if (connectionPassword == null) { throw new ConfigurationException("password is not set"); }
    }

    private Integer getStatusDBIndex(int startFrom) {
        String dbIndex = queryOne("select currentIndex from flume_meta where source_tab='" + table + "'");
        if (dbIndex != null) {
            return Integer.parseInt(dbIndex);
        }
        return startFrom;
    }

    private String queryOne(String sql) {
        ResultSet result = null;
        try {
            ps = conn.prepareStatement(sql);
            result = ps.executeQuery();
            while (result.next()) {
                return result.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    List<List<Object>> executeQuery() {
        try {
            customQuery = buildQuery();
            ArrayList<List<Object>> results = new ArrayList<List<Object>>();
            if (ps == null) {
                ps = conn.prepareStatement(customQuery);
            }
            ResultSet result = ps.executeQuery(customQuery);
            while (result.next()){
                ArrayList<Object> row = new ArrayList<Object>();
                for (int i = 1; i <= result.getMetaData().getColumnCount();i++){
                    row.add(result.getObject(1));
                }
                results.add(row);
            }
            return results;
        } catch (SQLException e) {
            conn = InitConnection(connectionURL, connectionUserName, connectionPassword);
        }
        return null;
    }

    List<String> getAllRows(List<List<Object>> result ){
        List<String> allRows = new ArrayList<String>();
        if (result == null || result.isEmpty()){
            return allRows;
        }
        StringBuilder row = new StringBuilder();
        for (List<Object> rawRow : result) {
            Object value = null;
            for (Object aRawRow : rawRow) {
                value = aRawRow;
                if (value == null){
                    row.append(",");
                }else {
                    row.append(aRawRow.toString()).append(",");
                }
            }
            allRows.add(row.toString());
            row = new StringBuilder();
        }
        return allRows;
    }

    void updateOffset2DB(int size){
        String sql = "update flume_meta set currentIndex='"
                + (recordSixe += size) + "' where source_tab= '"+ this.table + "'" ;
        execSql(sql);
    }

    private void execSql(String sql){
        try {
            ps = conn.prepareStatement(sql);
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    int getCurrentIndex() { return currentIndex; }
    void setCurrentIndex(int newValue) { currentIndex = newValue; }
    int getRunQueryDelay() { return runQueryDelay; }
    String getQuery() { return query; }
    String getConnectionURL() { return connectionURL; }
    private boolean isCustomQuerySet() { return (customQuery != null); }
    Context getContext() { return context; }
    public String getConnectionUserName() { return connectionUserName; }
    public String getConnectionPassword() { return connectionPassword; }
    String getDefaultCharsetResultSet() { return defaultCharsetResultSet; }

    //关闭相关资源
    void close() {
        try {
            ps.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
