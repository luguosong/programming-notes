---
title: 事务管理
description: JDBC 事务 ACID 特性、autoCommit、手动提交/回滚、Savepoint 保存点、隔离级别对比
---

# 事务管理

### ACID 简介

| 特性 | 全称 | 含义 |
|------|------|------|
| `A` | Atomicity（原子性） | 事务内所有操作要么全部成功，要么全部回滚 |
| `C` | Consistency（一致性） | 事务前后数据满足所有完整性约束 |
| `I` | Isolation（隔离性） | 并发事务之间互不干扰 |
| `D` | Durability（持久性） | 事务提交后数据永久保存 |

### autoCommit 默认行为

JDBC 连接默认 `autoCommit = true`，即每条 SQL 语句执行后立即自动提交，无需手动调用 `commit()`。

``` java title="autoCommit 默认为 true，每条 SQL 自动提交"
--8<-- "code/java/database/jdbc-transaction-batch/src/test/java/com/luguosong/jdbc/TransactionTest.java:auto_commit_default"
```

### 手动事务提交

``` java title="关闭 autoCommit，手动管理转账事务"
--8<-- "code/java/database/jdbc-transaction-batch/src/test/java/com/luguosong/jdbc/TransactionTest.java:manual_transaction_commit"
```

### 事务回滚

``` java title="模拟异常，rollback() 撤销所有未提交操作"
--8<-- "code/java/database/jdbc-transaction-batch/src/test/java/com/luguosong/jdbc/TransactionTest.java:manual_transaction_rollback"
```

### Savepoint 保存点

`Savepoint` 允许在事务内设置中间检查点，可回滚到某个保存点而不必撤销整个事务。

``` java title="设置多个保存点，回滚到中间某个保存点"
--8<-- "code/java/database/jdbc-transaction-batch/src/test/java/com/luguosong/jdbc/TransactionTest.java:savepoint"
```

### 查看当前隔离级别

``` java title="读取连接的事务隔离级别"
--8<-- "code/java/database/jdbc-transaction-batch/src/test/java/com/luguosong/jdbc/TransactionTest.java:check_isolation_level"
```

### 事务隔离级别对比

| 隔离级别 | 脏读 | 不可重复读 | 幻读 | 说明 |
|---------|------|-----------|------|------|
| `READ_UNCOMMITTED` | ✅ 可能 | ✅ 可能 | ✅ 可能 | 最低隔离，可读未提交数据 |
| `READ_COMMITTED` | ❌ 不会 | ✅ 可能 | ✅ 可能 | 多数数据库默认级别 |
| `REPEATABLE_READ` | ❌ 不会 | ❌ 不会 | ✅ 可能[^1] | MySQL InnoDB 默认级别 |
| `SERIALIZABLE` | ❌ 不会 | ❌ 不会 | ❌ 不会 | 最高隔离，并发性能最低 |

[^1]: MySQL InnoDB 通过 MVCC（多版本并发控制）和 Next-Key Lock，在 `REPEATABLE_READ` 级别下已在大多数场景中防止了幻读，与 SQL 标准的理论描述有所不同。
