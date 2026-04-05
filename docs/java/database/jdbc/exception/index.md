---
title: 异常处理
description: JDBC SQLException 异常处理机制，包括异常链遍历、SQLState 诊断、SQLWarning、分类子类体系
---

# 异常处理

## 🤔 为什么需要了解 SQLException

JDBC 操作可能因多种原因失败：数据库服务不可达、认证失败、SQL 语法错误、约束违反、连接超时等。`SQLException` 不仅携带了错误描述信息，还提供了 `SQLState`（标准化的 5 字符错误码）、`ErrorCode`（数据库厂商私有码）以及异常链，使得开发者可以精确诊断问题根因，并根据错误类型采取不同的恢复策略。

## 📋 SQLException 包含的诊断信息

### 错误描述与 SQLState

`SQLException` 提供三个核心诊断方法：

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `getMessage()` | `String` | 人类可读的错误描述 |
| `getSQLState()` | `String` | 5 字符的 SQLState 错误码，遵循 X/Open 标准约定 |
| `getErrorCode()` | `int` | 数据库厂商私有的错误码（如 MySQL 的错误编号） |

`SQLState` 的前 2 位是类别码，标识错误的大类，后 3 位是子类码。同一个 `SQLState` 在不同数据库之间具有一致的语义，因此可用于编写与数据库无关的错误处理逻辑。

### 异常链遍历

数据库操作失败时，底层可能产生多个关联异常（例如驱动层异常 + 数据库层异常）。`SQLException` 通过 `getNextException()` 将它们串联成链，开发者需逐一遍历才能获取完整的错误信息。

``` java title="异常链遍历：getNextException()"
--8<-- "code/java/database/jdbc-exception/src/test/java/com/luguosong/jdbc/ExceptionTest.java:exception_chain"
```

### 因果链遍历

除了 `getNextException()`，`SQLException` 也支持 Java 标准的 `getCause()` 因果链。两者的区别：

| 方法 | 所属体系 | 用途 |
|------|---------|------|
| `getNextException()` | JDBC 特有 | 链接同类型的多层 `SQLException`，如驱动异常 + 数据库返回的异常 |
| `getCause()` | Java 标准异常链 | `initCause()` 设置，通常包装底层 `IOException` 等非 `SQLException` |

在实际排查中，建议**两种链都遍历**，确保不遗漏任何诊断信息。

## 🔍 SQLState 过滤实践

通过 `SQLState` 类别码可以区分错误严重程度，对非致命错误执行特殊处理（如自动建表、跳过已存在的索引等），而非直接向上抛出异常。

``` java title="根据 SQLState 过滤非致命异常"
--8<-- "code/java/database/jdbc-exception/src/test/java/com/luguosong/jdbc/ExceptionTest.java:sqlstate_filter"
```

常见 `SQLState` 类别码：

| 类别码 | 含义 | 典型场景 |
|--------|------|---------|
| `42` | 语法错误或访问规则错误 | 表/视图不存在（`42S02`）、列不存在 |
| `08` | 连接异常 | 数据库服务不可达（`08001`）、连接超时 |
| `23` | 完整性约束违反 | 主键冲突（`23000`）、外键约束 |
| `25` | 事务状态无效 | 事务回滚后继续操作（`25000`） |
| `HY` | 驱动/连接实现错误 | 驱动不支持的操作（如 H2 特有的 `HY000`） |

!!! tip "编码建议"
    判断 `SQLState` 时优先使用 `startsWith("42")` 按类别匹配，而非精确匹配完整的 5 字符代码。这样同一类别下的不同子错误都能被覆盖，同时保持代码的数据库兼容性。

## ⚠️ SQLWarning：非致命警告

`SQLWarning` 是 `SQLException` 的子类，表示数据库操作产生的非致命警告。与 `SQLException` 不同，警告**不会中断程序执行**，也不会被 `try-catch` 捕获，必须主动调用 `getWarnings()` 获取。

`Connection`、`Statement`、`ResultSet` 三个接口都提供了 `getWarnings()` 和 `clearWarnings()` 方法，分别对应不同层级的警告信息。警告也支持链式遍历（`getNextWarning()`），用法与异常链一致。

最常见的警告类型是 `DataTruncation`（数据截断警告），表示读取或写入数据时发生了精度丢失或截断。

``` java title="获取 Connection/Statement/ResultSet 上的 SQLWarning"
--8<-- "code/java/database/jdbc-exception/src/test/java/com/luguosong/jdbc/ExceptionTest.java:sql_warning"
```

## 🌳 SQLException 分类子类体系

JDBC 4.0（Java 6）引入了更细粒度的异常子类，按错误性质分为两大分支：

``` mermaid
graph TD
    SQLException --> SQLNonTransientException
    SQLException --> SQLTransientException
    SQLNonTransientException --> SQLSyntaxErrorException
    SQLNonTransientException --> SQLIntegrityConstraintViolationException
    SQLNonTransientException --> SQLDataException
    SQLTransientException --> SQLTimeoutException
    SQLTransientException --> SQLTransactionRollbackException
    SQLTransientException --> SQLTransientConnectionException
    SQLException --> BatchUpdateException

    classDef base fill:transparent,stroke:#e3b341,color:#adbac7,stroke-width:2px
    classDef nonTrans fill:transparent,stroke:#f47067,color:#adbac7,stroke-width:1px
    classDef trans fill:transparent,stroke:#539bf5,color:#adbac7,stroke-width:1px
    classDef batch fill:transparent,stroke:#57ab5a,color:#adbac7,stroke-width:1px
    class SQLException base
    class SQLNonTransientException,SQLSyntaxErrorException,SQLIntegrityConstraintViolationException,SQLDataException nonTrans
    class SQLTransientException,SQLTimeoutException,SQLTransactionRollbackException,SQLTransientConnectionException trans
    class BatchUpdateException batch
```

| 分支 | 含义 | 说明 |
|------|------|------|
| `SQLNonTransientException` | 非瞬时异常 | 重试无意义，必须修复问题（如 SQL 语法错误、约束违反） |
| `SQLTransientException` | 瞬时异常 | 可能通过重试恢复（如连接超时、锁等待超时） |
| `BatchUpdateException` | 批处理异常 | 继承自 `SQLException`，额外携带 `getUpdateCounts()` 表示每条语句的执行结果 |

!!! note "框架已处理的场景"
    在 Spring Boot 等框架中，这些子类通常已被统一转换，开发者较少直接与它们打交道。但在编写原生 JDBC 代码或自定义异常转换逻辑时，了解这套分类体系有助于实现更精确的错误处理策略。
