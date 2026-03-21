package com.luguosong.jdbc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 演示 Statement 执行 SQL 的基本操作
 */
class StatementTest {

    private static final String URL = "jdbc:h2:mem:testdb_stmt;DB_CLOSE_DELAY=-1";
    private Connection conn;

    // --8<-- [start:setup]
    /**
     * 每个测试前：建表并插入初始数据
     */
    @BeforeEach
    void setUp() throws SQLException {
        conn = DriverManager.getConnection(URL);
        try (Statement stmt = conn.createStatement()) {
            // 创建 users 表
            stmt.execute("CREATE TABLE IF NOT EXISTS users ("
                    + "id INT, "
                    + "name VARCHAR(50), "
                    + "age INT)");
            // 插入 3 条初始数据
            stmt.executeUpdate("INSERT INTO users VALUES (1, '张三', 25)");
            stmt.executeUpdate("INSERT INTO users VALUES (2, '李四', 30)");
            stmt.executeUpdate("INSERT INTO users VALUES (3, '王五', 28)");
        }
    }
    // --8<-- [end:setup]

    // --8<-- [start:execute_query]
    /**
     * 演示 executeQuery 查询所有用户
     */
    @Test
    void testExecuteQuery() throws SQLException {
        try (Statement stmt = conn.createStatement();
             // executeQuery 专用于 SELECT 语句，返回 ResultSet
             ResultSet rs = stmt.executeQuery("SELECT * FROM users ORDER BY id")) {

            int count = 0;
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int age = rs.getInt("age");
                System.out.printf("用户: id=%d, name=%s, age=%d%n", id, name, age);
                count++;
            }
            assertEquals(3, count, "应查询到 3 条用户记录");
        }
    }
    // --8<-- [end:execute_query]

    // --8<-- [start:execute_update_insert]
    /**
     * 演示 executeUpdate 插入一条记录
     */
    @Test
    void testExecuteUpdateInsert() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // executeUpdate 用于 INSERT/UPDATE/DELETE，返回受影响的行数
            int rowsAffected = stmt.executeUpdate(
                    "INSERT INTO users VALUES (4, '赵六', 35)");
            assertEquals(1, rowsAffected, "插入操作应影响 1 行");
            System.out.println("插入影响行数: " + rowsAffected);

            // 验证插入成功
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            rs.next();
            assertEquals(4, rs.getInt(1), "插入后应有 4 条记录");
        }
    }
    // --8<-- [end:execute_update_insert]

    // --8<-- [start:execute_update_delete]
    /**
     * 演示 executeUpdate 删除记录
     */
    @Test
    void testExecuteUpdateDelete() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // 删除 age > 27 的用户（李四30岁、王五28岁，共 2 条）
            int rowsAffected = stmt.executeUpdate(
                    "DELETE FROM users WHERE age > 27");
            assertEquals(2, rowsAffected, "删除操作应影响 2 行");
            System.out.println("删除影响行数: " + rowsAffected);

            // 验证只剩 1 条记录
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            rs.next();
            assertEquals(1, rs.getInt(1), "删除后应只剩 1 条记录");
        }
    }
    // --8<-- [end:execute_update_delete]

    // --8<-- [start:execute_generic]
    /**
     * 演示 execute() 方法，处理返回 ResultSet 或更新计数两种情况
     */
    @Test
    void testExecuteGeneric() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // execute() 可以执行任意 SQL，返回 boolean：
            // true  -> 结果是 ResultSet（用 getResultSet() 获取）
            // false -> 结果是更新计数（用 getUpdateCount() 获取）

            // 情况1：执行 SELECT，返回 true
            boolean isResultSet = stmt.execute("SELECT * FROM users");
            assertTrue(isResultSet, "SELECT 语句应返回 true（有 ResultSet）");
            try (ResultSet rs = stmt.getResultSet()) {
                int count = 0;
                while (rs.next()) count++;
                assertEquals(3, count, "应查询到 3 条记录");
                System.out.println("execute SELECT 返回 ResultSet，记录数: " + count);
            }

            // 情况2：执行 UPDATE，返回 false
            boolean isResultSet2 = stmt.execute("UPDATE users SET age = age + 1 WHERE id = 1");
            assertFalse(isResultSet2, "UPDATE 语句应返回 false（无 ResultSet）");
            int updateCount = stmt.getUpdateCount();
            assertEquals(1, updateCount, "更新操作应影响 1 行");
            System.out.println("execute UPDATE 返回更新计数: " + updateCount);
        }
    }
    // --8<-- [end:execute_generic]

    // --8<-- [start:sql_injection_demo]
    /**
     * 演示 SQL 注入风险：用字符串拼接构造恶意查询
     * 这就是为什么需要 PreparedStatement 的原因！
     */
    @Test
    void testSqlInjectionDemo() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // 模拟用户输入（恶意输入）
            String maliciousInput = "' OR '1'='1";

            // ⚠️ 危险：直接拼接用户输入到 SQL 中
            String sql = "SELECT * FROM users WHERE name = '" + maliciousInput + "'";
            System.out.println("拼接后的 SQL: " + sql);
            // 实际执行的 SQL: SELECT * FROM users WHERE name = '' OR '1'='1'
            // 条件 '1'='1' 恒为 true，导致查出所有记录

            ResultSet rs = stmt.executeQuery(sql);
            int count = 0;
            while (rs.next()) {
                count++;
            }
            // 注入成功：本应查不到数据，却查到了所有 3 条记录
            assertEquals(3, count, "SQL 注入导致查到了所有记录（应该 0 条，实际 3 条）");
            System.out.println("⚠️ SQL 注入成功！查到了 " + count + " 条记录（本不应查到任何数据）");
            // 这就是为什么必须使用 PreparedStatement 来防止 SQL 注入
        }
    }
    // --8<-- [end:sql_injection_demo]

    // --8<-- [start:teardown]
    /**
     * 每个测试后：删除表并关闭连接
     */
    @AfterEach
    void tearDown() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS users");
            }
            conn.close();
        }
    }
    // --8<-- [end:teardown]
}
