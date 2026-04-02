---
title: 数据库元数据
description: JDBC DatabaseMetaData 和 ResultSetMetaData 接口，获取数据库信息和查询结果列信息
---

# 数据库元数据

JDBC 提供两类元数据接口：

| 接口 | 获取方式 | 描述 |
|------|---------|------|
| `DatabaseMetaData` | `conn.getMetaData()` | 数据库级别信息：产品名、版本、驱动信息、表结构等 |
| `ResultSetMetaData` | `rs.getMetaData()` | 查询结果级别信息：列数、列名、列类型等 |

### DatabaseMetaData：数据库基本信息

``` java title="获取数据库产品名称、版本、驱动信息"
--8<-- "code/java/database/jdbc-metadata/src/test/java/com/luguosong/jdbc/MetaDataTest.java:database_metadata"
```

### DatabaseMetaData：枚举数据库中的表

``` java title="通过 getTables() 列出数据库中所有表"
--8<-- "code/java/database/jdbc-metadata/src/test/java/com/luguosong/jdbc/MetaDataTest.java:database_metadata_tables"
```

### ResultSetMetaData：查询结果的列信息

``` java title="从 ResultSetMetaData 获取列数、列名、列类型"
--8<-- "code/java/database/jdbc-metadata/src/test/java/com/luguosong/jdbc/MetaDataTest.java:resultset_metadata"
```

!!! note "元数据的典型应用场景"
    - 通用数据库工具（如 JDBC 客户端）动态渲染表格列头
    - ORM 框架在运行时反射映射列名与字段名
    - 数据库迁移工具获取表结构信息
