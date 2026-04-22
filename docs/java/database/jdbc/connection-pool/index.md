---
title: 连接池
description: JDBC 连接池原理，HikariCP 与 Druid 配置对比，DataSource 标准接口
---

# 连接池

**本文你会学到**：

- 为什么频繁创建物理连接会成为性能瓶颈
- HikariCP 和 Druid 两大连接池的配置和特点
- 连接池 vs 直连的使用方式对比
- 面向 `DataSource` 标准接口编程的意义

## 💡 连接池原理

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

## 🏭 主流实现

### Druid 连接池

[Druid](https://github.com/alibaba/druid) 是阿里巴巴开源的 JDBC 连接池，除基本连接池功能外，内置了 `SQL 监控、慢查询统计、连接池状态可视化`等运维能力，在国内 Java 项目中广泛使用。

``` java title="Druid 连接池基本参数配置"
--8<-- "code/java/database/jdbc-pool/src/test/java/com/luguosong/jdbc/ConnectionPoolTest.java:druid_basic_config"
```

`Druid 与 HikariCP 核心参数对比：`

| 参数含义 | HikariCP | Druid |
|---------|----------|-------|
| 最大连接数 | `maximumPoolSize` | `maxActive` |
| 最小空闲数 | `minimumIdle` | `minIdle` |
| 初始化连接数 | — | `initialSize` |
| 获取连接超时 | `connectionTimeout`（ms） | `maxWait`（ms） |
| 连接检测 SQL | `connectionTestQuery` | `validationQuery` |

``` java title="HikariCP vs Druid 使用方式对比"
--8<-- "code/java/database/jdbc-pool/src/test/java/com/luguosong/jdbc/ConnectionPoolTest.java:druid_vs_hikari"
```

!!! note "如何选择连接池"

    - `HikariCP`：性能最优，Spring Boot 默认，大多数场景首选
    - `Druid`：功能丰富，需要 SQL 监控/慢查询统计/连接池状态监控时首选

### HikariCP 基本配置

[HikariCP](https://github.com/brettwooldridge/HikariCP) 是目前 Java 生态中性能最优的连接池实现，也是 Spring Boot 的默认连接池。

``` java title="HikariCP 连接池创建与基本参数配置"
--8<-- "code/java/database/jdbc-pool/src/test/java/com/luguosong/jdbc/ConnectionPoolTest.java:hikari_basic_config"
```

## 📊 对比与最佳实践

### 连接池 vs 直连对比

``` java title="DriverManager 直连 vs HikariCP 连接池使用方式对比"
--8<-- "code/java/database/jdbc-pool/src/test/java/com/luguosong/jdbc/ConnectionPoolTest.java:pool_vs_direct"
```

### DataSource 标准接口

`DataSource` 是 `javax.sql` 包中的标准接口，生产代码应面向此接口编程，而非直接依赖 `HikariDataSource`。

``` java title="通过 DataSource 接口获取连接，连接用后自动归还池"
--8<-- "code/java/database/jdbc-pool/src/test/java/com/luguosong/jdbc/ConnectionPoolTest.java:datasource_get_connection"
```
