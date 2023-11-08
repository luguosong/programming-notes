---
layout: note
title: 反射
nav_order: 120
parent: JavaSE
latex: true
create_time: 2023/11/7
---

# 概述

`反射`是一种机制，允许程序在`运行时`获取类的信息，但访问和操作对象的内部`属性`和`方法`仍然受到访问控制和安全性的限制。

# 基本使用

功能：
- 调用`类`公共构造方法创建对象
- 调用`类`私有构造方法创建对象
- 获取`对象`公共成员变量
- 获取`对象`私有成员变量
- 调用`对象`公共成员方法
- 调用`对象`私有成员方法

{% highlight java %}
{% include_relative ReflectionHello.java %}
{% endhighlight %}

# Class类

将.class字节码文件加载到内存中，创建一个`Class`类对象，就可以使用这个对象来获取类的信息。

同一个类创建的多个对象，在内存中只有一个`Class`对象。

{% highlight java %}
{% include_relative ClassDemo.java %}
{% endhighlight %}

