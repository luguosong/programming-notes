---
layout: note
title: 原型（Prototype）
nav_order: 50
parent: 创建型模式
grand_parent: 设计模式
create_time: 2023/5/31
---

# 意图

能够复制已有对象，而又无需使代码依赖它们所属的类。

# 结构

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230531184852.png)

1. `抽象原型（Prototype）`接口将对克隆方法进行声明。在绝大多数情况下，其中只会有一个名为 clone 克隆 的方法。
2. `具体原型（Concrete Prototype）`类将实现克隆方法。除了将原始对象的数据复制到克隆体中之外，该方法有时还需处理克隆过程中的极端情况，例如克隆关联对象和梳理递归依赖等等。
3. `客户端（Client）`可以复制实现了原型接口的任何对象。

# 示例

JDK的`Object类`已经帮我们默认实现了`抽象原型（Prototype）`,我们只需要编写`具体原型（Concrete Prototype）`即可

{% highlight java %}
{% include_relative PrototypeExample.java %}
{% endhighlight %}

# 深克隆

- 序列化（Serialization）方式来实现

{% highlight java %}
{% include_relative PrototypeDeepCloneExample.java %}
{% endhighlight %}
