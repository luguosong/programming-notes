---
layout: note
title: SLF4J
nav_order: 50
parent: Java日志框架
create_time: 2023/5/5
---

# 概述

JAVA简易日志门面（Simple Logging Facade for Java，缩写`SLF4J`），是一套包装Logging框架的界面程式，以`外观模式`
实现。可以在软件部署的时候决定要使用的Logging框架，目前主要支援的有`Java Logging API`、`log4j`及`logback`等框架。以MIT授权方式发布。

# 日志门面级别

- ERROR
- WARN
- INFO
- DEBUG
- TRACE

# slf4j日志实现分类

`slf4j`至少需要导入一种日志实现，否则会报错

`slf4j`将优先使用先导入的日志实现

- 无日志实现
    - slf4j将提示`No SLF4J providers were found.`
- 无需适配器
    - nop
    - slf4j simple
    - logback
- 需要适配器
  - JUL
  - log4j

# slf4j + nop

集成`nop`表示不输出日志

- 引入依赖

{% highlight xml %}
{% include_relative log_slf4j_02_nop/pom.xml %}
{% endhighlight %}

- 代码

{% highlight java %}
{% include_relative log_slf4j_02_nop/src/main/java/com/luguosong/Slf4jWithNop.java %}
{% endhighlight %}

# slf4j + slf4j simple

`slf4j-simple`是`slf4j`自带的简单日志实现,只需要引入`slf4j simple`依赖就能使用

- 引入依赖

{% highlight xml %}
{% include_relative log_slf4j_01_basic/pom.xml %}
{% endhighlight %}

- 代码

{% highlight java %}
{% include_relative log_slf4j_01_basic/src/main/java/HelloSlf4j.java %}
{% endhighlight %}

# slf4j + logback

只需要引入`logback`依赖就能使用

- 引入依赖

{% highlight xml %}
{% include_relative log_slf4j_03_logback/pom.xml %}
{% endhighlight %}

- 代码

{% highlight java %}
{% include_relative log_slf4j_03_logback/src/main/java/com/luguosong/slf4jWithLogback.java %}
{% endhighlight %}

# slf4j + log4j

slf4j集成log4j,如果不添加`适配器`依赖，将报如下错误：

````shell
SLF4J: No SLF4J providers were found.
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See https://www.slf4j.org/codes.html#noProviders for further details.
````

- 导入依赖

{% highlight xml %}
{% include_relative log_slf4j_04_log4j/pom.xml %}
{% endhighlight %}

- 代码

{% highlight java %}
{% include_relative log_slf4j_04_log4j/src/main/java/Slf4jWithLog4j.java %}
{% endhighlight %}

# slf4j + jul

与log4j类似，slf4j集成jul也需要添加`适配器`依赖

- 导入依赖

{% highlight xml %}
{% include_relative log_slf4j_05_jul/pom.xml %}
{% endhighlight %}

- 代码

{% highlight java %}
{% include_relative log_slf4j_05_jul/src/main/java/com/luguosong/Slf4jWithJul.java %}
{% endhighlight %}

# log4j 转 logback

- 依赖

{% highlight xml %}
{% include_relative log_log4j_to_logback_by_slf4j/pom.xml %}
{% endhighlight %}

- 代码

{% highlight java %}
{% include_relative log_log4j_to_logback_by_slf4j/src/main/java/com/luguosong/Log4jToLogback.java %}
{% endhighlight %}
