package com.luguosong;

import java.sql.*;

/**
 * @author luguosong
 */
public class JDBCHello {
    public static void main(String[] args) throws SQLException {
        /*
         * 连接到数据源
         * */
        Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/learn_jdbc",
                "root",
                "12345678");

        /*
         * 向数据库发送查询和更新语句
         * */
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM user_table");

        /*
         * 检索和处理从数据库收到的查询结果
         * */
        while (rs.next()) {
            String user = rs.getString("user");
            String password = rs.getString("password");
            System.out.println("user: " + user + ", password: " + password);
        }

        rs.close();
        stmt.close();
        con.close();
    }
}
