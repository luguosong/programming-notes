---
layout: note
title: SLF4J
nav_order: 40
parent: Java日志框架
create_time: 2023/5/5
---

# 概述

JAVA简易日志门面（Simple Logging Facade for Java，缩写`SLF4J`），是一套包装Logging框架的界面程式，以`外观模式`实现。可以在软件部署的时候决定要使用的Logging框架，目前主要支援的有`Java Logging API`、`log4j`及`logback`等框架。以MIT授权方式发布。

# 日志门面级别

- ERROR
- WARN
- INFO
- DEBUG
- TRACE

# slf4j + slf4j simple

`slf4j-simple`是`slf4j`自带的简单日志实现

- 引入依赖

{% highlight xml %}
{% include_relative log_slf4j_01_basic/pom.xml %}
{% endhighlight %}

- 代码

{% highlight java %}
{% include_relative log_slf4j_01_basic/src/main/java/HelloSlf4j.java %}
{% endhighlight %}
