---
layout: note
title: 抽象工厂（Abstract Factory）
nav_order: 20
parent: 创建型模式
grand_parent: 设计模式
create_time: 2023/5/31
---

# 意图

能创建一系列相关的对象，而无需指定其具体类。

![](https://cdn.jsdelivr.net/gh/guosonglu/images@master/blog-img/20230531152711.png)

# 结构

![](https://cdn.jsdelivr.net/gh/guosonglu/images@master/blog-img/20230531150945.png)

1. `抽象产品（Abstract Product）`为每种产品声明接口，在抽象产品中声明了产品所具有的业务方法。
2. `具体产品（Concrete Product）`定义具体工厂生产的具体产品对象，实现在抽象产品接口中声明的业务方法。
3. `抽象工厂（Abstract Factory）`它声明了一组用于创建一族产品的方法，每个方法对应一种产品。
4. `具体工厂（Concrete Factory）`实现了在抽象工厂中声明的创建产品的方法，生成一组具体产品，这些产品构成了一个`产品族`
   ，每种产品都位于某个产品等级结构中。
5.
尽管具体工厂会对具体产品进行初始化，其构建方法签名必须返回相应的抽象产品。这样，使用工厂类的客户端代码就不会与工厂创建的特定产品变体耦合。`客户端（Client）`
只需通过抽象接口调用工厂和产品对象，就能与任何具体工厂/产品变体交互。

# 示例

{% highlight java %}
{% include_relative AbstractFactoryExample.java %}
{% endhighlight %}

# 利弊分析

- `单一职责原则`：将类的创建和使用分离
- `缺点`:如果需要增加新的产品时，需要修改抽象工厂接口及所有的具体工厂实现，这可能会导致一定的工作量,且`不满足开闭原则`
