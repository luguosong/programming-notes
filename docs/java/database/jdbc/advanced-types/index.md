---
title: 高级数据类型
description: JDBC 高级数据类型概览，包括 SQLXML 和 ARRAY 的创建与读取，实际开发使用建议
---

# 高级数据类型

**本文你会学到**：

- SQL 标准高级数据类型在 JDBC 中的 Java 映射
- ARRAY 数组类型的创建、存储和读取（`createArrayOf` / `getArray`）
- SQLXML 类型的创建和读写操作
- 实际开发中使用高级数据类型的建议

## 🗺️ 什么场景需要用到高级数据类型？

SQL 标准定义了一系列高级数据类型，JDBC 为这些类型提供了对应的 Java 映射支持。大多数时候 ORM 框架会帮你处理，但当你需要利用数据库原生能力（如 XML 索引查询、数组包含判断）时，了解这些类型就很有价值。主要包括：

| SQL 类型 | Java 映射 | 用途 |
|---------|----------|------|
| `ARRAY` | `java.sql.Array` | 存储数组值，如标签列表、ID 集合 |
| `SQLXML` | `java.sql.SQLXML` | 存储结构化 XML 数据 |
| `STRUCT` | `java.sql.Struct` | 存储自定义结构体 |
| `DISTINCT` | 底层类型的 Java 映射 | 基于已有类型的自定义类型 |
| `DATALINK` | `java.net.URL` | 引用外部资源链接 |

实际开发中，这些类型多由 ORM 框架（如 MyBatis、JPA）处理，直接使用 JDBC 操作的场景较少。

## 📦 ARRAY 数组类型

### 何时需要用 ARRAY 存储？

SQL `ARRAY` 类型允许在单个列中存储同构数组值，典型场景包括标签列表、ID 集合、枚举值等。

需要注意的是，**并非所有数据库都支持 `ARRAY` 类型**：

- **支持**：PostgreSQL、H2、Oracle（VARRAY）
- **不支持**：MySQL（可用 `JSON` 类型替代）

### 创建并存储 ARRAY

通过 `Connection.createArrayOf()` 方法可以将 Java 数组包装为 `java.sql.Array` 对象，再绑定到 `PreparedStatement` 参数中：

``` java title="createArrayOf() 创建数组并存入数据库"
--8<-- "code/java/database/jdbc-advanced-types/src/test/java/com/luguosong/jdbc/AdvancedTypesTest.java:array_create"
```

### 读取 ARRAY

从 `ResultSet` 获取 `java.sql.Array` 对象后，调用 `getArray()` 转换为 Java 数组：

``` java title="getArray() 读取数组并转换为 Java 数组"
--8<-- "code/java/database/jdbc-advanced-types/src/test/java/com/luguosong/jdbc/AdvancedTypesTest.java:array_read"
```

### ARRAY 操作方法速查

| 方法 | 说明 |
|------|------|
| `Connection.createArrayOf(typeName, elements)` | 从 Java 数组创建 `java.sql.Array` 对象 |
| `PreparedStatement.setArray(i, array)` | 将 Array 绑定到预编译参数 |
| `ResultSet.getArray(column)` | 从结果集获取 `java.sql.Array` 对象 |
| `Array.getArray()` | 将 ARRAY 转换为 Java `Object`（通常为 `Object[]`） |
| `Array.free()` | 释放 ARRAY 资源 |

!!! warning "数据库兼容性"

    ARRAY 类型的支持因数据库而异。H2 和 PostgreSQL 支持，MySQL 不支持。跨数据库项目需谨慎使用。

## 📄 SQLXML 类型

### 什么是 SQLXML

`SQLXML` 类型用于在数据库中存储 XML 数据，支持 XQuery、XPath 等 XML 操作的数据库（如 Oracle、PostgreSQL）可以充分利用此类型实现高效的 XML 查询和索引。

### 创建并存储 SQLXML

通过 `Connection.createSQLXML()` 创建空的 `SQLXML` 对象，再设置其内容并绑定到 `PreparedStatement`：

``` java title="createSQLXML() 创建并操作 XML 数据"
--8<-- "code/java/database/jdbc-advanced-types/src/test/java/com/luguosong/jdbc/AdvancedTypesTest.java:sqlxml_write_read"
```

### SQLXML 操作方法速查

| 方法 | 说明 |
|------|------|
| `Connection.createSQLXML()` | 创建空的 SQLXML 对象 |
| `SQLXML.setString(xml)` | 设置 XML 内容（字符串方式） |
| `SQLXML.setCharacterStream()` | 设置 XML 内容（流方式，适合大 XML） |
| `SQLXML.getString()` | 获取 XML 内容为 String |
| `SQLXML.getCharacterStream()` | 获取 XML 内容为 Reader |
| `PreparedStatement.setSQLXML(i, sqlxml)` | 将 SQLXML 绑定到预编译参数 |
| `SQLXML.free()` | 释放 SQLXML 资源 |

!!! warning "数据库兼容性"

    SQLXML 支持因数据库差异较大。Oracle 和 PostgreSQL 有原生支持，MySQL 需要用 `LONGTEXT` 存储。使用前务必确认目标数据库的兼容性。

## 💡 实际开发中的使用建议

- **优先使用 ORM 框架**（MyBatis / JPA）处理高级类型映射，避免手写 JDBC 操作
- `ARRAY` 可考虑用**关联表**替代，兼容性更好，且便于建立外键约束
- XML 数据可考虑用 **JSON 替代**（现代趋势），大多数数据库对 JSON 的支持优于 XML
- 仅在需要利用**数据库原生 XML/数组查询能力**时直接使用 JDBC 高级类型
- 所有大对象（LOB、ARRAY、SQLXML）使用后**务必调用 `free()` 释放资源**，避免内存泄漏
