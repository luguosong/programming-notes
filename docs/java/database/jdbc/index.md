# JDBC

JDBC（Java Database Connectivity）是 Java 标准 API，用于在 Java 程序中以统一方式连接和操作各种关系型数据库。

---

## 第 1 章：JDBC 概述

### 技术栈位置

JDBC 充当 Java 应用程序与底层数据库驱动之间的标准抽象层，屏蔽了不同数据库的实现差异。

``` mermaid
graph TB
    A[Java 应用程序] --> B[JDBC API\njava.sql / javax.sql]
    B --> C[JDBC Driver\n各数据库厂商提供]
    C --> D[(关系型数据库\nMySQL / PostgreSQL / Oracle / H2…)]

    style A fill:#4A90D9,color:#fff
    style B fill:#7B68EE,color:#fff
    style C fill:#20B2AA,color:#fff
    style D fill:#CD853F,color:#fff
```

### 核心接口

| 接口 / 类 | 所在包 | 职责说明 |
|-----------|--------|---------|
| `DriverManager` | `java.sql` | 管理 JDBC 驱动，提供 `getConnection()` 获取数据库连接 |
| `Connection` | `java.sql` | 代表一个数据库连接，是所有操作的入口 |
| `Statement` | `java.sql` | 执行静态 SQL 语句 |
| `PreparedStatement` | `java.sql` | 执行预编译的参数化 SQL，防 SQL 注入 |
| `ResultSet` | `java.sql` | 保存查询结果，提供游标逐行读取数据 |
| `DataSource` | `javax.sql` | 连接池的标准接口，生产环境推荐使用 |

---

## 第 2 章：建立数据库连接

### DriverManager.getConnection() 三种重载方式

JDBC 提供三种方式通过 `DriverManager` 获取连接。

**方式一：`url + user + password`（最常用）**

```java title="三参数方式建立连接"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/ConnectionTest.java:driver_manager_connect"
```

**方式二：仅 URL（将认证信息编码在 URL 中）**

```java title="单参数 URL 方式建立连接"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/ConnectionTest.java:connection_url_only"
```

**方式三：`Properties` 对象传参**

```java title="通过 Properties 对象传入认证信息"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/ConnectionTest.java:connection_properties"
```

### JDBC URL 格式

JDBC URL 由三部分组成：

```
jdbc:<子协议>:<子名称>
```

| 数据库 | URL 示例 |
|--------|---------|
| H2 内存库 | `jdbc:h2:mem:testdb` |
| MySQL | `jdbc:mysql://localhost:3306/mydb` |
| PostgreSQL | `jdbc:postgresql://localhost:5432/mydb` |
| Oracle | `jdbc:oracle:thin:@localhost:1521:orcl` |

### try-with-resources 最佳实践

`Connection` 实现了 `AutoCloseable` 接口，使用 try-with-resources 可确保连接在离开作用域后自动关闭，无需手动调用 `close()`。

```java title="try-with-resources 自动关闭连接"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/ConnectionTest.java:try_with_resources"
```

!!! warning "务必关闭连接"
    数据库连接属于稀缺资源。未关闭的连接会持续占用数据库服务器端的槽位，最终导致连接耗尽（`Too many connections` 错误）。
    **始终使用 try-with-resources 包裹 `Connection`、`Statement`、`ResultSet`。**

---

## 第 3 章：Statement 与 CRUD

### 三种执行方法对比

| 方法 | 返回值 | 适用场景 |
|------|--------|---------|
| `executeQuery(sql)` | `ResultSet` | 仅用于 `SELECT` 查询 |
| `executeUpdate(sql)` | `int`（受影响行数） | `INSERT` / `UPDATE` / `DELETE` / DDL |
| `execute(sql)` | `boolean`（`true` 表示有 ResultSet） | 不确定 SQL 类型时的通用方法 |

### 初始化测试数据

```java title="建表并插入初始数据（@BeforeEach）"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/StatementTest.java:setup"
```

### executeQuery 查询

```java title="executeQuery 查询所有用户"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/StatementTest.java:execute_query"
```

### executeUpdate 插入

```java title="executeUpdate 执行 INSERT"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/StatementTest.java:execute_update_insert"
```

### executeUpdate 删除

```java title="executeUpdate 执行 DELETE"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/StatementTest.java:execute_update_delete"
```

### execute 通用方法

```java title="execute() 处理 SELECT 和 UPDATE 两种情况"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/StatementTest.java:execute_generic"
```

### SQL 注入风险演示

!!! warning "SQL 注入危险"
    使用 `Statement` 拼接用户输入会导致严重的安全漏洞。攻击者可通过精心构造的输入绕过认证、读取全表数据，甚至删除数据库。

```java title="SQL 注入风险演示——直接拼接用户输入"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/StatementTest.java:sql_injection_demo"
```

---

## 第 4 章：ResultSet 数据读取

### 游标概念

`ResultSet` 内部维护一个**游标**，初始位置在第一行之前。调用 `next()` 将游标前移一行，返回 `true` 表示当前行有数据，`false` 表示已越过最后一行。

```
初始位置
    ↓
[before first] → [row 1] → [row 2] → [row 3] → [after last]
                    ↑
                  next() 调用后游标所在位置
```

### 向前遍历（按列名取值）

```java title="next() 遍历 ResultSet，按列名获取各类型数据"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/ResultSetTest.java:iterate_forward"
```

### 按列索引取值

```java title="按列索引（1-based）与按列名两种方式对比"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/ResultSetTest.java:get_by_index"
```

!!! note "按索引 vs 按列名"
    - 按列名（`rs.getString("name")`）：可读性好，不受 SELECT 列顺序变化影响，**推荐**
    - 按列索引（`rs.getString(2)`）：性能略高，但列顺序变化后容易出错

### ResultSetMetaData：在遍历中动态获取列信息

```java title="遍历结果集时同步读取列元数据"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/ResultSetTest.java:result_set_metadata_in_rs"
```

### 可滚动 ResultSet

默认 `ResultSet` 只能向前遍历（`TYPE_FORWARD_ONLY`）。通过 `createStatement` 时指定类型，可获得支持双向滚动的 `ResultSet`。

```java title="可滚动 ResultSet：last()、first()、absolute()、relative()"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/ResultSetTest.java:scrollable_result_set"
```

| 导航方法 | 说明 |
|---------|------|
| `first()` | 移动到第一行 |
| `last()` | 移动到最后一行 |
| `absolute(n)` | 移动到第 n 行（从 1 计数） |
| `relative(n)` | 从当前行相对移动 n 行（正数向后，负数向前） |
| `beforeFirst()` | 移动到第一行之前（初始位置） |
| `afterLast()` | 移动到最后一行之后 |

---

## 第 5 章：PreparedStatement 参数化查询

### 为什么要用 PreparedStatement？

1. **防 SQL 注入**：参数通过占位符 `?` 绑定，驱动对参数值进行转义，彻底防止注入攻击
2. **预编译性能**：SQL 结构只解析、编译一次，多次执行只需替换参数值，减少数据库解析开销

### 参数化查询

```java title="PreparedStatement 带 ? 占位符查询"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/PreparedStatementTest.java:parameterized_query"
```

### 参数化插入

```java title="PreparedStatement 参数化插入，绑定多种类型参数"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/PreparedStatementTest.java:parameterized_insert"
```

### 参数化更新

```java title="PreparedStatement 参数化 UPDATE"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/PreparedStatementTest.java:parameterized_update"
```

### 防 SQL 注入对比验证

```java title="Statement 注入成功 vs PreparedStatement 注入失败"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/PreparedStatementTest.java:prevent_sql_injection"
```

### 预编译复用

同一个 `PreparedStatement` 对象可通过重新绑定参数多次执行，SQL 只编译一次。

```java title="复用同一 PreparedStatement 对象执行多次查询"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/PreparedStatementTest.java:reuse_prepared_statement"
```

!!! tip "优先使用 PreparedStatement"
    任何含用户输入的 SQL 都应使用 `PreparedStatement`。即使是内部系统无注入风险的场景，预编译带来的性能提升和代码可读性提升也值得优先选择 `PreparedStatement`。

---

## 第 6 章：事务管理

### ACID 简介

| 特性 | 全称 | 含义 |
|------|------|------|
| **A** | Atomicity（原子性） | 事务内所有操作要么全部成功，要么全部回滚 |
| **C** | Consistency（一致性） | 事务前后数据满足所有完整性约束 |
| **I** | Isolation（隔离性） | 并发事务之间互不干扰 |
| **D** | Durability（持久性） | 事务提交后数据永久保存 |

### autoCommit 默认行为

JDBC 连接默认 `autoCommit = true`，即每条 SQL 语句执行后立即自动提交，无需手动调用 `commit()`。

```java title="autoCommit 默认为 true，每条 SQL 自动提交"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/TransactionTest.java:auto_commit_default"
```

### 手动事务提交

```java title="关闭 autoCommit，手动管理转账事务"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/TransactionTest.java:manual_transaction_commit"
```

### 事务回滚

```java title="模拟异常，rollback() 撤销所有未提交操作"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/TransactionTest.java:manual_transaction_rollback"
```

### Savepoint 保存点

`Savepoint` 允许在事务内设置中间检查点，可回滚到某个保存点而不必撤销整个事务。

```java title="设置多个保存点，回滚到中间某个保存点"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/TransactionTest.java:savepoint"
```

### 查看当前隔离级别

```java title="读取连接的事务隔离级别"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/TransactionTest.java:check_isolation_level"
```

### 事务隔离级别对比

| 隔离级别 | 脏读 | 不可重复读 | 幻读 | 说明 |
|---------|------|-----------|------|------|
| `READ_UNCOMMITTED` | ✅ 可能 | ✅ 可能 | ✅ 可能 | 最低隔离，可读未提交数据 |
| `READ_COMMITTED` | ❌ 不会 | ✅ 可能 | ✅ 可能 | 多数数据库默认级别 |
| `REPEATABLE_READ` | ❌ 不会 | ❌ 不会 | ✅ 可能[^1] | MySQL InnoDB 默认级别 |
| `SERIALIZABLE` | ❌ 不会 | ❌ 不会 | ❌ 不会 | 最高隔离，并发性能最低 |

[^1]: MySQL InnoDB 通过 MVCC（多版本并发控制）和 Next-Key Lock，在 `REPEATABLE_READ` 级别下已在大多数场景中防止了幻读，与 SQL 标准的理论描述有所不同。

---

## 第 7 章：批处理

### 为什么要用批处理？

每次执行单条 SQL 都会经历：**Java → 网络 → 数据库解析 → 执行 → 网络 → Java** 的完整往返。批处理将多条 SQL 打包一次性发送，显著减少网络往返次数，大幅提升大批量写入性能。

### Statement 批处理

```java title="Statement.addBatch() + executeBatch() 批量执行"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/BatchProcessingTest.java:statement_batch"
```

### PreparedStatement 批处理

```java title="PreparedStatement 批处理：SQL 预编译一次，参数绑定多次"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/BatchProcessingTest.java:prepared_statement_batch"
```

### 批处理 + 事务（最佳实践）

将批处理放在事务中执行，是大批量写入的最佳实践：既减少网络往返次数，又保证原子性；出错时可完整回滚。

```java title="批处理 + 事务结合，每批执行后统一提交"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/BatchProcessingTest.java:batch_with_transaction"
```

---

## 第 8 章：数据库元数据

JDBC 提供两类元数据接口：

| 接口 | 获取方式 | 描述 |
|------|---------|------|
| `DatabaseMetaData` | `conn.getMetaData()` | 数据库级别信息：产品名、版本、驱动信息、表结构等 |
| `ResultSetMetaData` | `rs.getMetaData()` | 查询结果级别信息：列数、列名、列类型等 |

### DatabaseMetaData：数据库基本信息

```java title="获取数据库产品名称、版本、驱动信息"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/MetaDataTest.java:database_metadata"
```

### DatabaseMetaData：枚举数据库中的表

```java title="通过 getTables() 列出数据库中所有表"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/MetaDataTest.java:database_metadata_tables"
```

### ResultSetMetaData：查询结果的列信息

```java title="从 ResultSetMetaData 获取列数、列名、列类型"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/MetaDataTest.java:resultset_metadata"
```

!!! note "元数据的典型应用场景"
    - 通用数据库工具（如 JDBC 客户端）动态渲染表格列头
    - ORM 框架在运行时反射映射列名与字段名
    - 数据库迁移工具获取表结构信息

---

## 第 9 章：连接池

### 为什么需要连接池？

每次通过 `DriverManager.getConnection()` 建立物理连接都需要完成 TCP 握手、数据库认证等开销，耗时通常在几毫秒到数十毫秒之间。高并发下频繁创建/销毁连接会成为瓶颈。

``` mermaid
sequenceDiagram
    participant App as Java 应用
    participant DB as 数据库

    Note over App,DB: 无连接池（每次请求建立新连接）
    App->>DB: TCP 握手 + 认证（每次都有）
    DB-->>App: 连接建立（耗时几ms~几十ms）
    App->>DB: 执行 SQL
    DB-->>App: 返回结果
    App->>DB: 关闭连接（物理断开）

    Note over App,DB: 有连接池（HikariCP）
    App->>App: 从池中取出已有连接（微秒级）
    App->>DB: 执行 SQL
    DB-->>App: 返回结果
    App->>App: 归还连接到池（不销毁）
```

### HikariCP 基本配置

[HikariCP](https://github.com/brettwooldridge/HikariCP) 是目前 Java 生态中性能最优的连接池实现，也是 Spring Boot 的默认连接池。

```java title="HikariCP 连接池创建与基本参数配置"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/ConnectionPoolTest.java:hikari_basic_config"
```

### 连接池 vs 直连对比

```java title="DriverManager 直连 vs HikariCP 连接池使用方式对比"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/ConnectionPoolTest.java:pool_vs_direct"
```

### DataSource 标准接口

`DataSource` 是 `javax.sql` 包中的标准接口，生产代码应面向此接口编程，而非直接依赖 `HikariDataSource`。

```java title="通过 DataSource 接口获取连接，连接用后自动归还池"
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/ConnectionPoolTest.java:datasource_get_connection"
```

---

## 第 10 章：最佳实践

| 原则 | 说明 |
|------|------|
| 使用 try-with-resources | 确保 `Connection` / `Statement` / `ResultSet` 自动关闭，防止连接泄漏 |
| 优先使用 PreparedStatement | 防 SQL 注入，同时提升预编译性能，即使无注入风险也推荐 |
| 关闭 autoCommit 管理事务 | 涉及多步操作时手动控制事务，确保原子性，异常时完整回滚 |
| 使用连接池（DataSource） | 避免频繁创建/销毁物理连接的开销，生产环境必选 HikariCP / Druid 等 |
| 参数化批处理大批量写入 | 使用 `PreparedStatement` + `addBatch()` + 事务，减少网络往返次数 |
| 不要硬编码 SQL | 将 SQL 提取为常量或通过 MyBatis / JPA 等 ORM 框架管理，提高可维护性 |
| 按列名而非索引读取数据 | `rs.getString("name")` 比 `rs.getString(2)` 更健壮，不受列顺序变化影响 |
| 用 DataSource 接口解耦 | 业务代码依赖 `javax.sql.DataSource` 接口，方便切换连接池实现 |
