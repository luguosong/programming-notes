---
layout: note
title: 单例（Singleton）
nav_order: 60
parent: 创建型模式
grand_parent: 设计模式
create_time: 2023/5/31
---

# 意图

- 确保一个类只有`一个实例`，并提供一个全局访问点来访问该实例。

# 结构

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230531095557.png)

1. `单例（Singleton）`类声明了一个名为getInstance获取实例的静态方法来返回其所属类的一个相同实例。单例的`构造函数`
   必须对客户端（Client）代码`隐藏`。 调用
   获取实例方法必须是获取单例对象的`唯一方式`。

# 示例

- 饿汉式
- 线程不安全的懒汉式
- synchronized修饰方法的线程安全懒汉式
- 双重检查锁的线程安全懒汉式
- 静态内部类的线程安全懒汉式
- 枚举类型实现饿汉式

{% highlight java %}
{% include_relative SingletonExample.java %}
{% endhighlight %}

# 破坏单例模式

- 通过序列化
- 通过反射
    - 可以在私有构造中进行限制，达到防止反射破坏单例模式的目的



