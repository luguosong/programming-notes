---
title: 存储过程
description: JDBC CallableStatement 调用存储过程，IN/OUT/INOUT 参数模式，registerOutParameter
---

# 存储过程

## 📖 为什么要把逻辑放到存储过程里？

存储过程（Stored Procedure）是数据库中预编译的一组 SQL 语句，封装为一个可复用的单元，通过名称调用。当你发现应用和数据库之间频繁往返执行多条 SQL 时，存储过程就派上用场了：

| 优势 | 说明 |
|------|------|
| `减少网络往返` | 一条调用代替多条 SQL，降低客户端与数据库之间的通信开销 |
| `复用与一致性` | 预编译后存储在数据库中，多个应用共享同一套逻辑 |
| `安全（参数化）` | 参数通过占位符传递，防止 SQL 注入，还可配合数据库权限控制访问 |
| `封装业务逻辑` | 将复杂查询和数据转换封装在数据库层，简化应用代码 |

## 📞 CallableStatement 基本用法

### 调用语法

`CallableStatement` 是 JDBC 中专门用于调用存储过程和存储函数的接口，通过 `Connection.prepareCall()` 创建。JDBC 定义了两种标准转义语法：

| 语法 | 适用场景 | 说明 |
|------|---------|------|
| `{call procedure_name(?, ?, ?)}` | 存储过程 | 无返回值占位符，参数全为 `?` |
| `{? = call function_name(?, ?)}` | 存储函数 | 第一个 `?` 是返回值占位符 |

完整调用流程：

``` java title="CallableStatement 调用语法总结"
--8<-- "code/java/database/jdbc-stored-procedure/src/test/java/com/luguosong/jdbc/StoredProcedureTest.java:callable_syntax"
```

### 参数模式

JDBC 存储过程支持三种参数模式，决定参数的数据流向：

| 模式 | 方向 | 说明 |
|------|------|------|
| `IN` | 输入 | 向存储过程传入值（默认模式） |
| `OUT` | 输出 | 存储过程返回值，必须调用 `registerOutParameter()` 注册类型 |
| `INOUT` | 双向 | 传入初始值，存储过程修改后返回更新后的值 |

## 📥 IN 参数示例

IN 参数是最常用的模式，通过 `setString()`、`setInt()` 等方法设置传入值。当存储函数有返回值时，使用 `{? = call ...}` 语法，并通过 `registerOutParameter()` 注册返回值类型。

``` java title="IN 参数：调用带返回值的存储函数"
--8<-- "code/java/database/jdbc-stored-procedure/src/test/java/com/luguosong/jdbc/StoredProcedureTest.java:in_params"
```

## 📤 OUT 参数示例

OUT 参数用于从存储过程中获取输出值。调用前必须通过 `registerOutParameter()` 注册参数的 SQL 类型，执行后通过 `getString()`、`getInt()` 等方法读取返回值。

``` java title="OUT 参数：获取存储过程的输出值"
--8<-- "code/java/database/jdbc-stored-procedure/src/test/java/com/luguosong/jdbc/StoredProcedureTest.java:out_params"
```

H2 数据库不支持原生 OUT 参数，示例中通过返回 `ResultSet` 的函数模拟。在 MySQL / Oracle 中，OUT 参数是原生支持的，调用方式见代码注释中的 MySQL 示例。

## 🔄 INOUT 参数示例

INOUT 参数兼具输入和输出功能：调用前设置初始值，存储过程内部修改后，通过同一个参数索引获取更新后的值。

``` java title="INOUT 参数：传入初始值并获取修改后的值"
--8<-- "code/java/database/jdbc-stored-procedure/src/test/java/com/luguosong/jdbc/StoredProcedureTest.java:inout_params"
```

与 OUT 参数类似，H2 数据库不支持原生 INOUT 参数，示例通过返回 `ResultSet` 模拟。MySQL 原生写法见代码注释。

## 📝 registerOutParameter 注册输出参数

OUT 和 INOUT 参数在执行存储过程之前，必须调用 `registerOutParameter()` 注册输出参数的 SQL 类型，告知 JDBC 驱动如何解析返回值。

``` java
// 注册返回值类型（存储函数的第一个参数）
cs.registerOutParameter(1, Types.VARCHAR);

// 注册 OUT 参数类型
cs.registerOutParameter(2, Types.INTEGER);
```

常用的 `java.sql.Types` 常量：

| Types 常量 | 对应 SQL 类型 | 获取方法 |
|-----------|-------------|---------|
| `Types.VARCHAR` | `VARCHAR` / `CHAR` | `getString()` |
| `Types.INTEGER` | `INT` / `INTEGER` | `getInt()` |
| `Types.BIGINT` | `BIGINT` | `getLong()` |
| `Types.DOUBLE` | `DOUBLE` / `FLOAT` | `getDouble()` |
| `Types.BOOLEAN` | `BOOLEAN` | `getBoolean()` |
| `Types.TIMESTAMP` | `TIMESTAMP` | `getTimestamp()` |

!!! note "H2 数据库限制"
    H2 不支持原生 OUT/INOUT 参数。上述示例通过返回 `ResultSet` 的函数模拟，代码注释中附带了 MySQL 原生写法。实际项目中如需使用存储过程，建议在 MySQL / PostgreSQL / Oracle 等完整功能数据库上测试。
