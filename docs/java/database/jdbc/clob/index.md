---
title: CLOB 大对象
description: JDBC CLOB 字符大对象读写，createClob、setCharacterStream、getSubString，BLOB vs CLOB 对比
---

# CLOB 大对象

## CLOB 是什么

`CLOB`（Character Large Object）是 JDBC 中用于存储大文本数据的类型，适用于文章内容、JSON 文档、XML、日志等场景。与之对应的 `BLOB`（Binary Large Object）用于存储二进制数据（图片、视频、音频）。

在 MySQL 中，`CLOB` 对应的类型是 `TEXT` / `LONGTEXT`；在 Oracle 中则直接使用 `CLOB` 类型。JDBC 提供了统一的 API 来操作这些大对象，屏蔽了不同数据库的差异。

## 写入 CLOB 数据

### createClob + setCharacterStream 方式

`Connection.createClob()` 创建一个空的 `Clob` 对象，再通过 `setCharacterStream(1)` 获取 `Writer` 进行流式写入。这种方式适合大文本场景（MB 级别），数据通过流逐步写入，不会一次性占用内存。

``` java title="createClob() + setCharacterStream() 写入 CLOB"
--8<-- "code/java/database/jdbc-clob/src/test/java/com/luguosong/jdbc/ClobTest.java:clob_write"
```

### setString 直接写入

`PreparedStatement.setString()` 可以直接将字符串写入 CLOB 列，这是最简单的方式。但数据量较大时（如超过 JVM 堆内存），会将整个文本加载到内存中，不适合超大文本。

``` java title="setString() 直接写入 CLOB"
--8<-- "code/java/database/jdbc-clob/src/test/java/com/luguosong/jdbc/ClobTest.java:clob_write_string"
```

## 读取 CLOB 数据

读取 CLOB 数据有两种常见方式：

- `ResultSet.getClob()` 返回 `Clob` 对象，通过 `getSubString()` 提取全部或部分内容，适合需要分段读取的场景
- `ResultSet.getString()` 直接返回完整字符串，JDBC 驱动会自动处理 CLOB 到 String 的转换

``` java title="getClob() + getSubString() 读取 CLOB"
--8<-- "code/java/database/jdbc-clob/src/test/java/com/luguosong/jdbc/ClobTest.java:clob_read"
```

!!! tip "推荐使用 getString()"
    对于大多数场景，直接使用 `ResultSet.getString()` 读取 CLOB 列是最简单的方式。JDBC 驱动会自动将 CLOB 内容转换为 `String`。只有处理超大文本（如 100MB+）时才需要使用 `getCharacterStream()` 流式读取。

## BLOB vs CLOB 对比

| 维度 | BLOB | CLOB |
|------|------|------|
| 全称 | Binary Large Object | Character Large Object |
| 存储内容 | 二进制数据（图片、视频、音频） | 字符数据（文章、JSON、XML） |
| JDBC 类型 | `Blob` | `Clob` |
| 创建方法 | `Connection.createBlob()` | `Connection.createClob()` |
| 写入方式 | `setBinaryStream()` | `setCharacterStream()` |
| 读取方式 | `getBinaryStream()` | `getCharacterStream()` / `getString()` |
| 简单写入 | `setBytes()` | `setString()` |
| MySQL 类型 | `BLOB` / `LONGBLOB` | `TEXT` / `LONGTEXT` |

选择原则很简单：存储的是字符文本就用 `CLOB`，存储的是二进制数据就用 `BLOB`。

## 资源释放

`Clob` 和 `Blob` 对象在创建它们的事务期间一直有效，占用数据库服务端的资源。如果长事务中持有大量大对象而不释放，可能导致资源耗尽。使用完毕后应调用 `free()` 主动释放。

``` java title="free() 释放 CLOB 资源"
--8<-- "code/java/database/jdbc-clob/src/test/java/com/luguosong/jdbc/ClobTest.java:clob_free"
```

!!! warning "及时释放大对象资源"
    `Clob`、`Blob` 对象在创建它们的事务期间一直有效。长事务可能导致资源耗尽，应在使用完毕后调用 `free()` 主动释放。对于小文本使用 `setString()` / `getString()` 方式则无需手动管理。
