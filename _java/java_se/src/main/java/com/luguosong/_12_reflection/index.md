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

`java.lang.Class`用于描述`类`的类，它是一个`final`类，因此不能被继承。

将.class字节码文件加载到内存中，创建一个`Class`类对象，就可以使用这个对象来获取类的信息。Class对象可以看作反射的源头。

同一个类创建的多个对象，在内存中只有一个`Class`对象。

{% highlight java %}
{% include_relative ClassDemo.java %}
{% endhighlight %}

`Class对象`是在类加载的过程中自动产生的，而不是像普通对象那样通过构造方法创建的。因此，对于Class对象我们只关注如何获取而不关注它是如何创建的。多种方式获取Class实例：

{% highlight java %}
{% include_relative GetClass.java %}
{% endhighlight %}

# 创建运行时类的对象

{% highlight java %}
{% include_relative CreateObject.java %}
{% endhighlight %}

# 获取运行时类的完整结构

- 获取所有public属性
- 获取所有属性
- 获取当前类以及父类的公共方法
- 获取当前类声明的所有方法
- 获取父类信息，包含父类泛型
- 获取实现的接口
- 获取类所在包

{% highlight java %}
{% include_relative GetClassStructure.java %}
{% endhighlight %}

# 调用指定结构

{% highlight java %}
{% include_relative InvokeStructure.java %}
{% endhighlight %}
