---
title: 最佳实践
description: JDBC 开发最佳实践总结，包括资源管理、SQL 注入防护、事务控制、连接池使用
---

# JDBC 开发中有哪些常踩的坑？——最佳实践

写 JDBC 代码时，连接忘关闭、SQL 被注入、事务没控制好……这些问题你迟早会遇到。下面这张表总结了最关键的防坑原则：

| 原则 | 说明 |
|------|------|
| 使用 try-with-resources | 确保 `Connection` / `Statement` / `ResultSet` 自动关闭，防止连接泄漏 |
| 优先使用 PreparedStatement | 防 SQL 注入，同时提升预编译性能，即使无注入风险也推荐 |
| 关闭 autoCommit 管理事务 | 涉及多步操作时手动控制事务，确保原子性，异常时完整回滚 |
| 使用连接池（DataSource） | 避免频繁创建/销毁物理连接的开销，生产环境必选 HikariCP / Druid 等 |
| 参数化批处理大批量写入 | 使用 `PreparedStatement` + `addBatch()` + 事务，减少网络往返次数 |
| 不要硬编码 SQL | 将 SQL 提取为常量或通过 MyBatis / JPA 等 ORM 框架管理，提高可维护性 |
| 按列名而非索引读取数据 | `rs.getString("name")` 比 `rs.getString(2)` 更健壮，不受列顺序变化影响 |
| 用 DataSource 接口解耦 | 业务代码依赖 `javax.sql.DataSource` 接口，方便切换连接池实现 |
