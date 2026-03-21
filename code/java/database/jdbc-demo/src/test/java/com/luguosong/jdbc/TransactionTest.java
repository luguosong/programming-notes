package com.luguosong.jdbc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 演示事务管理
 */
class TransactionTest {

    private static final String URL = "jdbc:h2:mem:testdb_tx;DB_CLOSE_DELAY=-1";
    private Connection conn;

    // --8<-- [start:setup]
    /**
     * 每个测试前：创建 accounts 表并插入 Alice 和 Bob
     */
    @BeforeEach
    void setUp() throws SQLException {
        conn = DriverManager.getConnection(URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS accounts ("
                    + "id INT, "
                    + "owner VARCHAR(50), "
                    + "balance DOUBLE)");
            stmt.executeUpdate("INSERT INTO accounts VALUES (1, 'Alice', 1000.0)");
            stmt.executeUpdate("INSERT INTO accounts VALUES (2, 'Bob', 500.0)");
        }
    }
    // --8<-- [end:setup]

    // --8<-- [start:auto_commit_default]
    /**
     * 演示 autoCommit 默认为 true，每个操作自动提交
     */
    @Test
    void testAutoCommitDefault() throws SQLException {
        // 默认情况下 autoCommit 为 true
        assertTrue(conn.getAutoCommit(), "默认 autoCommit 应为 true");
        System.out.println("autoCommit 默认值: " + conn.getAutoCommit());

        // 在 autoCommit=true 模式下，每条 SQL 自动提交
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("UPDATE accounts SET balance = 900.0 WHERE id = 1");
            // 此时更新已经自动提交，即使不调用 commit()

            // 验证更新已生效
            ResultSet rs = stmt.executeQuery("SELECT balance FROM accounts WHERE id = 1");
            rs.next();
            assertEquals(900.0, rs.getDouble("balance"), 0.01,
                    "autoCommit 模式下更新应立即生效");
            System.out.println("autoCommit 模式: Alice 余额已自动更新为 " + rs.getDouble("balance"));
        }
    }
    // --8<-- [end:auto_commit_default]

    // --8<-- [start:manual_transaction_commit]
    /**
     * 演示手动事务：转账操作（从 Alice 扣 200，给 Bob 加 200）
     */
    @Test
    void testManualTransactionCommit() throws SQLException {
        // 关闭自动提交，开启手动事务管理
        conn.setAutoCommit(false);
        assertFalse(conn.getAutoCommit(), "手动事务模式 autoCommit 应为 false");

        try (Statement stmt = conn.createStatement()) {
            // 转账操作：Alice -> Bob 转 200
            stmt.executeUpdate("UPDATE accounts SET balance = balance - 200 WHERE id = 1");
            stmt.executeUpdate("UPDATE accounts SET balance = balance + 200 WHERE id = 2");

            // 手动提交事务
            conn.commit();
            System.out.println("转账事务已提交");

            // 验证转账结果
            ResultSet rs = stmt.executeQuery(
                    "SELECT owner, balance FROM accounts ORDER BY id");
            rs.next();
            assertEquals(800.0, rs.getDouble("balance"), 0.01,
                    "Alice 余额应为 800（1000-200）");
            System.out.println(rs.getString("owner") + " 余额: " + rs.getDouble("balance"));

            rs.next();
            assertEquals(700.0, rs.getDouble("balance"), 0.01,
                    "Bob 余额应为 700（500+200）");
            System.out.println(rs.getString("owner") + " 余额: " + rs.getDouble("balance"));
        }
    }
    // --8<-- [end:manual_transaction_commit]

    // --8<-- [start:manual_transaction_rollback]
    /**
     * 演示事务回滚：转账到一半模拟异常，回滚保证数据一致性
     */
    @Test
    void testManualTransactionRollback() throws SQLException {
        conn.setAutoCommit(false);

        try (Statement stmt = conn.createStatement()) {
            // 第一步：从 Alice 扣 300
            stmt.executeUpdate("UPDATE accounts SET balance = balance - 300 WHERE id = 1");

            // 模拟异常发生（例如系统错误），此时 Bob 还未加钱
            // 为保证数据一致性，必须回滚
            boolean simulatedError = true;
            if (simulatedError) {
                // 回滚事务：撤销所有未提交的更改
                conn.rollback();
                System.out.println("模拟异常，事务已回滚");
            }

            // 验证回滚后 Alice 余额未变
            conn.setAutoCommit(true); // 恢复自动提交以便查询
            ResultSet rs = stmt.executeQuery(
                    "SELECT balance FROM accounts WHERE id = 1");
            rs.next();
            assertEquals(1000.0, rs.getDouble("balance"), 0.01,
                    "回滚后 Alice 余额应恢复为 1000（未变）");
            System.out.println("回滚后 Alice 余额: " + rs.getDouble("balance"));
        }
    }
    // --8<-- [end:manual_transaction_rollback]

    // --8<-- [start:savepoint]
    /**
     * 演示 Savepoint：设置多个保存点，回滚到中间某个保存点
     */
    @Test
    void testSavepoint() throws SQLException {
        conn.setAutoCommit(false);

        try (Statement stmt = conn.createStatement()) {
            // 操作1：Alice 余额 -100 (1000 -> 900)
            stmt.executeUpdate("UPDATE accounts SET balance = balance - 100 WHERE id = 1");
            // 设置保存点 sp1
            Savepoint sp1 = conn.setSavepoint("sp1");
            System.out.println("保存点 sp1 已设置（Alice 余额应为 900）");

            // 操作2：Alice 余额再 -200 (900 -> 700)
            stmt.executeUpdate("UPDATE accounts SET balance = balance - 200 WHERE id = 1");
            // 设置保存点 sp2
            Savepoint sp2 = conn.setSavepoint("sp2");
            System.out.println("保存点 sp2 已设置（Alice 余额应为 700）");

            // 操作3：Alice 余额再 -500 (700 -> 200)
            stmt.executeUpdate("UPDATE accounts SET balance = balance - 500 WHERE id = 1");
            System.out.println("操作3 执行后（Alice 余额应为 200）");

            // 回滚到 sp1：撤销操作2和操作3，只保留操作1
            conn.rollback(sp1);
            System.out.println("回滚到 sp1");

            // 提交事务
            conn.commit();

            // 验证结果：只有操作1（-100）生效
            conn.setAutoCommit(true);
            ResultSet rs = stmt.executeQuery(
                    "SELECT balance FROM accounts WHERE id = 1");
            rs.next();
            assertEquals(900.0, rs.getDouble("balance"), 0.01,
                    "回滚到 sp1 后，Alice 余额应为 900（只有第一步 -100 生效）");
            System.out.println("最终 Alice 余额: " + rs.getDouble("balance"));
        }
    }
    // --8<-- [end:savepoint]

    // --8<-- [start:check_isolation_level]
    /**
     * 打印当前连接的事务隔离级别
     */
    @Test
    void testCheckIsolationLevel() throws SQLException {
        int level = conn.getTransactionIsolation();
        System.out.println("当前事务隔离级别（数值）: " + level);

        // 对照 Connection 中定义的常量
        String levelName;
        switch (level) {
            case Connection.TRANSACTION_NONE:
                levelName = "TRANSACTION_NONE (0)";
                break;
            case Connection.TRANSACTION_READ_UNCOMMITTED:
                levelName = "TRANSACTION_READ_UNCOMMITTED (1)";
                break;
            case Connection.TRANSACTION_READ_COMMITTED:
                levelName = "TRANSACTION_READ_COMMITTED (2)";
                break;
            case Connection.TRANSACTION_REPEATABLE_READ:
                levelName = "TRANSACTION_REPEATABLE_READ (4)";
                break;
            case Connection.TRANSACTION_SERIALIZABLE:
                levelName = "TRANSACTION_SERIALIZABLE (8)";
                break;
            default:
                levelName = "UNKNOWN (" + level + ")";
        }
        System.out.println("隔离级别名称: " + levelName);

        // H2 默认隔离级别为 READ_COMMITTED
        assertTrue(level >= Connection.TRANSACTION_NONE,
                "隔离级别应为有效值");
        assertNotEquals(Connection.TRANSACTION_NONE, level,
                "隔离级别不应为 NONE");
    }
    // --8<-- [end:check_isolation_level]

    // --8<-- [start:teardown]
    /**
     * 每个测试后：删除表并关闭连接
     */
    @AfterEach
    void tearDown() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            // 确保恢复自动提交，避免影响清理操作
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {
                // 连接可能已处于不可用状态
            }
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS accounts");
            }
            conn.close();
        }
    }
    // --8<-- [end:teardown]
}
