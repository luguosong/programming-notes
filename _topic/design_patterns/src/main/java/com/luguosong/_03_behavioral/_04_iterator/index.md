---
layout: note
title: 迭代器（Iterator）
nav_order: 40
parent: 行为型模式
grand_parent: 设计模式
create_time: 2023/6/19
---

# 意图

将迭代器功能与被迭代对象分离。

# 结构

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202306192300564-%E8%BF%AD%E4%BB%A3%E5%99%A8%E7%BB%93%E6%9E%84.png)

1. `迭代器（Iterator）`接口声明了遍历集合所需的操作：获取下一个元素、获取当前位置和重新开始迭代等。
2. `具体迭代器（Concrete Iterators）`实现遍历集合的一种特定算法。迭代器对象必须跟踪自身遍历的进度。这使得多个迭代器可以相互独立地遍历同一集合。
3. `集合（Collection）`接口声明一个或多个方法来获取与集合兼容的迭代器。请注意，返回方法的类型必须被声明为迭代器接口，因此具体集合可以返回各种不同种类的迭代器。
4. `具体集合（Concrete Collections）`会在客户端请求迭代器时返回一个特定的具体迭代器类实体。你可能会琢磨，剩下的集合代码在什么地方呢？不用担心，它也会在同一个类中。只是这些细节对于实际模式来说并不重要，所以我们将其省略了而已。
5. `客户端（Client）`通过集合和迭代器的接口与两者进行交互。这样一来客户端无需与具体类进行耦合，允许同一客户端代码使用各种不同的集合和迭代器。客户端通常不会自行创建迭代器，而是会从集合中获取。但在特定情况下，客户端可以直接创建一个迭代器（例如当客户端需要自定义特殊迭代器时）。


# 示例

{% highlight java %}
{% include_relative IteratorExample.java %}
{% endhighlight %}


