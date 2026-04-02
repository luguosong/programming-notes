---
title: PreparedStatement
description: JDBC PreparedStatement 参数化查询，防 SQL 注入、预编译复用、BLOB 大字段读写
---

# PreparedStatement 参数化查询

### 为什么要用 PreparedStatement？

1. `防 SQL 注入`：参数通过占位符 `?` 绑定，驱动对参数值进行转义，彻底防止注入攻击
2. `预编译性能`：SQL 结构只解析、编译一次，多次执行只需替换参数值，减少数据库解析开销

### 参数化查询

``` java title="PreparedStatement 带 ? 占位符查询"
--8<-- "code/java/database/jdbc-preparedstatement/src/test/java/com/luguosong/jdbc/PreparedStatementTest.java:parameterized_query"
```

### 参数化插入

``` java title="PreparedStatement 参数化插入，绑定多种类型参数"
--8<-- "code/java/database/jdbc-preparedstatement/src/test/java/com/luguosong/jdbc/PreparedStatementTest.java:parameterized_insert"
```

### 参数化更新

``` java title="PreparedStatement 参数化 UPDATE"
--8<-- "code/java/database/jdbc-preparedstatement/src/test/java/com/luguosong/jdbc/PreparedStatementTest.java:parameterized_update"
```

### 防 SQL 注入对比验证

``` java title="Statement 注入成功 vs PreparedStatement 注入失败"
--8<-- "code/java/database/jdbc-preparedstatement/src/test/java/com/luguosong/jdbc/PreparedStatementTest.java:prevent_sql_injection"
```

### 预编译复用

同一个 `PreparedStatement` 对象可通过重新绑定参数多次执行，SQL 只编译一次。

``` java title="复用同一 PreparedStatement 对象执行多次查询"
--8<-- "code/java/database/jdbc-preparedstatement/src/test/java/com/luguosong/jdbc/PreparedStatementTest.java:reuse_prepared_statement"
```

### Blob 大字段读写

`PreparedStatement` 通过 `setBytes()` / `setBinaryStream()` 写入二进制大对象（BLOB），通过 `getBytes()` / `getBinaryStream()` 读取。典型场景：存储图片、文档、音频等文件数据。

| 方法 | 适用场景 |
|------|---------|
| `setBytes(int, byte[])` | 小文件，数据已在内存中（推荐） |
| `setBinaryStream(int, InputStream)` | 大文件，流式写入，不占用内存 |
| `getBytes(String)` | 读取小文件，直接返回 `byte[]` |
| `getBinaryStream(String)` | 读取大文件，返回 `InputStream` |

``` java title="PreparedStatement 写入和读取 BLOB 大字段"
--8<-- "code/java/database/jdbc-preparedstatement/src/test/java/com/luguosong/jdbc/PreparedStatementTest.java:blob_write_read"
```

!!! warning "BLOB 大小限制"
    MySQL 默认 `max_allowed_packet` 为 64 MB，存储超大文件建议使用对象存储（OSS/S3），数据库只存元数据和文件路径。

!!! tip "优先使用 PreparedStatement"
    任何含用户输入的 SQL 都应使用 `PreparedStatement`。即使是内部系统无注入风险的场景，预编译带来的性能提升和代码可读性提升也值得优先选择 `PreparedStatement`。
