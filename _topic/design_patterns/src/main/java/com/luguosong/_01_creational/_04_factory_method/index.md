---
layout: note
title: 工厂方法（Factory Method）
nav_order: 40
parent: 创建型模式
grand_parent: 设计模式
create_time: 2023/5/31
---

# 意图

在父类中提供一个创建对象的方法，允许子类决定实例化对象的类型。

# 结构

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230531142302.png)

1. `产品（Product）`将会对接口进行声明。对于所有由创建者及其子类构建的对象，这些接口都是通用的。
2. `具体产品（Concrete Products）`是产品接口的不同实现。
3. `创建者（Creator）`类声明返回产品对象的工厂方法。该方法的返回对象类型必须与产品接口相匹配。
4. `具体创建者（Concrete Creators）` 将会重写基础工厂方法，使其返回不同类型的产品。

# 示例

{% highlight java %}
{% include_relative FactoryMethodExample.java %}
{% endhighlight %}

# 利弊分析

- `开闭原则`：当需要创建新的产品时，只需要创建一个新的具体产品和具体创建者即可，不需要修改现有代码。
- `依赖倒置原则`：`创建者`依赖`抽象产品`，具体创建者依赖具体产品。
- `缺点`：每次创建新的产品时，都需要创建一个新的具体产品和具体创建者，增加了代码量。

# 使用场景

## Iterable和Iterator

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/diagrams/design-pattern/creational/%E5%B7%A5%E5%8E%82%E6%96%B9%E6%B3%95-Iterable%E7%A4%BA%E4%BE%8B.svg)
