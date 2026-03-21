package com.luguosong.jdbc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 演示 PreparedStatement 的参数化查询
 */
class PreparedStatementTest {

    private static final String URL = "jdbc:h2:mem:testdb_ps;DB_CLOSE_DELAY=-1";
    private Connection conn;

    // --8<-- [start:setup]
    /**
     * 每个测试前：创建 employees 表并插入 4 条数据
     */
    @BeforeEach
    void setUp() throws SQLException {
        conn = DriverManager.getConnection(URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS employees ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "name VARCHAR(50), "
                    + "department VARCHAR(50), "
                    + "salary DOUBLE)");
            stmt.executeUpdate("INSERT INTO employees (name, department, salary) VALUES ('张三', '技术部', 15000.0)");
            stmt.executeUpdate("INSERT INTO employees (name, department, salary) VALUES ('李四', '技术部', 18000.0)");
            stmt.executeUpdate("INSERT INTO employees (name, department, salary) VALUES ('王五', '市场部', 12000.0)");
            stmt.executeUpdate("INSERT INTO employees (name, department, salary) VALUES ('赵六', '市场部', 13000.0)");
        }
    }
    // --8<-- [end:setup]

    // --8<-- [start:parameterized_query]
    /**
     * 演示 PreparedStatement 带 ? 占位符查询
     */
    @Test
    void testParameterizedQuery() throws SQLException {
        // 使用 ? 作为参数占位符，避免 SQL 注入
        String sql = "SELECT * FROM employees WHERE department = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 绑定参数（索引从 1 开始）
            pstmt.setString(1, "技术部");

            try (ResultSet rs = pstmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    System.out.printf("技术部员工: id=%d, name=%s, salary=%.1f%n",
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("salary"));
                    count++;
                }
                assertEquals(2, count, "技术部应有 2 名员工");
            }
        }
    }
    // --8<-- [end:parameterized_query]

    // --8<-- [start:parameterized_insert]
    /**
     * 演示 PreparedStatement 插入数据，绑定多种类型的参数
     */
    @Test
    void testParameterizedInsert() throws SQLException {
        String sql = "INSERT INTO employees (name, department, salary) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 分别使用 setString、setString、setDouble 绑定不同类型参数
            pstmt.setString(1, "孙七");
            pstmt.setString(2, "财务部");
            pstmt.setDouble(3, 14000.0);

            int rowsAffected = pstmt.executeUpdate();
            assertEquals(1, rowsAffected, "插入应影响 1 行");
            System.out.println("PreparedStatement 插入影响行数: " + rowsAffected);

            // 验证插入成功
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM employees")) {
                rs.next();
                assertEquals(5, rs.getInt(1), "插入后应有 5 条记录");
            }
        }
    }
    // --8<-- [end:parameterized_insert]

    // --8<-- [start:parameterized_update]
    /**
     * 演示 PreparedStatement 更新操作
     */
    @Test
    void testParameterizedUpdate() throws SQLException {
        String sql = "UPDATE employees SET salary = ? WHERE name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, 20000.0); // 新薪资
            pstmt.setString(2, "张三");   // 更新条件

            int rowsAffected = pstmt.executeUpdate();
            assertEquals(1, rowsAffected, "更新应影响 1 行");

            // 验证更新成功
            try (PreparedStatement query = conn.prepareStatement(
                    "SELECT salary FROM employees WHERE name = ?")) {
                query.setString(1, "张三");
                try (ResultSet rs = query.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(20000.0, rs.getDouble("salary"), 0.01,
                            "张三的薪资应更新为 20000.0");
                    System.out.println("张三薪资更新后: " + rs.getDouble("salary"));
                }
            }
        }
    }
    // --8<-- [end:parameterized_update]

    // --8<-- [start:prevent_sql_injection]
    /**
     * 对比 Statement（注入成功）和 PreparedStatement（注入失败）
     * 用断言证明 PreparedStatement 的安全性
     */
    @Test
    void testPreventSqlInjection() throws SQLException {
        String maliciousInput = "' OR '1'='1";

        // --- Statement：SQL 注入成功 ---
        try (Statement stmt = conn.createStatement()) {
            String unsafeSql = "SELECT * FROM employees WHERE name = '" + maliciousInput + "'";
            ResultSet rs = stmt.executeQuery(unsafeSql);
            int unsafeCount = 0;
            while (rs.next()) unsafeCount++;
            // 注入成功：查到了所有 4 条记录
            assertEquals(4, unsafeCount, "Statement 拼接导致 SQL 注入，查到所有记录");
            System.out.println("⚠️ Statement 拼接（注入成功）: 查到 " + unsafeCount + " 条");
        }

        // --- PreparedStatement：SQL 注入失败 ---
        String safeSql = "SELECT * FROM employees WHERE name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(safeSql)) {
            // 恶意输入被当作普通字符串字面值处理，不会破坏 SQL 结构
            pstmt.setString(1, maliciousInput);
            try (ResultSet rs = pstmt.executeQuery()) {
                int safeCount = 0;
                while (rs.next()) safeCount++;
                // 注入失败：没有名字叫 "' OR '1'='1" 的员工，查到 0 条
                assertEquals(0, safeCount,
                        "PreparedStatement 防止了 SQL 注入，恶意输入被当作字面值");
                System.out.println("✅ PreparedStatement（注入失败）: 查到 " + safeCount + " 条");
            }
        }
    }
    // --8<-- [end:prevent_sql_injection]

    // --8<-- [start:reuse_prepared_statement]
    /**
     * 演示复用同一个 PreparedStatement 对象多次执行
     * 预编译的 SQL 只解析一次，改变参数值即可重复执行，性能更优
     */
    @Test
    void testReusePreparedStatement() throws SQLException {
        String sql = "SELECT * FROM employees WHERE department = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 第一次执行：查询技术部
            pstmt.setString(1, "技术部");
            try (ResultSet rs = pstmt.executeQuery()) {
                int count = 0;
                while (rs.next()) count++;
                assertEquals(2, count, "技术部应有 2 名员工");
                System.out.println("第1次执行（技术部）: " + count + " 条");
            }

            // 第二次执行：改变参数值，查询市场部（复用同一个 PreparedStatement）
            pstmt.setString(1, "市场部");
            try (ResultSet rs = pstmt.executeQuery()) {
                int count = 0;
                while (rs.next()) count++;
                assertEquals(2, count, "市场部应有 2 名员工");
                System.out.println("第2次执行（市场部）: " + count + " 条");
            }

            // 第三次执行：查询不存在的部门
            pstmt.setString(1, "不存在的部门");
            try (ResultSet rs = pstmt.executeQuery()) {
                int count = 0;
                while (rs.next()) count++;
                assertEquals(0, count, "不存在的部门应返回 0 条记录");
                System.out.println("第3次执行（不存在的部门）: " + count + " 条");
            }
            // 预编译优势：SQL 只编译一次，三次执行只需替换参数值
        }
    }
    // --8<-- [end:reuse_prepared_statement]

    // --8<-- [start:blob_write_read]
    /**
     * 演示 PreparedStatement 处理 BLOB 大字段
     * 写入：setBytes() / setBinaryStream()
     * 读取：getBytes() / getBinaryStream()
     * 典型场景：存储图片、文件、音频等二进制数据
     */
    @Test
    void testBlobWriteAndRead() throws Exception {
        // 建一张含 BLOB 列的表（在测试内自行建表，避免影响其他测试）
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS attachments ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "filename VARCHAR(100), "
                    + "content BLOB)");
        }

        // === 写入 BLOB 数据 ===
        byte[] originalData = "Hello, JDBC Blob! 这是二进制内容。".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        String insertSql = "INSERT INTO attachments (filename, content) VALUES (?, ?)";

        try {
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, "test.txt");
                // setBytes：直接传入 byte[]（适合小文件）
                pstmt.setBytes(2, originalData);
                int rows = pstmt.executeUpdate();
                assertEquals(1, rows, "BLOB 写入应影响 1 行");
                System.out.println("BLOB 写入成功，数据长度: " + originalData.length + " 字节");
            }

            // === 读取 BLOB 数据 ===
            String querySql = "SELECT filename, content FROM attachments WHERE filename = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(querySql)) {
                pstmt.setString(1, "test.txt");
                try (ResultSet rs = pstmt.executeQuery()) {
                    assertTrue(rs.next(), "应查到 BLOB 记录");
                    String filename = rs.getString("filename");
                    // getBytes：直接读出 byte[]
                    byte[] readData = rs.getBytes("content");

                    assertEquals("test.txt", filename);
                    assertArrayEquals(originalData, readData, "读取的 BLOB 数据应与写入的完全一致");

                    String readText = new String(readData, java.nio.charset.StandardCharsets.UTF_8);
                    System.out.println("BLOB 读取成功，文件名: " + filename + "，内容: " + readText);
                }
            }
        } finally {
            // 使用 finally 确保断言失败时也能清理临时表
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS attachments");
            }
        }
    }
    // --8<-- [end:blob_write_read]

    // --8<-- [start:teardown]
    /**
     * 每个测试后：删除表并关闭连接
     */
    @AfterEach
    void tearDown() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS employees");
            }
            conn.close();
        }
    }
    // --8<-- [end:teardown]
}
