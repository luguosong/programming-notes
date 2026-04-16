---
title: ResultSet 数据读取
description: JDBC ResultSet 游标遍历、按列名/索引取值、ResultSetMetaData、可滚动 ResultSet
---

# ResultSet 数据读取

**本文你会学到**：

- `ResultSet` 游标机制和 `next()` 遍历方式
- 按列名取值与按列索引取值的区别和选择
- `ResultSetMetaData` 动态获取列信息
- 可滚动 `ResultSet` 的创建和双向导航方法

## 🚀 游标遍历机制

### ResultSet 怎么读数据？——游标机制

`ResultSet` 内部维护一个`游标`，初始位置在第一行之前。调用 `next()` 将游标前移一行，返回 `true` 表示当前行有数据，`false` 表示已越过最后一行。

```
初始位置
    ↓
[before first] → [row 1] → [row 2] → [row 3] → [after last]
                    ↑
                  next() 调用后游标所在位置
```

### 向前遍历（按列名取值）

``` java title="next() 遍历 ResultSet，按列名获取各类型数据"
--8<-- "code/java/database/jdbc-statement/src/test/java/com/luguosong/jdbc/ResultSetTest.java:iterate_forward"
```

### 按列索引取值

``` java title="按列索引（1-based）与按列名两种方式对比"
--8<-- "code/java/database/jdbc-statement/src/test/java/com/luguosong/jdbc/ResultSetTest.java:get_by_index"
```

!!! note "按索引 vs 按列名"
    - 按列名（`rs.getString("name")`）：可读性好，不受 SELECT 列顺序变化影响，`推荐`
    - 按列索引（`rs.getString(2)`）：性能略高，但列顺序变化后容易出错

## 🔍 元数据与高级遍历

### ResultSetMetaData：在遍历中动态获取列信息

``` java title="遍历结果集时同步读取列元数据"
--8<-- "code/java/database/jdbc-statement/src/test/java/com/luguosong/jdbc/ResultSetTest.java:result_set_metadata_in_rs"
```

### 只能往前遍历？——可滚动 ResultSet

默认 `ResultSet` 只能向前遍历（`TYPE_FORWARD_ONLY`）。通过 `createStatement` 时指定类型，可获得支持双向滚动的 `ResultSet`。

``` java title="可滚动 ResultSet：last()、first()、absolute()、relative()"
--8<-- "code/java/database/jdbc-statement/src/test/java/com/luguosong/jdbc/ResultSetTest.java:scrollable_result_set"
```

| 导航方法 | 说明 |
|---------|------|
| `first()` | 移动到第一行 |
| `last()` | 移动到最后一行 |
| `absolute(n)` | 移动到第 n 行（从 1 计数） |
| `relative(n)` | 从当前行相对移动 n 行（正数向后，负数向前） |
| `beforeFirst()` | 移动到第一行之前（初始位置） |
| `afterLast()` | 移动到最后一行之后 |
