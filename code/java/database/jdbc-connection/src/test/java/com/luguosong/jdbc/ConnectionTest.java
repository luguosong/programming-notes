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
class   ConnectionTest {

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

    // --8<-- [start:driver_register_way1]
    /**
     * 驱动注册方式一：显式调用 DriverManager.registerDriver()
     * 缺点：会导致驱动注册两次（驱动本身的静态代码块也会注册一次）
     */
    @Test
    void testRegisterDriverWay1() throws Exception {
        // 显式创建驱动对象并注册
        // 注意：这种方式会造成驱动被注册两次，一般不推荐
        DriverManager.registerDriver(new org.h2.Driver());
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            assertNotNull(conn, "方式一：显式注册驱动后应能获取连接");
            System.out.println("方式一（registerDriver）连接成功: " + conn.isValid(2));
        }
    }
    // --8<-- [end:driver_register_way1]

    // --8<-- [start:driver_register_way2]
    /**
     * 驱动注册方式二：使用 Class.forName() 反射加载驱动类
     * 触发驱动类的静态代码块，静态代码块内部调用 DriverManager.registerDriver()
     * 优点：驱动类名可写在配置文件中，解耦实现
     */
    @Test
    void testRegisterDriverWay2() throws Exception {
        // 反射加载驱动类，触发静态初始化，驱动自动注册
        Class.forName("org.h2.Driver");
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            assertNotNull(conn, "方式二：Class.forName 注册驱动后应能获取连接");
            System.out.println("方式二（Class.forName）连接成功: " + conn.isValid(2));
        }
    }
    // --8<-- [end:driver_register_way2]

    // --8<-- [start:driver_register_way3]
    /**
     * 驱动注册方式三：JDBC 4.0+ SPI 自动发现（Java 6 起）
     * 无需任何显式注册代码，JVM 启动时自动扫描 classpath 中
     * 所有 JAR 包的 META-INF/services/java.sql.Driver 文件并注册声明的驱动
     */
    @Test
    void testRegisterDriverWay3() throws Exception {
        // JDBC 4.0+ 无需手动注册，直接获取连接即可
        // JVM 已通过 SPI 机制自动发现并注册了 org.h2.Driver
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            assertNotNull(conn, "方式三：SPI 自动注册驱动后应能获取连接");
            // 验证驱动已被自动注册：DriverManager 中有驱动信息
            java.util.Enumeration<java.sql.Driver> drivers = DriverManager.getDrivers();
            boolean h2Found = false;
            while (drivers.hasMoreElements()) {
                java.sql.Driver driver = drivers.nextElement();
                if (driver.getClass().getName().contains("h2")) {
                    h2Found = true;
                    break;
                }
            }
            assertTrue(h2Found, "H2 驱动应已被 SPI 自动注册");
            System.out.println("方式三（SPI 自动）连接成功: " + conn.isValid(2));
        }
    }
    // --8<-- [end:driver_register_way3]
}
