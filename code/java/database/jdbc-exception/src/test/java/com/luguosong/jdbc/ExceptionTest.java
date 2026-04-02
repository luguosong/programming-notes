package com.luguosong.jdbc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JDBC 异常处理演示
 * <p>
 * 涵盖 SQLException 异常链遍历、SQLState 过滤、SQLWarning 获取等场景
 */
class ExceptionTest {

    private Connection conn;

    @BeforeEach
    void setUp() throws SQLException {
        String url = "jdbc:h2:mem:testdb";
        conn = DriverManager.getConnection(url, "sa", "");
        // 创建测试表并插入初始数据
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE exception_test (id INT, name VARCHAR(50))");
            stmt.executeUpdate("INSERT INTO exception_test VALUES (1, '测试数据')");
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    // region exception_chain
    /**
     * 演示 SQLException 异常链遍历
     * <p>
     * 一个 SQLException 可能关联多个异常，通过 getNextException() 逐个获取
     */
    @Test
    @DisplayName("异常链遍历")
    void testExceptionChain() {
        // 查询不存在的表，触发 SQLException
        try (Statement stmt = conn.createStatement()) {
            stmt.executeQuery("SELECT * FROM non_existent_table");
            fail("应该抛出 SQLException");
        } catch (SQLException e) {
            // 遍历异常链，打印每个异常的信息
            System.out.println("=== 异常链遍历 ===");
            System.out.println("异常总数: " + countExceptions(e));

            SQLException current = e;
            int index = 1;
            while (current != null) {
                System.out.println("异常 #" + index + ":");
                System.out.println("  Message: " + current.getMessage());
                System.out.println("  SQLState: " + current.getSQLState());
                System.out.println("  ErrorCode: " + current.getErrorCode());
                current = current.getNextException();
                index++;
            }

            // 验证至少有一个异常
            assertNotNull(e.getMessage());
        }
    }

    /**
     * 递归统计异常链中的异常总数
     */
    private int countExceptions(SQLException e) {
        int count = 1;
        SQLException next = e.getNextException();
        while (next != null) {
            count++;
            next = next.getNextException();
        }
        return count;
    }
    // endregion exception_chain

    // region sqlstate_filter
    /**
     * 演示通过 SQLState 过滤异常类型
     * <p>
     * SQLState 是一个 5 字符字符串，遵循 X/Open SQL 约定：
     * - 42S02：表或视图不存在
     * - 23000：违反完整性约束（如主键冲突）
     * - 08001：无法建立连接
     */
    @Test
    @DisplayName("SQLState 过滤")
    void testSqlStateFilter() {
        // 尝试删除不存在的表
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE non_existent_table");
            fail("应该抛出 SQLException");
        } catch (SQLException e) {
            String sqlState = e.getSQLState();
            System.out.println("=== SQLState 过滤 ===");
            System.out.println("SQLState: " + sqlState);
            System.out.println("Message: " + e.getMessage());

            if ("42S02".equals(sqlState)) {
                // 表不存在的错误码，属于预期的非致命错误
                System.out.println("[预期错误] 表不存在，可以安全忽略或自动创建表");
            } else {
                // 其他 SQLState 表示非预期错误，需要关注
                System.out.println("[意外错误] 未预期的 SQLState: " + sqlState);
                fail("意外错误: " + e.getMessage());
            }

            // 验证 SQLState 以 "42" 开头（语法错误或访问规则类错误）
            assertTrue(sqlState.startsWith("42"),
                    "表不存在错误的 SQLState 应以 '42' 开头");
        }
    }
    // endregion sqlstate_filter

    // region sql_warning
    /**
     * 演示 SQLWarning 的获取与处理
     * <p>
     * SQLWarning 是 SQLException 的子类，表示数据库操作产生的警告信息。
     * 与 SQLException 不同，警告不会中断程序执行，需要主动调用 getWarnings() 获取。
     * H2 内存数据库通常不产生警告，这里主要演示 API 的使用方式。
     */
    @Test
    @DisplayName("SQLWarning 获取")
    void testSqlWarning() throws SQLException {
        System.out.println("=== SQLWarning 获取 ===");

        // 1. Connection 级别的警告
        SQLWarning connWarning = conn.getWarnings();
        if (connWarning != null) {
            System.out.println("Connection 警告: " + connWarning.getMessage());
            // 遍历警告链（与异常链类似）
            SQLWarning w = connWarning;
            while (w != null) {
                System.out.println("  Warning: " + w.getMessage()
                        + ", SQLState: " + w.getSQLState());
                w = w.getNextWarning();
            }
        } else {
            System.out.println("Connection 无警告");
        }

        // 2. Statement 级别的警告
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("SELECT * FROM exception_test");

            SQLWarning stmtWarning = stmt.getWarnings();
            if (stmtWarning != null) {
                System.out.println("Statement 警告: " + stmtWarning.getMessage());
            } else {
                System.out.println("Statement 无警告");
            }

            // 3. ResultSet 级别的警告
            try (ResultSet rs = stmt.getResultSet()) {
                SQLWarning rsWarning = rs.getWarnings();
                if (rsWarning != null) {
                    System.out.println("ResultSet 警告: " + rsWarning.getMessage());
                } else {
                    System.out.println("ResultSet 无警告");
                }
            }
        }

        // 4. clearWarnings() 演示：清除所有已记录的警告
        conn.clearWarnings();
        System.out.println("已调用 conn.clearWarnings()，Connection 警告已清除");
        assertNull(conn.getWarnings(), "清除后不应有警告");

        // 验证基本功能正常：确认测试表数据可查询
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM exception_test")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1), "测试表应有 1 条数据");
        }
    }
    // endregion sql_warning
}
