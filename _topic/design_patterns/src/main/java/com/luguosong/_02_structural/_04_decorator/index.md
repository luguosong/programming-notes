---
layout: note
title: 装饰（Decorator）
nav_order: 40
parent: 结构型模式
grand_parent: 设计模式
create_time: 2023/5/26
---

# 意图

允许你通过将对象放入包含行为的特殊封装对象中来为原对象绑定新的行为。

# 不使用装饰

- 不使用装饰想对某个方法进行拓展，会导致扩展功能类爆炸式增长

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/diagrams/design-pattern/structural/%E4%B8%8D%E4%BD%BF%E7%94%A8%E8%A3%85%E9%A5%B0.svg)

# 结构

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230526141458.png)

1. `部件（Component）`声明封装器和被封装对象的公用接口。
2. `具体部件（Concrete Component）`类是被封装对象所属的类。它定义了基础行为，但装饰类可以改变这些行为。
3. `基础装饰（Base Decorator）`类拥有一个指向被封装对象的引用成员变量。该变量的类型应当被声明为通用部件接口，这样它就可以引用具体的部件和装饰。装饰基类会将所有操作委派给被封装的对象。
4. `具体装饰类（Concrete Decorators）`定义了可动态添加到部件的额外行为。具体装饰类会重写装饰基类的方法，并在调用父类方法之前或之后进行额外的行为。
5. `客户端（Client）`可以使用多层装饰来封装部件，只要它能使用通用接口与所有对象互动即可。

# 示例

{% highlight java %}
{% include_relative DecoratorExample.java %}
{% endhighlight %}

# 使用场景

## java.io.BufferedWriter

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230607155316.png)

