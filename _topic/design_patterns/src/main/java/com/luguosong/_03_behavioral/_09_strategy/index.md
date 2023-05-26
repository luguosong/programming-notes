---
layout: note
title: 策略（Strategy）
nav_order: 90
parent: 行为型模式
grand_parent: 设计模式
create_time: 2023/5/26
---

# 意图

能让你定义一系列算法，并将每种算法分别放入独立的类中，以使算法的对象能够相互替换。

# 不使用策略示例

- 一旦需要增加新策略，就需要修改Context类，不符合`开闭原则`。
- Context类中的代码会变得越来越复杂。

{% highlight java %}
{% include_relative WithoutStrategyExample.java %}
{% endhighlight %}

# 结构

![](https://cdn.jsdelivr.net/gh/guosonglu/images@master/blog-img/20230526094426.png)

1. `上下文（Context）`维护指向具体策略的引用，且仅通过策略接口与该对象进行交流。
2. `策略（Strategy）`接口是所有具体策略的通用接口，它声明了一个上下文用于执行策略的方法。
3. `具体策略（Concrete Strategies）`实现了上下文所用算法的各种不同变体。
4. 当上下文需要运行算法时，它会在其已连接的策略对象上调用执行方法。上下文不清楚其所涉及的策略类型与算法的执行方式。
5. `客户端（Client）`会创建一个特定策略对象并将其传递给上下文。上下文则会提供一个设置器以便客户端在运行时替换相关联的策略。

# 使用策略示例

{% highlight java %}
{% include_relative StrategyExample.java %}
{% endhighlight %}
