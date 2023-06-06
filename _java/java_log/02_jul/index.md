---
layout: note
title: JUL
nav_order: 20
parent: Java日志框架
create_time: 2023/4/17
---

# 概述

Jul（全称`Java Util Logging`）是Java平台的一个标准日志框架，它提供了一组API，可用于在应用程序中记录日志信息。

Jul已经成为`Java SE的一部分`，因此不需要任何外部库或框架来使用它。

# 日志组件

Jul框架的核心组件包括`Logger`、`Handler`、`Formatter`和`Filter`。

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230419090143.png)

{: .note}
> `Logger`用于记录日志
> `Handler`用于将日志输出到指定位置
> `Formatter`用于将日志格式化
> `Filter`用于过滤日志

- `Logger`是日志记录器，它用于`记录`应用程序的日志消息。
- `Handler`负责将日志消息发送到适当的位置，例如文件、控制台或数据库。一个`Logger`可以对应多个`Handler`。
- `Formatter`负责将日志消息转换为特定的格式，以便更好地阅读和分析。
- `Filter`用于过滤特定级别的日志消息。

# 日志级别

Jul支持多种`日志级别`，包括`SEVERE`、`WARNING`、`INFO`、`CONFIG`、`FINE`、`FINER`和`FINEST`。可以根据需要选择适当的日志级别来记录日志。

- `SEVERE`：此级别表示严重的问题，通常导致应用程序无法继续运行。例如：系统崩溃、严重的资源不足等情况。
- `WARNING`：此级别表示潜在的问题，可能会影响程序的正常运行，但不会导致程序崩溃。例如：使用了废弃的API、未正确关闭资源等情况。
- `INFO`：此级别用于记录程序正常运行时的一般信息，通常用于生产环境的日志记录。例如：服务启动/停止、用户登录/登出等情况。
- `CONFIG`：此级别用于记录系统的配置信息，例如：配置文件加载、系统参数设置等情况。
- `FINE`：此级别用于记录较详细的程序运行过程信息，通常用于开发和调试。例如：方法入口/出口、循环遍历等情况。
- `FINER`：此级别比FINE更详细，用于记录更为详尽的程序运行过程信息。例如：变量值改变、数据结构操作等情况。
- `FINEST`：此级别是最详细的日志级别，用于记录非常详尽的程序运行过程信息。例如：每次循环迭代、最底层的方法调用等情况。

# 入门案例

{% highlight java %}
{% include_relative log_jul_01_basic/src/main/java/com/luguosong/JulHello.java %}
{% endhighlight %}

# 输出日志到文件

{: .note}
> 可以设置多个日志处理器，将日志输出到多个地方

{% highlight java %}
{% include_relative log_jul_01_basic/src/main/java/com/luguosong/HandlerToFile.java %}
{% endhighlight %}

# 日志处理器父子关系

{: .note-title}
> 父子关系的作用
>
> 父级的配置可以作用到子级

`java.util.logging.LogManager.RootLogger`是所有日志记录器的父级。

```java
class Demo {
    Logger logger1 = Logger.getLogger("com.luguosong"); //父级
    Logger logger2 = Logger.getLogger("com.luguosong.HandlerToFile"); //子级
}
```

# 配置文件

Jul还支持日志`配置文件`，以便更灵活地控制日志记录的行为。可以在配置文件中指定要使用的Handler、Formatter和Filter，以及每个组件的属性。

## JDK中自带的配置文件

在jdk目录下存在`logging.properties`配置文件，不同版本的jdk文件位置不一样。

```properties
############################################################
#  	默认日志记录配置文档
#
# 您可以通过使用 java.util.logging.config.file 
# 系统属性指定文件名来使用不同的文件。
# 例如，java -Djava.util.logging.config.file=myfile
############################################################
############################################################
#  	全局属性
############################################################
# handlers指定一个以逗号分隔的日志处理程序列表。这些处理程序将在VM启动期间安装。
# 请注意，这些类必须在系统类路径上。
# 默认情况下，我们只配置ConsoleHandler，它只会显示INFO及以上级别的消息。
handlers=java.util.logging.ConsoleHandler
# 要同时添加FileHandler，请改用以下行。
#handlers= java.util.logging.FileHandler, java.util.logging.ConsoleHandler
# 这指定了要记录所有记录器的事件类型。对于任何给定的设施，此全局级别
# 可以被设施特定的级别覆盖
# 请注意，ConsoleHandler也有一个单独的级别设置，用于限制打印到控制台的消息。
.level=INFO
############################################################
# 处理程序特定属性
# 描述处理程序的特定配置信息。
############################################################
# 默认的文件输出位于用户的主目录中。
java.util.logging.FileHandler.pattern=%h/java%u.log
java.util.logging.FileHandler.limit=50000
java.util.logging.FileHandler.count=1
# FileHandler可以同步获取的默认锁数。
# 这指定了通过增加唯一字段%u实现的FileHandler API文档来获取锁文件的最大尝试次数。
java.util.logging.FileHandler.maxLocks=100
java.util.logging.FileHandler.formatter=java.util.logging.XMLFormatter
# 限制在控制台上打印的消息仅为INFO及以上级别。
java.util.logging.ConsoleHandler.level=INFO
java.util.logging.ConsoleHandler.formatter=java.util.logging.SimpleFormatter
# 自定义SimpleFormatter输出格式的示例，以打印以下单行日志消息：
# <level>: <log message> [<date/time>]
#
# java.util.logging.SimpleFormatter.format=%4$s: %5$s [%1$tc]%n
############################################################
# 设施特定属性。
# 为每个记录器提供额外的控制。
############################################################
# 例如，将com.xyz.foo记录器设置为仅记录SEVERE消息
# com.xyz.foo.level = SEVERE
```

## 添加自定义配置文件

{: .note}
> 通过`java.util.logging.LogManager`读取配置文件

配置文件：

{% highlight properties %}
{% include_relative log_jul_02_custom_configuration_file/src/main/resources/log.properties %}
{% endhighlight %}

Java指定配置文件：

{% highlight java %}
{% include_relative log_jul_02_custom_configuration_file/src/main/java/com/lugusong/CustomConfiguration.java %}
{% endhighlight %}
