package com.luguosong.jdbc;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 演示建立数据库连接的各种方式
 */
class ConnectionTest {

    private static final String URL = "jdbc:h2:mem:testdb_conn;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    // --8<-- [start:driver_manager_connect]
    /**
     * 使用 DriverManager.getConnection(url, user, password) 三参数方式建立连接
     */
    @Test
    void testDriverManagerConnect() throws SQLException {
        // 通过 DriverManager 获取数据库连接
        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        assertNotNull(conn, "连接对象不应为 null");
        // 验证连接是否有效（超时时间 2 秒）
        assertTrue(conn.isValid(2), "连接应当有效");
        System.out.println("连接是否有效: " + conn.isValid(2));
        System.out.println("连接类: " + conn.getClass().getName());
        conn.close();
    }
    // --8<-- [end:driver_manager_connect]

    // --8<-- [start:connection_url_only]
    /**
     * 使用 DriverManager.getConnection(url) 单参数版本（H2 无密码场景）
     */
    @Test
    void testConnectionUrlOnly() throws SQLException {
        // 单参数版本：将用户名密码信息编码在 URL 中
        // H2 支持在 URL 后追加 ;USER=sa;PASSWORD= 的形式
        String urlWithAuth = URL + ";USER=sa;PASSWORD=";
        Connection conn = DriverManager.getConnection(urlWithAuth);
        assertNotNull(conn, "单参数连接不应为 null");
        assertTrue(conn.isValid(2), "单参数连接应当有效");
        System.out.println("单参数连接成功: " + conn);
        conn.close();
    }
    // --8<-- [end:connection_url_only]

    // --8<-- [start:try_with_resources]
    /**
     * 演示 try-with-resources 自动关闭连接
     * 确认连接在离开 try 块后自动关闭
     */
    @Test
    void testTryWithResources() throws SQLException {
        Connection outerRef;
        // try-with-resources 自动调用 close()，无需手动关闭
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            outerRef = conn;
            assertFalse(conn.isClosed(), "try 块内连接应处于打开状态");
            System.out.println("try 块内连接已关闭: " + conn.isClosed());
        }
        // 离开 try 块后连接自动关闭
        assertTrue(outerRef.isClosed(), "离开 try 块后连接应已关闭");
        System.out.println("离开 try 块后连接已关闭: " + outerRef.isClosed());
    }
    // --8<-- [end:try_with_resources]

    // --8<-- [start:connection_properties]
    /**
     * 使用 Properties 对象传入 user/password 建立连接
     */
    @Test
    void testConnectionProperties() throws SQLException {
        // 通过 Properties 对象设置连接参数
        Properties props = new Properties();
        props.setProperty("user", USER);
        props.setProperty("password", PASSWORD);

        Connection conn = DriverManager.getConnection(URL, props);
        assertNotNull(conn, "Properties 方式连接不应为 null");
        assertTrue(conn.isValid(2), "Properties 方式连接应当有效");
        System.out.println("Properties 方式连接成功: " + conn);
        conn.close();
    }
    // --8<-- [end:connection_properties]
}
