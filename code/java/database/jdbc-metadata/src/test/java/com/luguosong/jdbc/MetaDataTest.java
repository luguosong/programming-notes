package com.luguosong.jdbc;

import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 演示数据库元数据操作
 */
class MetaDataTest {

    private static final String URL = "jdbc:h2:mem:testdb_meta;DB_CLOSE_DELAY=-1";

    // --8<-- [start:database_metadata]
    /**
     * 获取 DatabaseMetaData，打印数据库和驱动信息
     */
    @Test
    void testDatabaseMetadata() throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL)) {
            // 从连接获取数据库元数据
            DatabaseMetaData dbmd = conn.getMetaData();

            // 数据库产品信息
            String productName = dbmd.getDatabaseProductName();
            String productVersion = dbmd.getDatabaseProductVersion();
            System.out.println("数据库产品名称: " + productName);
            System.out.println("数据库产品版本: " + productVersion);

            // 驱动信息
            String driverName = dbmd.getDriverName();
            String driverVersion = dbmd.getDriverVersion();
            System.out.println("驱动名称: " + driverName);
            System.out.println("驱动版本: " + driverVersion);

            // JDBC 版本
            int jdbcMajor = dbmd.getJDBCMajorVersion();
            int jdbcMinor = dbmd.getJDBCMinorVersion();
            System.out.println("JDBC 版本: " + jdbcMajor + "." + jdbcMinor);

            // 断言验证
            assertNotNull(productName, "数据库产品名称不应为 null");
            assertTrue(productName.contains("H2"), "应为 H2 数据库");
            assertNotNull(driverName, "驱动名称不应为 null");
            assertTrue(jdbcMajor >= 4, "JDBC 主版本应 >= 4");
        }
    }
    // --8<-- [end:database_metadata]

    // --8<-- [start:database_metadata_tables]
    /**
     * 用 DatabaseMetaData.getTables() 获取表信息
     */
    @Test
    void testDatabaseMetadataTables() throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL)) {
            // 先创建一个测试表
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS metadata_test ("
                        + "id INT PRIMARY KEY, "
                        + "name VARCHAR(100))");
            }

            DatabaseMetaData dbmd = conn.getMetaData();
            // getTables 参数：catalog, schemaPattern, tableNamePattern, types
            // null 表示不过滤，"%" 匹配所有
            try (ResultSet tables = dbmd.getTables(null, null, "%", new String[]{"TABLE"})) {
                boolean foundTestTable = false;
                System.out.println("=== 数据库中的表 ===");
                while (tables.next()) {
                    String catalog = tables.getString("TABLE_CAT");
                    String schema = tables.getString("TABLE_SCHEM");
                    String tableName = tables.getString("TABLE_NAME");
                    String tableType = tables.getString("TABLE_TYPE");
                    System.out.printf("表: catalog=%s, schema=%s, name=%s, type=%s%n",
                            catalog, schema, tableName, tableType);
                    if ("METADATA_TEST".equalsIgnoreCase(tableName)) {
                        foundTestTable = true;
                    }
                }
                assertTrue(foundTestTable, "应能查到 METADATA_TEST 表");
            }

            // 清理
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS metadata_test");
            }
        }
    }
    // --8<-- [end:database_metadata_tables]

    // --8<-- [start:resultset_metadata]
    /**
     * 从 ResultSetMetaData 获取列信息
     */
    @Test
    void testResultSetMetadata() throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL)) {
            // 创建测试表
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS rs_meta_test ("
                        + "id INT, "
                        + "name VARCHAR(100), "
                        + "score DOUBLE, "
                        + "active BOOLEAN)");
                stmt.executeUpdate(
                        "INSERT INTO rs_meta_test VALUES (1, '测试', 95.5, true)");
            }

            // 从查询结果获取 ResultSetMetaData
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM rs_meta_test")) {

                ResultSetMetaData rsmd = rs.getMetaData();
                int columnCount = rsmd.getColumnCount();
                assertEquals(4, columnCount, "应有 4 列");
                System.out.println("列数: " + columnCount);

                System.out.println("=== 列信息 ===");
                for (int i = 1; i <= columnCount; i++) {
                    String colName = rsmd.getColumnName(i);
                    String colTypeName = rsmd.getColumnTypeName(i);
                    System.out.printf("列 %d: name=%s, typeName=%s%n",
                            i, colName, colTypeName);
                }

                // 断言验证列名
                assertEquals("ID", rsmd.getColumnName(1));
                assertEquals("NAME", rsmd.getColumnName(2));
                assertEquals("SCORE", rsmd.getColumnName(3));
                assertEquals("ACTIVE", rsmd.getColumnName(4));
            }

            // 清理
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS rs_meta_test");
            }
        }
    }
    // --8<-- [end:resultset_metadata]
}
