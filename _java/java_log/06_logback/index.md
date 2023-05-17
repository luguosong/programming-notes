---
layout: note
title: Logback
nav_order: 40
parent: Java日志框架
create_time: 2023/5/14
---

# 简介

Logback是由log4j创始人设计的日志框架，是log4j的改进版，也是SpringBoot默认的日志框架。

Logback分为三个模块:

- logback-core:Logback框架的基础
- `logback-classic`:Logback框架的核心,`logback-classic`包含logback-core基础模块
- logback-access:与Servlet容器集成的模块

# 组件

- Logger:日志记录器
- Appender：指定日志输出目的地
- Layout：格式化日志输出，被封装在`encoder`中

# 

