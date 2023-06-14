---
layout: note
title: 组合（Composite）
nav_order: 30
parent: 结构型模式
grand_parent: 设计模式
create_time: 2023/6/8
---

# 意图

将对象组合成`树状结构`，并且能像使用独立对象一样使用它们。

# 结构

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202306081030572-%E7%BB%84%E5%90%88%E6%A8%A1%E5%BC%8F%E7%BB%93%E6%9E%84%E5%9B%BE.png)

1. `组件（Component）`接口描述了树中简单项目和复杂项目所共有的操作。
2. `叶节点（Leaf）`是树的基本结构，它不包含子项目。一般情况下，叶节点最终会完成大部分的实际工作，因为它们无法将工作指派给其他部分。
3. `容器（Container）`——又名`组合（Composite）`
   ——是包含叶节点或其他容器等子项目的单位。容器不知道其子项目所属的具体类，它只通过通用的组件接口与其子项目交互。容器接收到请求后会将工作分配给自己的子项目，处理中间结果，然后将最终结果返回给客户端。
4. `客户端（Client）`通过组件接口与所有项目交互。因此，客户端能以相同方式与树状结构中的简单或复杂项目交互。

# 示例

{% highlight java %}
{% include_relative CompositeExample.java %}
{% endhighlight %}




