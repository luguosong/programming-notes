package com.luguosong.jdbc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * 演示 JDBC 高级数据类型操作（ARRAY、SQLXML）
 * <p>
 * 注意：H2 对高级类型的支持有限，部分操作可能被跳过。
 * 实际项目中请根据数据库厂商文档使用对应的高级类型特性。
 */
class AdvancedTypesTest {

    private static final String URL = "jdbc:h2:mem:testdb_advanced;DB_CLOSE_DELAY=-1";

    private Connection conn;

    @BeforeEach
    void setUp() throws SQLException {
        conn = DriverManager.getConnection(URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS products ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "name VARCHAR(100), "
                    + "tags VARCHAR ARRAY)");
            stmt.execute("CREATE TABLE IF NOT EXISTS xml_documents ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "content CLOB)");
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS products");
                stmt.execute("DROP TABLE IF EXISTS xml_documents");
            }
            conn.close();
        }
    }

    // region array_create
    /**
     * ARRAY 创建：使用 Connection.createArrayOf() 创建数组并插入数据库
     */
    @Test
    @DisplayName("ARRAY 创建：通过 createArrayOf 创建数组并插入")
    void testArrayCreate() throws SQLException {
        // 构造标签数组
        String[] tags = {"Java", "JDBC", "Database"};

        // 使用 Connection.createArrayOf 创建 java.sql.Array 对象
        java.sql.Array sqlArray = conn.createArrayOf("VARCHAR", tags);

        // 通过 PreparedStatement.setArray() 插入数组
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO products (name, tags) VALUES (?, ?)")) {
            ps.setString(1, "JDBC 高级类型教程");
            ps.setArray(2, sqlArray);
            int affectedRows = ps.executeUpdate();
            assertEquals(1, affectedRows, "应成功插入 1 行");
        }

        // 验证插入结果
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products WHERE name = 'JDBC 高级类型教程'")) {
            assertTrue(rs.next(), "应有查询结果");
            assertEquals(1, rs.getInt(1), "应有 1 条匹配记录");
        }

        System.out.println("ARRAY 创建测试通过，标签: " + Arrays.toString(tags));
    }
    // endregion

    // region array_read
    /**
     * ARRAY 读取：从 ResultSet 获取数组并转换为 Java 数组
     */
    @Test
    @DisplayName("ARRAY 读取：从 ResultSet 获取数组内容")
    void testArrayRead() throws SQLException {
        // 先插入测试数据
        String[] tags = {"Spring", "Boot", "Security"};
        java.sql.Array sqlArray = conn.createArrayOf("VARCHAR", tags);
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO products (name, tags) VALUES (?, ?)")) {
            ps.setString(1, "Spring Security 实战");
            ps.setArray(2, sqlArray);
            ps.executeUpdate();
        }

        // 从 ResultSet 读取数组
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name, tags FROM products WHERE name = 'Spring Security 实战'")) {
            assertTrue(rs.next(), "应有查询结果");

            // 使用 getArray() 获取 java.sql.Array 对象
            java.sql.Array resultArray = rs.getArray("tags");
            assertNotNull(resultArray, "tags 列不应为 null");

            // 将 java.sql.Array 转换为 String[]（H2 返回 Object[]，需手动转换）
            Object[] arrayObj = (Object[]) resultArray.getArray();
            String[] resultTags = new String[arrayObj.length];
            for (int i = 0; i < arrayObj.length; i++) {
                resultTags[i] = (String) arrayObj[i];
            }

            // 打印每个元素
            System.out.println("读取到的标签:");
            for (String tag : resultTags) {
                System.out.println("  - " + tag);
            }

            // 验证数组内容与插入时一致
            assertArrayEquals(tags, resultTags, "读取的数组应与插入时一致");
        }
    }
    // endregion

    // region sqlxml_write_read
    /**
     * SQLXML 演示：创建并操作 SQLXML 对象
     * <p>
     * 注意：SQLXML 支持因数据库厂商而异。H2 不完全支持 SQLXML，
     * 在 MySQL、PostgreSQL、Oracle 等数据库中 SQLXML 通常有更完善的支持。
     * 此处使用 CLOB 列存储 XML 文本作为替代方案。
     */
    @Test
    @DisplayName("SQLXML 演示：创建和读取 XML 数据")
    void testSqlXmlWriteRead() throws SQLException {
        // 尝试使用 createSQLXML()，H2 可能不支持
        String xmlContent = "<root><item>test</item></root>";

        try {
            // 创建 SQLXML 对象
            SQLXML sqlxml = conn.createSQLXML();
            sqlxml.setString(xmlContent);

            // 插入数据——将 SQLXML 作为字符串写入 CLOB 列
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO xml_documents (content) VALUES (?)")) {
                ps.setString(1, sqlxml.getString());
                int affectedRows = ps.executeUpdate();
                assertEquals(1, affectedRows, "应成功插入 1 行");
            }

            // 释放 SQLXML 资源
            sqlxml.free();

            // 读取并验证 XML 内容
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT content FROM xml_documents")) {
                assertTrue(rs.next(), "应有查询结果");
                String resultXml = rs.getString("content");
                assertNotNull(resultXml, "XML 内容不应为 null");
                assertTrue(resultXml.contains("<item>test</item>"),
                        "XML 应包含预期的元素内容");
                System.out.println("读取到的 XML: " + resultXml);
            }

        } catch (SQLFeatureNotSupportedException e) {
            // H2 不支持 SQLXML 时优雅跳过
            assumeTrue(false, "SQLXML not supported: " + e.getMessage());
        }
    }
    // endregion
}
