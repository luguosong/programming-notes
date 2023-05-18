---
layout: note
title: Log4j2
nav_order: 70
parent: Java日志框架
create_time: 2023/5/18
---

# 简介

- `事件不丢失`：Log4j 2在重新配置时不会丢失事件，确保日志记录的完整性。
- `异步日志记录`：Log4j 2引入了基于LMAX Disruptor库的异步日志记录器，具有更高的吞吐量和更低的延迟。
- `无垃圾生成`：对于独立应用程序，Log4j 2几乎是`无垃圾的`，在Web应用程序中的稳态日志记录过程中产生的垃圾也较低，减少了垃圾收集器的压力。
- `插件系统`：Log4j 2引入了插件系统，使得扩展和定制日志框架更加简单，可以轻松添加新的Appenders、Filters、Layouts、Lookups和Pattern Converters。
- `自定义日志级别`：Log4j 2支持自定义日志级别的定义，可以在代码或配置中灵活配置。
- `Lambda表达式`：Log4j 2对Java 8的lambda表达式提供了支持，可以在请求的日志级别启用时延迟构造日志消息，使得代码更简洁。
- `消息对象`：Log4j 2引入了消息对象的概念，允许传递和处理复杂的数据结构，用户可以创建自定义的消息类型，并编写自定义的Layouts、Filters和Lookups对其进行处理。
- `灵活的过滤器`：Log4j 2支持在事件处理前、处理中或Appender上配置过滤器，提供`更灵活的事件过滤功能`。
- `格式转换`：Log4j 2的大多数Appenders都支持Layouts，允许以各种所需的格式传输日志数据。
- `Syslog Appender改进`：Log4j 2的Syslog Appender支持TCP和UDP，并支持BSD syslog和RFC 5424格式。
- `并发性能改进`：Log4j 2利用Java 5的并发支持，并在尽可能低的级别上进行锁定，解决了Log4j 1.x存在的死锁问题。
- `Apache软件基金会项目`：Log4j 2是Apache软件基金会的项目，遵循ASF项目的社区和支持模型，提供广泛的社区支持和贡献机会。

# 入门案例

- 依赖

{% highlight xml %}
{% include_relative log4j2_basic/pom.xml %}
{% endhighlight %}

- 配置文件

{% highlight xml %}
{% include_relative log4j2_basic/src/main/resources/log4j2.xml %}
{% endhighlight %}

- 测试代码

{% highlight java %}
{% include_relative log4j2_basic/src/main/java/com/luguosong/Log4j2Demo.java %}
{% endhighlight %}

# 异步日志

- 依赖引入

{% highlight xml %}
{% include_relative log4j2_async/pom.xml %}
{% endhighlight %}

- 全局异步配置文件

{% highlight properties %}
{% include_relative log4j2_async/src/main/resources/log4j2.component.properties %}
{% endhighlight %}

- 其它方式异步配置文件

{% highlight xml %}
{% include_relative log4j2_async/src/main/resources/log4j2.xml %}
{% endhighlight %}

