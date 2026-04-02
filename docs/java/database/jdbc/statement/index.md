---
title: Statement 与 CRUD
description: JDBC Statement 执行 SQL 语句，包括 executeQuery/executeUpdate/execute 三种方法、SQL 注入风险、自增主键获取
---

# Statement 与 CRUD

### 三种执行方法对比

| 方法 | 返回值 | 适用场景 |
|------|--------|---------|
| `executeQuery(sql)` | `ResultSet` | 仅用于 `SELECT` 查询 |
| `executeUpdate(sql)` | `int`（受影响行数） | `INSERT` / `UPDATE` / `DELETE` / DDL |
| `execute(sql)` | `boolean`（`true` 表示有 ResultSet） | 不确定 SQL 类型时的通用方法 |

### 初始化测试数据

``` java title="建表并插入初始数据（@BeforeEach）"
--8<-- "code/java/database/jdbc-statement/src/test/java/com/luguosong/jdbc/StatementTest.java:setup"
```

### executeQuery 查询

``` java title="executeQuery 查询所有用户"
--8<-- "code/java/database/jdbc-statement/src/test/java/com/luguosong/jdbc/StatementTest.java:execute_query"
```

### executeUpdate 插入

``` java title="executeUpdate 执行 INSERT"
--8<-- "code/java/database/jdbc-statement/src/test/java/com/luguosong/jdbc/StatementTest.java:execute_update_insert"
```

### executeUpdate 删除

``` java title="executeUpdate 执行 DELETE"
--8<-- "code/java/database/jdbc-statement/src/test/java/com/luguosong/jdbc/StatementTest.java:execute_update_delete"
```

### execute 通用方法

``` java title="execute() 处理 SELECT 和 UPDATE 两种情况"
--8<-- "code/java/database/jdbc-statement/src/test/java/com/luguosong/jdbc/StatementTest.java:execute_generic"
```

### SQL 注入风险演示

!!! warning "SQL 注入危险"
    使用 `Statement` 拼接用户输入会导致严重的安全漏洞。攻击者可通过精心构造的输入绕过认证、读取全表数据，甚至删除数据库。

``` java title="SQL 注入风险演示——直接拼接用户输入"
--8<-- "code/java/database/jdbc-statement/src/test/java/com/luguosong/jdbc/StatementTest.java:sql_injection_demo"
```

### 获取自增主键

INSERT 语句执行后，经常需要立即获取新记录的自增主键 id（例如插入订单后需要 orderId 用于后续关联）。JDBC 通过 `Statement.RETURN_GENERATED_KEYS` 标志和 `getGeneratedKeys()` 方法实现此功能。

``` java title="executeUpdate 传入 RETURN_GENERATED_KEYS，插入后读取自增主键"
--8<-- "code/java/database/jdbc-statement/src/test/java/com/luguosong/jdbc/StatementTest.java:generated_keys"
```

!!! tip "PreparedStatement 同样支持"
    `PreparedStatement` 也支持获取自增主键，在 `prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)` 时传入标志即可，用法完全一致。
