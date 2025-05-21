# JDBC

## 概述

Java数据库连接，（Java Database Connectivity，简称`JDBC`）是Java语言中用来规范客户端程序如何来访问数据库的`应用程序接口`，提供了诸如查询和更新数据库中数据的方法。JDBC也是Sun Microsystems的商标。JDBC是面向`关系型数据库`的。

JDBC API 的设计旨在保持简单事物的简单性。这意味着 JDBC 使日常的数据库任务变得容易。本教程将通过示例引导您使用 JDBC 执行常见的 SQL 语句，以及完成数据库应用程序中常见的其他目标。

## 入门案例

- 导入依赖

```xml
<!--java连接MySQL-->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.3.0</version>
</dependency>
```

JDBC编程可以分为以下三个步骤：

1. 连接到数据源
2. 向数据库发送查询和更新语句
3. 检索和处理从数据库收到的查询结果

- 代码如下：

``` java
--8<-- "code/java-serve/database/jdbc/jdbc-demo/src/main/java/com/luguosong/JDBCHello.java"
```

!!! warning

    JDBC4.0之后 自动扫描jar包下这个文件，理论上是不用我们主动的注册驱动，方便了我们的编程。

    <figure markdown="span">
      ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202407022117913.png){ loading=lazy }
      <figcaption></figcaption>
    </figure>

## Sql注入

### Sql注入问题

### prepareStatement解决Sql注入
