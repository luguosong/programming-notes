---
layout: note
title: Log4j
nav_order: 30
parent: Java日志框架
create_time: 2023/4/20
---

# 概述

`Apache Log4j`是一个基于Java的日志记录工具，由瑞士程序员`Ceki Gülcü`于2001年开发，现在则是Apache软件基金会的一个项目。Log4j是几种Java日志框架之一。

`Gülcü`随后开发了`SLF4J`和`Logback`项目，旨在成为`Log4j`的继任者。

Log4j团队推出了Log4j的新版本2.0，致力于解决Log4j 1.2、1.3、java.util.logging和logback中存在的问题，并解决这些框架中的架构问题。此外，Log4j 2.0还提供了一个可扩展的插件架构。需要注意的是，Log4j 2.0`不与1.x向后兼容`，但提供了一个“适配器”以协助迁移。

然而，在2021年12月9日，`Log4j 2.0`被发现存在一个零日远程代码执行漏洞，被称为`Log4Shell`，公共漏洞和暴露编号为CVE-2021-44228。该漏洞被认为是“过去十年来最大、最关键的漏洞”。


# Hello World

{% highlight java %}
{% include_relative log_log4j_01_basic/src/main/java/com/luguosong/Log4jHello.java %}
{% endhighlight %}

{: .warning}
> log4j默认会去读取根目录下的`log4j.properties`文件,
> 当既没有配置`log4j.properties`文件,
> 又没有调用`BasicConfigurator.configure()`，
> 则会报缺少`Appender`的错误

其中`BasicConfigurator.configure()`代码如下:

``` java
  /**
     Add a {@link ConsoleAppender} that uses {@link PatternLayout}
     using the {@link PatternLayout#TTCC_CONVERSION_PATTERN} and
     prints to <code>System.out</code> to the root category.  */
  static
  public
  void configure() {
    Logger root = Logger.getRootLogger();
    root.addAppender(new ConsoleAppender(
           new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN)));
  }
```

# 设置配置文件

log4j.properties配置文件：

{% highlight properties %}
{% include_relative log_log4j_01_basic/src/main/resources/log4j.properties %}
{% endhighlight %}

{% highlight java %}
{% include_relative log_log4j_01_basic/src/main/java/com/luguosong/Log4jConfig.java %}
{% endhighlight %}

{% highlight java %}
{% include_relative log_log4j_01_basic/src/main/java/com/luguosong/child/ChildLogger.java %}
{% endhighlight %}
