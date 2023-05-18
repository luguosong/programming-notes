---
layout: note
title: Logback
nav_order: 60
parent: Java日志框架
create_time: 2023/5/14
---

# 简介

Logback是由log4j创始人设计的日志框架，是log4j的改进版，也是SpringBoot默认的日志框架。

Logback分为三个模块:

- `logback-core`:Logback框架的基础
- `logback-classic`:Logback框架的核心,`logback-classic`包含logback-core基础模块
- `logback-access`:与Servlet容器集成的模块

# 组件

- Logger:日志记录器
- Appender：指定日志输出目的地
- Layout：格式化日志输出，被封装在`encoder`中

# 入门案例

- 引入依赖

{% highlight xml %}
{% include_relative logback_basic/pom.xml %}
{% endhighlight %}

- 配置文件

{% highlight xml %}
{% include_relative logback_basic/src/main/resources/logback.xml %}
{% endhighlight %}

- 代码编写

{% highlight java %}
{% include_relative logback_basic/src/main/java/com/luguosong/LogBackDemo.java %}
{% endhighlight %}

# 异步日志

- 配置文件

{% highlight xml %}
{% include_relative logback_async/src/main/resources/logback.xml %}
{% endhighlight %}


# log4j配置文件转换

官网提供了将log4j转换为logback配置文件的工具，[log4j.properties to logback.xml Translator](https://logback.qos.ch/translator/services/propertiesTranslator.html)
