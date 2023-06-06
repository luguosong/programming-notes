---
layout: note
title: 代理（Proxy）
nav_order: 70
parent: 结构型模式
grand_parent: 设计模式
create_time: 2023/5/28
---

# 意图

- 提供一个代理对象来`控制对原始对象的访问`。
- 添加额外的功能。

# 不使用代理

# 代理结构

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230529151436.png)

1. `服务接口（Service Interface）`声明了服务接口。代理必须遵循该接口才能伪装成服务对象。
2. `具体服务（Service）`类提供了一些实用的业务逻辑。
3. `代理（Proxy）`类包含一个指向服务对象的引用成员变量。代理完成其任务（例如延迟初始化、记录日志、访问控制和缓存等）后会将请求传递给服务对象。通常情况下，代理会对其服务对象的整个生命周期进行管理。
4. `客户端（Client）` 能通过同一接口与服务或代理进行交互，所以你可在一切需要服务对象的代码中使用代理。

# 示例

{% highlight java %}
{% include_relative ProxyExample.java %}
{% endhighlight %}

# JDK动态代理

{% highlight java %}
{% include_relative ProxyJDKExample.java %}
{% endhighlight %}

# Cglib动态代理

{: .warning}
> JDK17和cglib一起会报错

{% highlight java %}
{% include_relative ProxyCglibExample.java %}
{% endhighlight %}

# 利弊分析

- 单一职责原则：额外功能封装到代理类中，`具体服务（Service）`只关心自己的核心职责
- 开闭原则：通过增加代理类来扩展`具体服务（Service）`的行为，无需修改`具体服务（Service）`或客户端
- 代理类在客户端和`具体服务（Service）`之间起到中介作用，降低了系统的耦合度
