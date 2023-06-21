---
layout: note
title: 责任链（Chain of Responsibility）
nav_order: 10
parent: 行为型模式
grand_parent: 设计模式
create_time: 2023/6/15
---

# 意图

允许你将请求沿着处理者链进行发送。收到请求后，每个处理者均可对请求进行处理，或将其传递给链上的下个处理者

# 结构

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202306151602349-%E8%B4%A3%E4%BB%BB%E9%93%BE%E6%A8%A1%E5%BC%8F%E7%BB%93%E6%9E%84.png)

1. `处理者（Handler）`声明了所有具体处理者的通用接口。该接口通常仅包含单个方法用于请求处理，但有时其还会包含一个设置链上下个处理者的方法。
2. 基础处理者（Base Handler）是一个可选的类，你可以将所有处理者共用的样本代码放置在其中。

   通常情况下，该类中定义了一个保存对于下个处理者引用的成员变量。客户端可通过将处理者传递给上个处理者的构造函数或设定方法来创建链。该类还可以实现默认的处理行为：确定下个处理者存在后再将请求传递给它。
3. `具体处理者（Concrete Handlers）`包含处理请求的实际代码。每个处理者接收到请求后，都必须决定是否进行处理，以及是否沿着链传递请求。

   处理者通常是独立且不可变的，需要通过构造函数一次性地获得所有必要地数据。
4. `客户端（Client）`可根据程序逻辑一次性或者动态地生成链。值得注意的是，请求可发送给链上的任意一个处理者，而非必须是第一个处理者。

# 示例

{% highlight java %}
{% include_relative ChainOfResponsibilityExample.java %}
{% endhighlight %}

# 适用场景

## 
