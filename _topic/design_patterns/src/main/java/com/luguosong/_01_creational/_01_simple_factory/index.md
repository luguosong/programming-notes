---
layout: note
title: 简单工厂（Simple Factory）
nav_order: 10
parent: 创建型模式
grand_parent: 设计模式
create_time: 2023/5/30
---

# 意图

- 将代码的创建和使用分离
- 根据参数的不同返回不同类的实例

# 结构

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230530224000.png)

- `Factory（具体工厂角色）`：负责实现创建所有产品实例的内部逻辑。在工厂类中提供了`静态的`
  工厂方法factoryMethod（），它的返回类型为抽象产品类型Product。
- `Product（抽象产品角色）`：它是工厂类所创建的所有对象的父类，封装了各种产品对象的公有方法。
- `ConcreteProduct（具体产品角色）`：它是简单工厂模式的创建目标，所有被创建的对象都充当这个角色的某个具体类的实例。

# 示例

{% highlight java %}
{% include_relative SimpleFactoryExample.java %}
{% endhighlight %}

# 利弊分析

- `单一职责原则`：将`对象的创建`过程封装在一个专门的工厂类中，该类负责对象的创建，实现了创建对象的职责分离。
- `开闭原则`：
    - 当新增产品时，客户端不需要做任何修改（只需要传入不同的参数即可）
    - 当新增产品时，工厂类需要进行修改，`不满足开闭原则`
- `依赖倒置原则`：客户端只需要依赖抽象产品角色 ，不需要依赖具体产品类，可以降低客户端与具体产品的耦合性。

# 配置文件+反射

这解决了简单工厂不满足`开闭原则`的问题

```properties
productA=com.luguosong._01_creational._01_simple_factory.ConcreteProductA
productB=com.luguosong._01_creational._01_simple_factory.ConcreteProductB
```

{% highlight java %}
{% include_relative SimpleFactoryWithReflection.java %}
{% endhighlight %}
