package com.luguosong.jdbc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 演示 ResultSet 的各种访问方式
 */
class ResultSetTest {

    private static final String URL = "jdbc:h2:mem:testdb_rs;DB_CLOSE_DELAY=-1";
    private Connection conn;

    // --8<-- [start:setup]
    /**
     * 每个测试前：创建 products 表并插入 5 条数据
     */
    @BeforeEach
    void setUp() throws SQLException {
        conn = DriverManager.getConnection(URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS products ("
                    + "id INT, "
                    + "name VARCHAR(50), "
                    + "price DECIMAL(10,2), "
                    + "in_stock BOOLEAN)");
            stmt.executeUpdate("INSERT INTO products VALUES (1, '笔记本电脑', 5999.99, true)");
            stmt.executeUpdate("INSERT INTO products VALUES (2, '无线鼠标', 89.50, true)");
            stmt.executeUpdate("INSERT INTO products VALUES (3, '机械键盘', 399.00, false)");
            stmt.executeUpdate("INSERT INTO products VALUES (4, '显示器', 1299.00, true)");
            stmt.executeUpdate("INSERT INTO products VALUES (5, '耳机', 199.90, false)");
        }
    }
    // --8<-- [end:setup]

    // --8<-- [start:iterate_forward]
    /**
     * 演示 next() 向前遍历 ResultSet，按列名取值
     */
    @Test
    void testIterateForward() throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM products ORDER BY id")) {

            int count = 0;
            while (rs.next()) {
                // 按列名获取不同类型的值
                String name = rs.getString("name");
                int id = rs.getInt("id");
                double price = rs.getDouble("price");
                boolean inStock = rs.getBoolean("in_stock");
                System.out.printf("产品: id=%d, name=%s, price=%.2f, inStock=%b%n",
                        id, name, price, inStock);
                count++;
            }
            assertEquals(5, count, "应遍历到 5 条产品记录");
        }
    }
    // --8<-- [end:iterate_forward]

    // --8<-- [start:get_by_index]
    /**
     * 演示按列索引（1-based）获取数据
     */
    @Test
    void testGetByIndex() throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT id, name, price, in_stock FROM products WHERE id = 1")) {

            assertTrue(rs.next(), "应有一条记录");

            // 按列索引获取（从 1 开始）
            int id = rs.getInt(1);           // 第1列: id
            String name = rs.getString(2);   // 第2列: name
            double price = rs.getDouble(3);  // 第3列: price
            boolean inStock = rs.getBoolean(4); // 第4列: in_stock

            // 按列名获取（两种方式结果相同）
            int idByName = rs.getInt("id");
            String nameByName = rs.getString("name");

            assertEquals(id, idByName, "按索引和按列名获取的 id 应一致");
            assertEquals(name, nameByName, "按索引和按列名获取的 name 应一致");
            assertEquals(1, id);
            assertEquals("笔记本电脑", name);

            System.out.println("按索引获取: id=" + id + ", name=" + name
                    + ", price=" + price + ", inStock=" + inStock);
            System.out.println("按列名获取: id=" + idByName + ", name=" + nameByName);
            // 注意：按列名可读性更好，按索引性能略高
        }
    }
    // --8<-- [end:get_by_index]

    // --8<-- [start:result_set_metadata_in_rs]
    /**
     * 演示在遍历 ResultSet 过程中访问列元数据（ResultSetMetaData）。
     * <p>
     * 与 MetaDataTest.testResultSetMetadata() 的区别：
     * 后者专门演示 DatabaseMetaData / ResultSetMetaData 的完整能力（类型名、精度等）；
     * 本方法聚焦于"边遍历数据、边读取列元数据"这一典型使用场景——
     * 即在不知道列数和列名的情况下，动态构建输出。
     */
    @Test
    void testResultSetMetadataInRs() throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM products")) {

            // 查询执行后即可从 ResultSet 获取元数据，无需先调用 next()
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();
            // products 表有 4 列：id, name, price, in_stock
            assertEquals(4, columnCount, "products 表应有 4 列");

            // 打印列名标题行（体现"不硬编码列名，动态读取"的使用场景）
            System.out.println("=== 动态列名标题 ===");
            for (int i = 1; i <= columnCount; i++) {
                System.out.printf("%-15s", meta.getColumnName(i));
            }
            System.out.println();

            // 遍历第一行数据，同时演示在迭代中访问元数据
            if (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    // 通过列索引读取当前行数据，列名来自元数据
                    System.out.printf("%-15s", rs.getString(i));
                }
                System.out.println();
            }

            // 验证 4 列的列名（H2 默认返回大写列名）
            assertEquals("ID", meta.getColumnName(1));
            assertEquals("NAME", meta.getColumnName(2));
            assertEquals("PRICE", meta.getColumnName(3));
            assertEquals("IN_STOCK", meta.getColumnName(4));
        }
    }
    // --8<-- [end:result_set_metadata_in_rs]

    // --8<-- [start:scrollable_result_set]
    /**
     * 演示可滚动 ResultSet：TYPE_SCROLL_INSENSITIVE + CONCUR_READ_ONLY
     * 支持 last()、first()、absolute()、relative() 等导航方法
     */
    @Test
    void testScrollableResultSet() throws SQLException {
        try (Statement stmt = conn.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,  // 可滚动，对数据变更不敏感
                ResultSet.CONCUR_READ_ONLY);        // 只读
             ResultSet rs = stmt.executeQuery(
                     "SELECT * FROM products ORDER BY id")) {

            // 移动到最后一行
            rs.last();
            assertEquals(5, rs.getInt("id"), "最后一行的 id 应为 5");
            System.out.println("last() -> id=" + rs.getInt("id")
                    + ", name=" + rs.getString("name"));

            // 移动到第一行
            rs.first();
            assertEquals(1, rs.getInt("id"), "第一行的 id 应为 1");
            System.out.println("first() -> id=" + rs.getInt("id")
                    + ", name=" + rs.getString("name"));

            // absolute(n): 移动到第 n 行（从 1 开始）
            rs.absolute(3);
            assertEquals(3, rs.getInt("id"), "第 3 行的 id 应为 3");
            System.out.println("absolute(3) -> id=" + rs.getInt("id")
                    + ", name=" + rs.getString("name"));

            // relative(n): 从当前位置相对移动 n 行
            rs.relative(-1); // 当前第3行，向前移1行到第2行
            assertEquals(2, rs.getInt("id"), "relative(-1) 后应在第 2 行");
            System.out.println("relative(-1) -> id=" + rs.getInt("id")
                    + ", name=" + rs.getString("name"));

            rs.relative(2); // 当前第2行，向后移2行到第4行
            assertEquals(4, rs.getInt("id"), "relative(2) 后应在第 4 行");
            System.out.println("relative(2) -> id=" + rs.getInt("id")
                    + ", name=" + rs.getString("name"));
        }
    }
    // --8<-- [end:scrollable_result_set]

    // --8<-- [start:teardown]
    /**
     * 每个测试后：删除表并关闭连接
     */
    @AfterEach
    void tearDown() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS products");
            }
            conn.close();
        }
    }
    // --8<-- [end:teardown]
}
