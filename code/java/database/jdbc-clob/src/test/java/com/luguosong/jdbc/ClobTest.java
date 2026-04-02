package com.luguosong.jdbc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Writer;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 演示 JDBC CLOB（Character Large Object）的读写操作
 */
class ClobTest {

    private static final String URL = "jdbc:h2:mem:testdb_clob;DB_CLOSE_DELAY=-1";
    private Connection conn;

    @BeforeEach
    void setUp() throws SQLException {
        conn = DriverManager.getConnection(URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS documents ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "name VARCHAR(100), "
                    + "content CLOB)");
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS documents");
            }
            conn.close();
        }
    }

    // region clob_write
    /**
     * CLOB 写入：使用 Connection.createClob() + setCharacterStream 写入大文本
     */
    @Test
    @DisplayName("CLOB 写入：createClob + setCharacterStream")
    void testClobWrite() throws SQLException {
        String longText = "JDBC（Java Database Connectivity）是 Java 访问数据库的标准 API。"
                + "它提供了一套统一的接口，使 Java 程序能够以数据库无关的方式执行 SQL 语句、"
                + "检索结果集，并管理数据库连接。JDBC API 由 java.sql 和 javax.sql 包组成，"
                + "是 Java SE 和 Java EE 平台的核心组成部分。通过 JDBC，开发者可以连接各种关系型数据库，"
                + "包括 MySQL、PostgreSQL、Oracle、SQL Server 等。";

        // 通过 Connection.createClob() 创建空的 Clob 对象
        Clob clob = conn.createClob();
        // 获取 Writer，从位置 1 开始写入（CLOB 位置从 1 开始）
        try (Writer writer = clob.setCharacterStream(1)) {
            writer.write(longText);
        } catch (IOException e) {
            throw new SQLException("写入 CLOB 内容失败", e);
        }

        // 使用 PreparedStatement.setClob() 绑定并插入
        String sql = "INSERT INTO documents (name, content) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "JDBC 简介");
            ps.setClob(2, clob);
            int affectedRows = ps.executeUpdate();
            assertEquals(1, affectedRows, "应成功插入 1 条记录");
        }

        // 验证插入成功：查询记录数
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM documents")) {
            assertTrue(rs.next(), "应能查询到记录");
            assertEquals(1, rs.getInt(1), "documents 表应有 1 条记录");
        }
    }
    // endregion clob_write

    // region clob_write_string
    /**
     * CLOB 写入：直接使用 setString() 插入文本（适合较小内容）
     */
    @Test
    @DisplayName("CLOB 写入：setString 直接方式")
    void testClobWriteString() throws SQLException {
        String content = "这是一段通过 setString() 直接写入的文本。"
                + "对于较小的文本内容，无需创建 Clob 对象，直接使用 setString() 即可。"
                + "H2 数据库的 CLOB 类型支持直接设置字符串值，简化了操作流程。";

        String sql = "INSERT INTO documents (name, content) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "简短文档");
            // 直接用 setString() 设置 CLOB 列值（H2 支持，Oracle 等对大文本需用 setClob）
            ps.setString(2, content);
            int affectedRows = ps.executeUpdate();
            assertEquals(1, affectedRows, "应成功插入 1 条记录");
        }

        // 验证插入内容
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT content FROM documents WHERE name = ?")) {
            ps.setString(1, "简短文档");
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "应能查询到刚插入的记录");
                assertEquals(content, rs.getString("content"), "读取的内容应与写入的一致");
            }
        }
    }
    // endregion clob_write_string

    // region clob_read
    /**
     * CLOB 读取：使用 getClob() 和 getString() 两种方式读取大文本
     */
    @Test
    @DisplayName("CLOB 读取：getClob 和 getString")
    void testClobRead() throws SQLException {
        // 先插入一条测试数据
        String expectedContent = "CLOB（Character Large Object）用于存储大文本数据，"
                + "最大可存储数 GB 的字符数据。与 VARCHAR 不同，CLOB 专门为海量文本设计，"
                + "适合存储文章内容、日志文件、XML/JSON 文档等大文本场景。"
                + "在 JDBC 中，通过 java.sql.Clob 接口操作 CLOB 数据，"
                + "支持流式读写以避免将全部内容加载到内存。";
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO documents (name, content) VALUES (?, ?)")) {
            ps.setString(1, "CLOB 说明文档");
            ps.setString(2, expectedContent);
            ps.executeUpdate();
        }

        // 方式一：使用 ResultSet.getClob() 获取 Clob 对象，再通过 getSubString() 读取
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT name, content FROM documents WHERE name = ?")) {
            ps.setString(1, "CLOB 说明文档");
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "应能查询到记录");

                // getClob() 返回 Clob 对象
                Clob clob = rs.getClob("content");
                assertNotNull(clob, "Clob 对象不应为 null");

                // getSubString(pos, length): 从指定位置读取指定长度的子串（位置从 1 开始）
                String contentFromClob = clob.getSubString(1, (int) clob.length());
                assertEquals(expectedContent, contentFromClob,
                        "通过 Clob.getSubString() 读取的内容应与写入的一致");

                // 方式二：直接使用 getString()，更简洁（适合较小的 CLOB）
                String contentFromString = rs.getString("content");
                assertEquals(expectedContent, contentFromString,
                        "通过 getString() 读取的内容应与写入的一致");
            }
        }
    }
    // endregion clob_read

    // region clob_free
    /**
     * CLOB 资源释放：演示 free() 方法的使用
     * <p>
     * 对于大型 CLOB 对象，在长事务中及时调用 free() 可以释放数据库端和驱动端的资源。
     * H2 的内存数据库中 free() 效果不明显，但在 Oracle 等真实数据库中，
     * 未释放的 CLOB 可能占用临时表空间，尤其在批量处理场景下。
     */
    @Test
    @DisplayName("CLOB 资源释放：free() 方法")
    void testClobFree() throws SQLException {
        // 通过 Connection.createClob() 创建 Clob 对象
        Clob clob = conn.createClob();
        assertNotNull(clob, "createClob() 应返回非 null 的 Clob 对象");

        String expectedText = "用于演示 free() 的测试文本，在实际生产环境中，"
                + "处理完大文本后应及时释放资源。";
        try (Writer writer = clob.setCharacterStream(1)) {
            writer.write(expectedText);
        } catch (IOException e) {
            throw new SQLException("写入 CLOB 内容失败", e);
        }

        // 验证 Clob 对象可用
        assertEquals(expectedText.length(), clob.length(), "Clob 长度应与写入的文本一致");

        // 调用 free() 释放资源
        clob.free();

        // 释放后再次访问 Clob 会抛出 SQLException
        assertThrows(SQLException.class, () -> clob.length(),
                "free() 之后访问 Clob 应抛出 SQLException");
    }
    // endregion clob_free
}
