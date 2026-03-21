package com.luguosong.jdbc;

import com.alibaba.druid.pool.DruidDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 演示 HikariCP 连接池
 */
class ConnectionPoolTest {

    private static final String USER = "sa";
    private static final String PASSWORD = "";

    // --8<-- [start:hikari_basic_config]
    /**
     * 演示创建 HikariConfig + HikariDataSource，配置连接池参数
     */
    @Test
    void testHikariBasicConfig() throws SQLException {
        String url = "jdbc:h2:mem:testdb_pool_basic;DB_CLOSE_DELAY=-1";
        // 创建 HikariCP 配置
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(USER);
        config.setPassword(PASSWORD);
        // 连接池核心参数
        config.setMaximumPoolSize(10);  // 最大连接数
        config.setMinimumIdle(2);       // 最小空闲连接数
        config.setConnectionTimeout(30000); // 连接超时（毫秒）
        config.setIdleTimeout(600000);      // 空闲连接超时（毫秒）

        // 通过配置创建数据源（连接池）
        try (HikariDataSource dataSource = new HikariDataSource(config)) {
            System.out.println("HikariCP 连接池已创建");
            System.out.println("最大连接数: " + config.getMaximumPoolSize());
            System.out.println("最小空闲: " + config.getMinimumIdle());

            // 从连接池获取连接并执行查询
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {

                assertNotNull(conn, "从连接池获取的连接不应为 null");
                assertTrue(conn.isValid(2), "连接应有效");

                // 执行简单查询验证连接可用
                ResultSet rs = stmt.executeQuery("SELECT 1 AS result");
                assertTrue(rs.next());
                assertEquals(1, rs.getInt("result"), "查询结果应为 1");
                System.out.println("连接池连接测试成功，查询结果: " + rs.getInt("result"));
            }
        }
    }
    // --8<-- [end:hikari_basic_config]

    // --8<-- [start:pool_vs_direct]
    /**
     * 对比直接 DriverManager 和连接池的使用方式
     */
    @Test
    void testPoolVsDirect() throws SQLException {
        String url = "jdbc:h2:mem:testdb_pool_vs;DB_CLOSE_DELAY=-1";

        // === 方式1：DriverManager 直接获取连接 ===
        // 每次都创建新的物理连接，开销大
        // 连接关闭后不可复用，需要重新创建
        try (Connection directConn = DriverManager.getConnection(url, USER, PASSWORD)) {
            assertNotNull(directConn);
            System.out.println("DriverManager 直连: " + directConn.getClass().getName());
            // 注意：close() 后连接被销毁，无法复用
        }

        // === 方式2：HikariCP 连接池 ===
        // 预先创建连接放入池中，获取时直接从池中取出
        // 归还时连接不会销毁，而是放回池中等待复用
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(USER);
        config.setPassword(PASSWORD);
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);

        try (HikariDataSource pool = new HikariDataSource(config)) {
            try (Connection poolConn = pool.getConnection()) {
                assertNotNull(poolConn);
                System.out.println("连接池连接: " + poolConn.getClass().getName());
                // 注意：close() 不是销毁连接，而是归还到池中
                // 性能优势：
                // 1. 避免频繁创建/销毁物理连接的开销
                // 2. 连接复用，减少数据库服务器压力
                // 3. 可控制最大连接数，防止连接耗尽
            }
        }
    }
    // --8<-- [end:pool_vs_direct]

    // --8<-- [start:datasource_get_connection]
    /**
     * 演示从 DataSource 获取连接的标准方式
     * 通过 try-with-resources 自动归还连接到池中
     */
    @Test
    void testDataSourceGetConnection() throws SQLException {
        String url = "jdbc:h2:mem:testdb_pool_ds;DB_CLOSE_DELAY=-1";
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(USER);
        config.setPassword(PASSWORD);
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);

        try (HikariDataSource dataSource = new HikariDataSource(config)) {
            // DataSource 是 JDBC 标准接口，HikariDataSource 是其实现
            DataSource ds = dataSource;

            // 第一次获取连接
            try (Connection conn1 = ds.getConnection()) {
                assertNotNull(conn1);
                System.out.println("第1次获取连接: " + conn1);
                System.out.println("连接类: " + conn1.getClass().getName());

                // 创建测试表
                try (Statement stmt = conn1.createStatement()) {
                    stmt.execute("CREATE TABLE IF NOT EXISTS pool_test (id INT, name VARCHAR(50))");
                    stmt.executeUpdate("INSERT INTO pool_test VALUES (1, '池化连接测试')");
                }
            }
            // try-with-resources 结束时 conn1.close() 被自动调用
            // 但连接不会被销毁，而是归还到连接池中等待复用

            // 第二次获取连接（可能复用了之前归还的连接）
            try (Connection conn2 = ds.getConnection()) {
                assertNotNull(conn2);
                System.out.println("第2次获取连接: " + conn2);

                // 验证之前的数据仍然存在（共享同一个内存数据库）
                try (Statement stmt = conn2.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM pool_test WHERE id = 1")) {
                    assertTrue(rs.next(), "应能读取到之前插入的数据");
                    assertEquals("池化连接测试", rs.getString("name"));
                    System.out.println("通过连接池第2次连接验证数据: " + rs.getString("name"));
                }

                // 清理
                try (Statement stmt = conn2.createStatement()) {
                    stmt.execute("DROP TABLE IF EXISTS pool_test");
                }
            }
        }
    }
    // --8<-- [end:datasource_get_connection]

    // --8<-- [start:druid_basic_config]
    /**
     * 演示 Druid 连接池基本配置
     * Druid 是阿里巴巴开源的连接池，内置 SQL 监控、慢查询统计等功能，广泛用于国内 Java 项目
     */
    @Test
    void testDruidBasicConfig() throws Exception {
        String url = "jdbc:h2:mem:testdb_druid_basic;DB_CLOSE_DELAY=-1";

        // 创建 DruidDataSource 并配置基本参数
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(USER);
        dataSource.setPassword(PASSWORD);
        // 连接池核心参数
        dataSource.setMaxActive(10);          // 最大活跃连接数
        dataSource.setMinIdle(2);             // 最小空闲连接数
        dataSource.setInitialSize(2);         // 初始化连接数
        dataSource.setMaxWait(30000);         // 获取连接最大等待时间（毫秒）
        // 连接有效性检测
        dataSource.setValidationQuery("SELECT 1");
        dataSource.setTestWhileIdle(true);    // 空闲时检测连接有效性

        System.out.println("Druid 连接池配置完成");
        System.out.println("最大活跃连接数: " + dataSource.getMaxActive());
        System.out.println("最小空闲连接数: " + dataSource.getMinIdle());

        // 从 Druid 连接池获取连接并执行查询
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            assertNotNull(conn, "从 Druid 连接池获取的连接不应为 null");

            // ResultSet 使用 try-with-resources 确保自动关闭（与最佳实践章节一致）
            try (ResultSet rs = stmt.executeQuery("SELECT 1 AS result")) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt("result"), "Druid 连接池连接测试查询应返回 1");
                System.out.println("Druid 连接池连接测试成功，查询结果: " + rs.getInt("result"));
            }
        } finally {
            dataSource.close();
        }
    }
    // --8<-- [end:druid_basic_config]

    // --8<-- [start:druid_vs_hikari]
    /**
     * 对比 Druid 与 HikariCP 的使用方式：两者均实现 javax.sql.DataSource 接口
     * HikariCP：性能极致，是 Spring Boot 默认连接池
     * Druid：功能丰富，内置监控（SQL 监控、慢查询、连接池状态），适合需要运维可观测性的场景
     */
    @Test
    void testDruidVsHikari() throws Exception {
        String url = "jdbc:h2:mem:testdb_druid_vs;DB_CLOSE_DELAY=-1";

        // === HikariCP ===
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(USER);
        hikariConfig.setPassword(PASSWORD);
        hikariConfig.setMaximumPoolSize(5);

        try (HikariDataSource hikari = new HikariDataSource(hikariConfig);
             Connection conn = hikari.getConnection()) {
            assertNotNull(conn);
            System.out.println("HikariCP 连接类: " + conn.getClass().getName());
        }

        // === Druid ===
        DruidDataSource druid = new DruidDataSource();
        druid.setUrl(url);
        druid.setUsername(USER);
        druid.setPassword(PASSWORD);
        druid.setMaxActive(5);

        try (Connection conn = druid.getConnection()) {
            assertNotNull(conn);
            System.out.println("Druid 连接类: " + conn.getClass().getName());
        } finally {
            druid.close();
        }

        System.out.println("两种连接池均实现 javax.sql.DataSource，业务代码无需关心底层实现");
    }
    // --8<-- [end:druid_vs_hikari]
}
