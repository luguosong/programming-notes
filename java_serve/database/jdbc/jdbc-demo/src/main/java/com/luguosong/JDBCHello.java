package com.luguosong;

import java.sql.*;

/**
 * @author luguosong
 */
public class JDBCHello {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        //注册驱动
        Class.forName("com.mysql.cj.jdbc.Driver");

        //获取连接
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/learn_jdbc", "root", "12345678");

        // 获取Statement对象
        Statement statement = connection.createStatement();
        String user = "AA";
        String password = "123456";
        ResultSet resultSet = statement.executeQuery("SELECT * FROM user_table WHERE user = '" + user + "' AND password = '" + password + "'");

        //结果处理
        if (resultSet.next()) {
            System.out.println("用户名："+resultSet.getString("user"));
        } else {
            System.out.println("登录失败");
        }
    }
}
