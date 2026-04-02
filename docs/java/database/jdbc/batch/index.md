---
title: 批处理
description: JDBC 批处理机制，Statement/PreparedStatement 的 addBatch+executeBatch，批处理与事务结合的最佳实践
---

# 批处理

### 为什么要用批处理？

每次执行单条 SQL 都会经历：`Java → 网络 → 数据库解析 → 执行 → 网络 → Java` 的完整往返。批处理将多条 SQL 打包一次性发送，显著减少网络往返次数，大幅提升大批量写入性能。

### Statement 批处理

``` java title="Statement.addBatch() + executeBatch() 批量执行"
--8<-- "code/java/database/jdbc-transaction-batch/src/test/java/com/luguosong/jdbc/BatchProcessingTest.java:statement_batch"
```

### PreparedStatement 批处理

``` java title="PreparedStatement 批处理：SQL 预编译一次，参数绑定多次"
--8<-- "code/java/database/jdbc-transaction-batch/src/test/java/com/luguosong/jdbc/BatchProcessingTest.java:prepared_statement_batch"
```

### 批处理 + 事务（最佳实践）

将批处理放在事务中执行，是大批量写入的最佳实践：既减少网络往返次数，又保证原子性；出错时可完整回滚。

``` java title="批处理 + 事务结合，每批执行后统一提交"
--8<-- "code/java/database/jdbc-transaction-batch/src/test/java/com/luguosong/jdbc/BatchProcessingTest.java:batch_with_transaction"
```
