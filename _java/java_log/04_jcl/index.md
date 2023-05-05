---
layout: note
title: Apache Commons Logging
nav_order: 40
parent: Java日志框架
create_time: 2023/4/26
---

# 概述

`Apache Commons Logging`（以前称为 `Jakarta Commons Logging` 或 `JCL`）是一个基于 Java 的日志工具和编程模型，用于进行日志记录和其他工具包。它提供了 API、日志实现以及对其他工具的封装实现。

在编写软件库时，我们经常需要记录日志信息，以便监控和排查问题。然而，市场上有很多不同的日志实现，我们编写的库不能强制整个应用程序采用某种特定的日志实现。

这时，我们可以采用日志包（Logging package），它充当了连接不同日志实现的超薄`桥接层`。通过在我们的库中使用 `commons-logging API`，我们可以确保在运行时，库能够与任何日志实现兼容。`Commons-logging` 支持众多流行的日志实现，并且为其他实现`编写适配器`也是相对简单的任务。

不仅库可以使用 commons-logging，应用程序也可以选择使用它。虽然对于应用程序来说，日志实现的独立性没有库那么重要，但是采用 commons-logging 可以让我们在不重新编译代码的情况下轻松更换日志实现。

需要注意的是，commons-logging `只负责在不同的日志实现之间进行桥接`，而不会负责初始化或终止运行时所使用的底层日志实现。这部分工作需要由应用程序负责。但是，很多流行的日志实现都具有自动初始化的功能，在这种情况下，我们的应用程序可能无需包含与所使用的日志实现相关的特定代码，从而实现了与具体日志实现的解耦。

# Hello World

- 导入依赖

{% highlight xml %}
{% include_relative log_jcl_01_basic/pom.xml %}
{% endhighlight %}

- 编写代码

{% highlight java %}
{% include_relative log_jcl_01_basic/src/main/java/com/luguosong/CommonLogHello.java %}
{% endhighlight %}

# JCL使用Log4j

{: .note}
> 只要引入Log4j依赖，JCL就会自动使用Log4j

- 导入依赖

{% highlight xml %}
{% include_relative log_jcl_02_jcl_with_log4j/pom.xml %}
{% endhighlight %}

- 编写代码

{% highlight java %}
{% include_relative log_jcl_02_jcl_with_log4j/src/main/java/com/luguosong/CommonLogWithLog4j.java %}
{% endhighlight %}

# JCL原理

jcl会按顺序加载`Log4JLogger`,`Jdk14Logger`,`Jdk13LumberjackLogger`,`SimpleLog(JCL自带的)`,有就使用，没有就去找下一个

![](https://cdn.jsdelivr.net/gh/guosonglu/images@master/blog-img/20230505111637.png)
