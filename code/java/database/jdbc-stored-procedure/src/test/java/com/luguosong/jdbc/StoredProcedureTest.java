package com.luguosong.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 演示 JDBC CallableStatement 调用存储过程
 *
 * <p>H2 数据库使用 CREATE ALIAS 语法创建存储过程/函数，
 * 与 MySQL、Oracle 等真实数据库的存储过程语法不同，
 * 但 CallableStatement 的调用方式是一致的。</p>
 */
@DisplayName("JDBC 存储过程调用演示")
class StoredProcedureTest {

    private static final String URL = "jdbc:h2:mem:testdb_sp;DB_CLOSE_DELAY=-1";

    @BeforeEach
    void setUp() throws SQLException {
        // 每个测试前清理并重建数据库
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP ALIAS IF EXISTS greet");
            stmt.execute("DROP ALIAS IF EXISTS get_status");
            stmt.execute("DROP ALIAS IF EXISTS modify_value");
        }
    }

    // --8<-- [start:in_params]
    /**
     * 演示带 IN 参数的存储函数调用
     *
     * <p>使用 {@code {? = call function_name(?)}} 语法调用有返回值的存储函数。
     * 第一个 {@code ?} 是返回值占位符，需要通过 registerOutParameter 注册。</p>
     */
    @Test
    @DisplayName("IN 参数：调用有返回值的存储函数")
    void testInParams() throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL)) {
            // 创建一个简单的存储函数：接收姓名，返回问候语
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE ALIAS greet AS $$\n"
                        + "String greet(String name) { return \"Hello, \" + name; }\n"
                        + "$$");
            }

            // 使用 CallableStatement 调用存储函数
            // ? = call greet(?) — 第一个 ? 是返回值，第二个 ? 是 IN 参数
            try (CallableStatement cs = conn.prepareCall("{? = call greet(?)}")) {
                // 注册返回值参数（索引 1）
                cs.registerOutParameter(1, Types.VARCHAR);
                // 设置 IN 参数（索引 2）
                cs.setString(2, "World");

                // 执行存储函数调用
                cs.execute();

                // 获取返回值
                String result = cs.getString(1);
                System.out.println("存储函数返回值: " + result);

                assertEquals("Hello, World", result, "应返回问候语");
            }
        }
    }
    // --8<-- [end:in_params]

    // --8<-- [start:out_params]
    /**
     * 演示带 OUT 参数的存储过程调用
     *
     * <p>使用 {@code {call procedure_name(?, ?)}} 语法调用存储过程。
     * OUT 参数需要通过 registerOutParameter 注册类型，执行后通过 getString 获取值。</p>
     *
     * <p>注意：H2 数据库没有原生 OUT 参数支持。这里使用返回 {@link ResultSet} 的函数来模拟
     * 存储过程的输出行为。在真实数据库（MySQL、Oracle）中，OUT 参数是原生支持的：</p>
     * <pre>
     * // MySQL 原生 OUT 参数示例
     * CREATE PROCEDURE get_status(IN input VARCHAR(50), OUT status VARCHAR(100))
     * BEGIN
     *     SET status = CONCAT('OK: ', input);
     * END
     * // JDBC 调用
     * cs.registerOutParameter(2, Types.VARCHAR);
     * cs.setString(1, "test");
     * cs.execute();
     * String result = cs.getString(2);
     * </pre>
     */
    @Test
    @DisplayName("OUT 参数：模拟带输出参数的存储过程")
    void testOutParams() throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL)) {
            // H2 中使用返回 ResultSet 的函数模拟 OUT 参数
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE ALIAS get_status AS $$\n"
                        + "ResultSet getStatus(Connection conn, String input) throws SQLException {\n"
                        + "    return conn.createStatement().executeQuery(\n"
                        + "        \"SELECT 'OK: \" + input + \"' AS status\");\n"
                        + "}\n"
                        + "$$");
            }

            // 使用 CallableStatement 调用
            try (CallableStatement cs = conn.prepareCall("{call get_status(?)}")) {
                // 设置 IN 参数
                cs.setString(1, "test");

                // 执行并获取返回的 ResultSet
                try (ResultSet rs = cs.executeQuery()) {
                    assertTrue(rs.next(), "应返回一行结果");
                    String status = rs.getString("status");
                    System.out.println("OUT 参数模拟返回的状态: " + status);

                    assertEquals("OK: test", status, "应返回包含输入的状态信息");
                }
            }
        }
    }
    // --8<-- [end:out_params]

    // --8<-- [start:inout_params]
    /**
     * 演示 INOUT 参数（输入输出参数）
     *
     * <p>INOUT 参数同时承担输入和输出角色：调用前设置值，执行后获取修改后的值。
     * 在 MySQL 中用 INOUT 关键字声明，Oracle 中同样支持。</p>
     *
     * <p>H2 数据库没有原生 INOUT 参数支持，这里使用返回 {@link ResultSet} 的函数来模拟。
     * 在真实数据库中的用法：</p>
     * <pre>
     * // MySQL 原生 INOUT 参数示例
     * CREATE PROCEDURE modify_value(INOUT val VARCHAR(100), IN suffix VARCHAR(50))
     * BEGIN
     *     SET val = CONCAT(val, ' ', suffix);
     * END
     * // JDBC 调用
     * cs.setString(1, "Hello");      // 设置 INOUT 初始值
     * cs.setString(2, "World");       // 设置 IN 参数
     * cs.registerOutParameter(1, Types.VARCHAR);  // 注册 INOUT 输出类型
     * cs.execute();
     * String result = cs.getString(1);  // 获取修改后的值
     * </pre>
     */
    @Test
    @DisplayName("INOUT 参数：模拟输入输出参数的存储过程")
    void testInOutParams() throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL)) {
            // H2 中使用返回 ResultSet 的函数模拟 INOUT 参数
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE ALIAS modify_value AS $$\n"
                        + "ResultSet modifyValue(Connection conn, String initialValue, String suffix) throws SQLException {\n"
                        + "    String modified = initialValue + \" \" + suffix;\n"
                        + "    return conn.createStatement().executeQuery(\n"
                        + "        \"SELECT '\" + modified + \"' AS modified_value\");\n"
                        + "}\n"
                        + "$$");
            }

            // 使用 CallableStatement 调用
            try (CallableStatement cs = conn.prepareCall("{call modify_value(?, ?)}")) {
                // 设置 INOUT 参数的初始值（索引 1）
                cs.setString(1, "Hello");
                // 设置 IN 参数（索引 2）
                cs.setString(2, "World");

                // 执行并获取模拟的 INOUT 返回值
                try (ResultSet rs = cs.executeQuery()) {
                    assertTrue(rs.next(), "应返回一行结果");
                    String modifiedValue = rs.getString("modified_value");
                    System.out.println("INOUT 参数修改后的值: " + modifiedValue);

                    assertEquals("Hello World", modifiedValue, "INOUT 参数应被追加后缀");
                }
            }
        }
    }
    // --8<-- [end:inout_params]

    // --8<-- [start:callable_syntax]
    /**
     * CallableStatement 语法总结
     *
     * <p>JDBC 通过 Connection.prepareCall() 创建 CallableStatement，
     * 使用标准转义语法调用存储过程和函数：</p>
     *
     * <pre>
     * // 1. 调用无返回值的存储过程
     * {call procedure_name(?, ?, ?)}
     *
     * // 2. 调用有返回值的存储函数（第一个 ? 是返回值）
     * {? = call function_name(?, ?)}
     *
     * // 3. 调用无参数的存储过程
     * {call procedure_name}
     * </pre>
     *
     * <p>完整流程：
     * <pre>
     * try (CallableStatement cs = conn.prepareCall("{call proc(?, ?)}")) {
     *     // 1. 注册 OUT 参数类型
     *     cs.registerOutParameter(2, Types.VARCHAR);
     *     // 2. 设置 IN / INOUT 参数值
     *     cs.setString(1, "input");
     *     // 3. 执行
     *     cs.execute();
     *     // 4. 获取 OUT 参数值
     *     String result = cs.getString(2);
     * }
     * </pre>
     * </p>
     */
    @Test
    @DisplayName("语法总结：CallableStatement 调用模式")
    void testCallableSyntaxSummary() {
        // 此测试仅作为语法参考文档，验证 CallableStatement 的基本模式
        // 实际数据库（MySQL、Oracle）中的存储过程创建语法如下：
        //
        // MySQL 示例：
        //   CREATE PROCEDURE add_user(IN name VARCHAR(50), OUT id INT)
        //   BEGIN
        //       INSERT INTO users(name) VALUES(name);
        //       SET id = LAST_INSERT_ID();
        //   END
        //
        // Oracle 示例：
        //   CREATE PROCEDURE get_dept_name(p_dept_id IN NUMBER, p_name OUT VARCHAR2)
        //   AS
        //   BEGIN
        //       SELECT dept_name INTO p_name FROM dept WHERE dept_id = p_dept_id;
        //   END
        //
        // 调用方式统一使用 JDBC 转义语法：
        //   {call add_user(?, ?)}
        //   {call get_dept_name(?, ?)}

        System.out.println("CallableStatement 语法总结测试通过");
        assertTrue(true, "语法总结测试");
    }
    // --8<-- [end:callable_syntax]
}
