---
layout: note
title: 类
nav_order: 90
parent: JavaScript
create_time: 2023/5/12
---

# 类的定义

{: .note}
> 在面向对象的编程中，class 是用于创建对象的可扩展的程序代码模版，它为对象提供了`状态（成员变量）`的初始值和`行为（成员函数或方法）`的实现。

{: .important}
> 在js中，`类`意味着一组对象从同一个`原型对象`继承属性。因此，`原型对象`是`类`的核心特征和基础

# 定义类方式一：原型对象

一下采用一种`老方式`方法创建类：

{% highlight js %}
{% include_relative range1.js %}
{% endhighlight %}

- 这段代码定义了一个工厂函数 range，用于创建新的 Range 对象。
- 它使用 range 函数的 methods 属性保存定义这个类的原型对象。这种做法是常见的，将原型对象作为属性来存储。
- range 函数为每个 Range 对象定义了 from 和 to 属性。这两个属性是非共享、非继承属性，定义每个范围对象独有的状态。
- range.methods 对象使用了 ES6 定义方法的简写语法，省略了 function 关键字。
- 原型的方法中包括了一个特殊的方法 Symbol.iterator，它为 Range 对象定义了一个迭代器。这个方法的名字前面有一个星号 *，表示它是一个生成器函数，而非普通函数。
- 定义在 range.methods 中的共享方法会引用在 range 工厂函数中初始化的 from 和 to 属性，通过 this 关键字来引用调用它们的对象。使用 this 是所有类方法的基本特征。

# 定义类方式二：构造方法

{: .warning}
> 不同的构造函数创建的对象可能属于同一个类。因为不同名字的构造函数，绑定的prototype原型对象可能是同一个

{% highlight js %}
{% include_relative range2.js %}
{% endhighlight %}

# 定义类方式三：class关键字



# 为已有类添加方法
# 子类
