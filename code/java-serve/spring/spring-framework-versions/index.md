# Spring 框架版本说明

本文档介绍了新功能、显著变化，并提供了从早期版本升级的指导。如果您发现任何遗漏或不准确之处，请对相关页面提交拉取请求，或创建一个问题。

## 支持的版本

- 6.2.x 是即将推出的功能分支（2024年11月）。
- 6.1.x 是截至2023年11月的主要生产线。
- 6.0.x 是自2022年11月以来的上一代生产线。这个新框架版本以 JDK 17 和 Jakarta EE 9 为基础。
- 5.3.x 是第5代的最终功能分支，提供长期支持，适用于 JDK 8、JDK 11、JDK 17、JDK 21 和 Java EE 8。
- 4.3.x 于2020年12月31日正式达到生命周期终点（EOL）。该版本不再计划进行维护和安全补丁更新。
- 3.2.x 于2016年12月31日正式达到生命周期终点（EOL）。该版本不再计划进行维护和安全补丁更新。

在此，我们建议在可能的情况下，从 Maven Central 升级到最新的 Spring Framework 6.0.x / 5.3.x 版本。

## JDK版本范围

- Spring Framework 6.2.x：JDK 17-25（预期）
- Spring Framework 6.1.x：JDK 17-23
- Spring Framework 6.0.x：JDK 17-21
- Spring Framework 5.3.x：JDK 8-21（截至5.3.26）

我们全面测试并支持 JDK 的长期支持 (LTS) 版本上的 Spring：目前包括 JDK 8、JDK 11、JDK 17 和 JDK 21。此外，我们在尽力而为的基础上支持
JDK 18/19/20 等中间版本，这意味着我们接受错误报告并会尽可能在技术上解决这些问题，但不提供任何服务级别保证。我们建议在生产环境中使用
JDK 17 和 21 搭配 Spring Framework 6.x 以及 5.3.x。

## Java/Jakarta EE 版本

- Spring Framework 6.2.x：Jakarta EE 9-11（jakarta 命名空间）
- Spring Framework 6.1.x：Jakarta EE 9-10（jakarta 命名空间）
- Spring Framework 6.0.x：Jakarta EE 9-10（jakarta 命名空间）
- Spring Framework 5.3.x：Java EE 7-8（javax 命名空间）

Spring Framework 5.3.x 支持的最后一个规范版本是基于 javax 的 Java EE 8（Servlet 4.0、JPA 2.2、Bean Validation 2.0）。从
Spring Framework 6.0 开始，最低要求是 Jakarta EE 9（Servlet 5.0、JPA 3.0、Bean Validation 3.0），并推荐使用最新的 Jakarta EE
10（Servlet 6.0、JPA 3.1）。即将发布的 Jakarta EE 11（计划于 2024 年年中推出）将在 Spring Framework 6.2.x 中尽力支持，特别是针对
Tomcat 11 等相应的服务器。

