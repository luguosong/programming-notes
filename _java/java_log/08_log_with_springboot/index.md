---
layout: note
title: SpringBoot中使用日志框架
nav_order: 80
parent: Java日志框架
create_time: 2023/5/18
---

# 默认

spring boot默认使用`slf4j`+`logback`的日志框架，不需要额外配置。

# 简单配置

一些简单的日志配置可以直接在`application.yml`中完成

{% highlight yaml %}
{% include_relative log_with_springboot/src/main/resources/application.yml %}
{% endhighlight %}

# 切换log4j2

- 配置依赖
  - 去除spring boot中的logback部分
  - 添加log4j2依赖

{% highlight xml %}
{% include_relative log_with_springboot/pom.xml %}
{% endhighlight %}
