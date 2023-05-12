---
layout: note
title: 日志框架概述
nav_order: 10
parent: Java日志框架
create_time: 2023/4/17
---
# 发展史

| 年份 | 事件                                           | 人物/组织        |
| ---- | ---------------------------------------------- | ---------------- |
| 1998 | Doug Lea开发了`JUL`                              | Doug Lea         |
| 1999 | `Log4j`项目启动                                  | Ceki Gülcü     |
| 2001 | `JUL`被添加到Java SE 1.4中<br />`Log4j`发布1.0版本 | -                |
| 2002 | `Log4j`项目获得Apache软件基金会支持              | Apache软件基金会 |
| 2003 | `Log4j`发布1.2版本                               | -                |
| 2004 | `JCL`项目启动                                    | Craig McClanahan |
| 2006 | `JCL`被添加到Apache Commons项目中                | -                |
| 2007 | `SLF4J`项目启动                                  | Ceki Gülcü     |
| 2009 | `Logback`项目启动                                | Ceki Gülcü     |
| 2010 | `Logback`发布1.0版本                             | -                |
| 2011 | `Log4j2`项目启动                                 | Apache软件基金会 |
| 2014 | `Log4j2`发布1.0版本                              | -                |
| 2017 | `Log4j2`发布2.0版本，增加对Java9的支持           | -                |
| 2021 | `Log4j2`发布2.14.1版本，修复多个漏洞             | -                |

# 日志门面和日志实现的区别

在Java中，`日志门面`和 `日志框架`是两个不同的概念。`日志门面`是一个 `接口`，它定义了一组用于记录日志的方法，而 `日志框架`则是实现这些方法的 `具体库`。

`日志门面`的作用是为应用程序提供一种与具体日志框架无关的记录日志的方式。这样，应用程序可以在不改变代码的情况下更换日志框架，而不会影响到应用程序的其他部分。常见的日志门面包括 `SLF4J`和 `Apache Commons Logging`。

`日志框架`则是实现日志门面定义的方法的具体库。它们提供了一些额外的功能，如日志级别、日志格式化和日志输出目的地的配置。常见的日志框架包括 `Log4j`,`JUL`和 `Logback`。