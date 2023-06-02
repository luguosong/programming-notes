---
layout: note
title: 建造者（Builder）
nav_order: 30
parent: 创建型模式
grand_parent: 设计模式
create_time: 2023/5/31
---

# 意图

将复杂对象的`构建（Builder）`和`装配(Director)`分离。适用于某些对象构建复杂的情况。

建造者创建的是结构复杂的对象

# 结构

![](https://cdn.jsdelivr.net/gh/guosonglu/images@master/blog-img/20230601090305.png)

1. `生成器（Builder）`接口声明在所有类型生成器中通用的产品构造步骤。
2. `具体生成器（Concrete Builders）`提供构造过程的不同实现。具体生成器也可以构造不遵循通用接口的产品。
3. `产品（Products）`是最终生成的对象。由不同生成器构造的产品无需属于同一类层次结构或接口。
4. `主管（Director）`类定义调用构造步骤的顺序，这样你就可以创建和复用特定的产品配置。
5. `客户端（Client）`必须将某个生成器对象与主管类关联。一般情况下，你只需通过主管类构造函数的参数进行一次性关联即可。此后主管类就能使用生成器对象完成后续所有的构造任务。
   但在客户端将生成器对象传递给主管类制造方法时还有另一种方式。在这种情况下，你在使用主管类生产产品时每次都可以使用不同的生成器。

# 示例

{% highlight java %}
{% include_relative BuilderExample.java %}
{% endhighlight %}

# 利弊分析

- `单一原则`：将部件的创建和组装分离
- `开闭原则`：可以通过增加`具体建造者`类扩展新的产品
