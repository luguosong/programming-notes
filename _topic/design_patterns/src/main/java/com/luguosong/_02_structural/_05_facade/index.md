---
layout: note
title: 外观(Facade)
nav_order: 50
parent: 结构型模式
grand_parent: 设计模式
create_time: 2023/5/5
---

# 意图

为子系统中的`一组接口`提供一个`一致的界面`

定义了一个`高层接口`，这个接口使得`程序库`、`框架`或其他`复杂类`更加容易使用。

# 不使用外观

- 客户端必须要了解子系统的工作原理和各个类之间的协作关系，才能够使用子系统。子系统越复杂，其使用越困难。
- 子系统中的变化会影响到直接使用它的客户端，需要对客户端进行修改。

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/diagrams/design-pattern/structural/%E4%B8%8D%E4%BD%BF%E7%94%A8%E5%A4%96%E8%A7%82%E6%A8%A1%E5%BC%8F.svg)

{% highlight java %}
{% include_relative WithoutFacadeExample.java %}
{% endhighlight %}

# 外观结构

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230526092405.png)

1. `外观（Facade）`提供了一种访问特定子系统功能的便捷方式，
   其了解如何重定向客户端请求，知晓如何操作一切活动部件。
2. `复杂子系统（Complex Subsystem）`由数十个不同对象构成。
   如果要用这些对象完成有意义的工作，你必须深入了解子系
   统的实现细节，比如按照正确顺序初始化对象和为其提供正
   确格式的数据。
   子系统类不会意识到外观的存在，它们在系统内运作并且相
   互之间可直接进行交互。
3. `客户端（Client）`使用外观代替对子系统对象的直接调用。

# 示例

{% highlight java %}
{% include_relative FacadeExample.java %}
{% endhighlight %}
