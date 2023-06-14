---
layout: note
title: 桥接（Bridge）
nav_order: 20
parent: 结构型模式
grand_parent: 设计模式
create_time: 2023/6/7
---

# 意图

抽象通过其子类可以实现一个维度的扩展。

桥接可以将抽象部分进行多个维度的扩展。

# 不使用桥接的情况

- 不使用桥接的情况

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230607171812.png)

- 使用桥接的情况

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230607171826.png)

# 结构

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230607161527.png)

1. `抽象部分（Abstraction）`提供高层控制逻辑，依赖于完成底层实际工作的实现对象。
2. `实现部分（Implementation）`为所有具体实现声明通用接口。抽象部分仅能通过在这里声明的方法与实现对象交互。抽象部分可以列出和实现部分一样的方法，但是抽象部分通常声明一些复杂行为，这些行为依赖于多种由实现部分声明的原语操作。
3. `具体实现（Concrete Implementations）`中包括特定于平台的代码。
4. `精确抽象（Refined Abstraction）`提供控制逻辑的变体。与其父类一样，它们通过通用实现接口与不同的实现进行交互。
5. 通常情况下，`客户端（Client）`仅关心如何与抽象部分合作。但是，客户端需要将抽象对象与一个实现对象连接起来。

# 示例

{% highlight java %}
{% include_relative BridgeExample.java %}
{% endhighlight %}
