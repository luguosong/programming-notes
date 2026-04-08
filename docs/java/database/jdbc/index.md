---
title: JDBC
---

# JDBC

想从 Java 程序访问数据库，却发现 MySQL、PostgreSQL、Oracle 各家的连接方式完全不同？JDBC（Java Database Connectivity）就是为解决这个问题而生的——它提供一套统一 API，让你的代码`不因切换数据库而重写`。

## 🏗️ 为什么需要 JDBC？——统一数据库访问的标准层

### JDBC 在技术栈中的位置

JDBC 充当 Java 应用程序与底层数据库驱动之间的标准抽象层，屏蔽了不同数据库的实现差异。

``` mermaid
graph TB
    A[Java 应用程序] --> B[JDBC API\njava.sql / javax.sql]
    B --> C[JDBC Driver\n各数据库厂商提供]
    C --> D[(关系型数据库\nMySQL / PostgreSQL / Oracle / H2…)]

    classDef app fill:transparent,stroke:#539bf5,color:#adbac7,stroke-width:2px
    classDef api fill:transparent,stroke:#768390,color:#adbac7,stroke-width:1px
    classDef driver fill:transparent,stroke:#e3b341,color:#adbac7,stroke-width:1px
    classDef db fill:transparent,stroke:#57ab5a,color:#adbac7,stroke-width:2px
    class A app
    class B api
    class C driver
    class D db
```

### 一次数据库操作涉及哪些接口？

| 接口 / 类 | 所在包 | 职责说明 |
|-----------|--------|---------|
| `DriverManager` | `java.sql` | 管理 JDBC 驱动，提供 `getConnection()` 获取数据库连接 |
| `Connection` | `java.sql` | 代表一个数据库连接，是所有操作的入口 |
| `Statement` | `java.sql` | 执行静态 SQL 语句 |
| `PreparedStatement` | `java.sql` | 执行预编译的参数化 SQL，防 SQL 注入 |
| `ResultSet` | `java.sql` | 保存查询结果，提供游标逐行读取数据 |
| `DataSource` | `javax.sql` | 连接池的标准接口，生产环境推荐使用 |

### 驱动是怎么加载的？——三代注册方式演进

在调用 `DriverManager.getConnection()` 之前，JDBC 驱动必须先完成注册。从手动注册到全自动，JDBC 经历了三个演进阶段：

| 方式 | 写法 | 说明 |
|------|------|------|
| 方式一：显式注册 | `DriverManager.registerDriver(new XxxDriver())` | 最原始的写法，会导致驱动被注册两次，不推荐 |
| 方式二：反射加载 | `Class.forName("com.mysql.cj.jdbc.Driver")` | 触发驱动静态代码块，内部调用 `registerDriver()`；驱动类名可写入配置文件，实现解耦 |
| 方式三：SPI 自动 | 无需任何代码 | JDBC 4.0（Java 6）起，JVM 自动扫描 classpath 中所有 JAR 的 `META-INF/services/java.sql.Driver` 文件并注册 |

`方式一：显式调用 DriverManager.registerDriver()`

``` java title="方式一：显式注册驱动对象"
--8<-- "code/java/database/jdbc-connection/src/test/java/com/luguosong/jdbc/ConnectionTest.java:driver_register_way1"
```

`方式二：Class.forName() 反射触发静态初始化`

``` java title="方式二：Class.forName 反射加载驱动"
--8<-- "code/java/database/jdbc-connection/src/test/java/com/luguosong/jdbc/ConnectionTest.java:driver_register_way2"
```

`方式三：JDBC 4.0+ SPI 自动发现（现代推荐方式）`

``` java title="方式三：SPI 自动注册，无需任何显式代码"
--8<-- "code/java/database/jdbc-connection/src/test/java/com/luguosong/jdbc/ConnectionTest.java:driver_register_way3"
```

!!! note "现代项目无需手动注册"
    使用 Maven/Gradle 引入驱动 JAR 后，JDBC 4.0 SPI 机制会在 `DriverManager` 初始化时自动完成注册。实际项目中`无需编写任何注册代码`，直接调用 `DriverManager.getConnection()` 即可。
