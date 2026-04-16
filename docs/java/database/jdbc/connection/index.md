---
title: 建立连接
description: JDBC 数据库连接方式，包括 DriverManager.getConnection() 三种重载、JDBC URL 格式、try-with-resources 最佳实践
---

# 建立数据库连接

**本文你会学到**：

- `DriverManager.getConnection()` 三种重载方式的区别和适用场景
- JDBC URL 的格式规则和常见数据库的 URL 写法
- 为什么以及如何使用 try-with-resources 确保连接自动关闭

### 怎样建立数据库连接？——三种重载方式

JDBC 提供三种方式通过 `DriverManager` 获取连接。

`方式一：url + user + password（最常用）`

``` java title="三参数方式建立连接"
--8<-- "code/java/database/jdbc-connection/src/test/java/com/luguosong/jdbc/ConnectionTest.java:driver_manager_connect"
```

`方式二：仅 URL（将认证信息编码在 URL 中）`

``` java title="单参数 URL 方式建立连接"
--8<-- "code/java/database/jdbc-connection/src/test/java/com/luguosong/jdbc/ConnectionTest.java:connection_url_only"
```

`方式三：Properties 对象传参`

``` java title="通过 Properties 对象传入认证信息"
--8<-- "code/java/database/jdbc-connection/src/test/java/com/luguosong/jdbc/ConnectionTest.java:connection_properties"
```

### 连接字符串怎么写？——JDBC URL 格式

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

### 连接忘记关闭会怎样？——try-with-resources

`Connection` 实现了 `AutoCloseable` 接口，使用 try-with-resources 可确保连接在离开作用域后自动关闭，无需手动调用 `close()`。

``` java title="try-with-resources 自动关闭连接"
--8<-- "code/java/database/jdbc-connection/src/test/java/com/luguosong/jdbc/ConnectionTest.java:try_with_resources"
```

!!! warning "务必关闭连接"
    数据库连接属于稀缺资源。未关闭的连接会持续占用数据库服务器端的槽位，最终导致连接耗尽（`Too many connections` 错误）。
    `始终使用 try-with-resources 包裹 Connection、Statement、ResultSet。`
