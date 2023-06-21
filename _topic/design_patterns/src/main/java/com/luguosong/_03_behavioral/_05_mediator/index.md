---
layout: note
title: 中介者（Mediator）
nav_order: 50
parent: 行为型模式
grand_parent: 设计模式
create_time: 2023/6/19
---

# 意图

集中处理相关对象之间复杂的沟通和控制方式

# 结构

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202306191413620-%E4%B8%AD%E4%BB%8B%E8%80%85%E6%A8%A1%E5%BC%8F%E7%BB%93%E6%9E%84.png)

1. `组件（Component）`是各种包含业务逻辑的类。每个组件都有一个指向中介者的引用，该引用被声明为中介者接口类型。组件不知道中介者实际所属的类，因此你可通过将其连接到不同的中介者以使其能在其他程序中复用。
2. `中介者（Mediator）`接口声明了与组件交流的方法，但通常仅包括一个通知方法。组件可将任意上下文（包括自己的对象）作为该方法的参数，只有这样接收组件和发送者类之间才不会耦合。
3. `具体中介者（Concrete Mediator）`封装了多种组件间的关系。具体中介者通常会保存所有组件的引用并对其进行管理，甚至有时会对其生命周期进行管理。
4. 组件并不知道其他组件的情况。如果组件内发生了重要事件，它只能通知中介者。中介者收到通知后能轻易地确定发送者，这或许已足以判断接下来需要触发的组件了。

# 示例

{% highlight java %}
{% include_relative MediatorExample.java %}
{% endhighlight %}
