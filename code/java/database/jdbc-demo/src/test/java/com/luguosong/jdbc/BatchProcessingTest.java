package com.luguosong.jdbc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 演示批处理操作
 */
class BatchProcessingTest {

    private static final String URL = "jdbc:h2:mem:testdb_batch;DB_CLOSE_DELAY=-1";
    private Connection conn;

    // --8<-- [start:setup]
    /**
     * 每个测试前：创建 logs 表
     */
    @BeforeEach
    void setUp() throws SQLException {
        conn = DriverManager.getConnection(URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS logs ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "message VARCHAR(200), "
                    + "created_at TIMESTAMP)");
        }
    }
    // --8<-- [end:setup]

    // --8<-- [start:statement_batch]
    /**
     * 演示 Statement 批处理：addBatch 添加多条 SQL，executeBatch 批量执行
     */
    @Test
    void testStatementBatch() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // 使用 addBatch 添加多条 INSERT SQL
            stmt.addBatch("INSERT INTO logs (message, created_at) VALUES ('系统启动', CURRENT_TIMESTAMP)");
            stmt.addBatch("INSERT INTO logs (message, created_at) VALUES ('用户登录', CURRENT_TIMESTAMP)");
            stmt.addBatch("INSERT INTO logs (message, created_at) VALUES ('数据查询', CURRENT_TIMESTAMP)");
            stmt.addBatch("INSERT INTO logs (message, created_at) VALUES ('用户登出', CURRENT_TIMESTAMP)");

            // executeBatch 一次性执行所有批量 SQL，返回每条的影响行数
            int[] results = stmt.executeBatch();
            assertEquals(4, results.length, "批处理应包含 4 条 SQL");

            for (int i = 0; i < results.length; i++) {
                System.out.printf("批处理第 %d 条: 影响 %d 行%n", i + 1, results[i]);
                assertEquals(1, results[i], "每条 INSERT 应影响 1 行");
            }

            // 验证总记录数
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM logs");
            rs.next();
            assertEquals(4, rs.getInt(1), "批处理后应有 4 条记录");
        }
    }
    // --8<-- [end:statement_batch]

    // --8<-- [start:prepared_statement_batch]
    /**
     * 演示 PreparedStatement 批处理：比 Statement 批处理更安全高效
     */
    @Test
    void testPreparedStatementBatch() throws SQLException {
        // PreparedStatement 批处理：SQL 预编译一次，参数绑定多次
        String sql = "INSERT INTO logs (message, created_at) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 批量插入 10 条记录
            for (int i = 1; i <= 10; i++) {
                pstmt.setString(1, "批量日志消息 #" + i);
                pstmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                // 将当前参数组添加到批次中
                pstmt.addBatch();
            }

            // 一次性执行所有批量操作
            int[] results = pstmt.executeBatch();
            assertEquals(10, results.length, "批处理应包含 10 条操作");
            System.out.println("PreparedStatement 批处理执行了 " + results.length + " 条");

            // 验证总记录数
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM logs")) {
                rs.next();
                assertEquals(10, rs.getInt(1), "批处理后应有 10 条记录");
                System.out.println("表中总记录数: " + rs.getInt(1));
            }
            // 优势：SQL 只编译一次，参数绑定防止注入，性能优于 Statement 批处理
        }
    }
    // --8<-- [end:prepared_statement_batch]

    // --8<-- [start:batch_with_transaction]
    /**
     * 演示批处理 + 事务：性能最佳实践
     * 在事务中执行批处理，批处理完成后统一提交
     */
    @Test
    void testBatchWithTransaction() throws SQLException {
        // 关闭自动提交，在事务中执行批处理（性能最佳实践）
        conn.setAutoCommit(false);

        try {
            String sql = "INSERT INTO logs (message, created_at) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (int i = 1; i <= 20; i++) {
                    pstmt.setString(1, "事务批量日志 #" + i);
                    pstmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                    pstmt.addBatch();

                    // 每 10 条执行一次批处理（大数据量时控制内存）
                    if (i % 10 == 0) {
                        int[] results = pstmt.executeBatch();
                        System.out.println("执行了一批 " + results.length + " 条");
                    }
                }
            }

            // 所有批次执行完毕后统一提交事务
            conn.commit();
            System.out.println("事务已提交");

            // 验证总记录数
            conn.setAutoCommit(true);
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM logs")) {
                rs.next();
                assertEquals(20, rs.getInt(1), "事务批处理后应有 20 条记录");
                System.out.println("事务批处理总记录数: " + rs.getInt(1));
            }
            // 优势：减少数据库提交次数，提升大批量插入性能
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        }
    }
    // --8<-- [end:batch_with_transaction]

    // --8<-- [start:teardown]
    /**
     * 每个测试后：删除表并关闭连接
     */
    @AfterEach
    void tearDown() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS logs");
            }
            conn.close();
        }
    }
    // --8<-- [end:teardown]
}
