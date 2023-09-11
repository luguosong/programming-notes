---
layout: note
title: 面向对象
nav_order: 30
parent: JavaSE
create_time: 2023/8/8
---

# 类

{: .note-title}
> 类
> 
> 具有相同属性和行为的事物的抽象描述。

{% highlight java %}
{% include_relative class_demo/Phone.java %}
{% endhighlight %}

# 变量

按在类中声明的位置进行分类：

- 字段
- 局部变量
  - 形参
  - 方法内

不同点：

- 位置不同
- 内存中分配的位置不同：`字段`存储在堆中，`局部变量`存储于栈中
- 生命周期不一样：`字段`随对象创建而创建,`局部变量`随着方法调用在栈中创建
- 作用域不一样:`局部变量`仅作用于声明它的方法
- 是否有修饰符修饰:仅有`字段`才需要被修饰符修饰
- 是否有默认值:仅有`字段`有默认值

# 权限修饰符

- `public`:
- `protected`:
- `默认（空）`:
- `private`:

# 方法



# 对象

{: .nete-title}
> 对象
> 
> 实际存在的该类事物的每个个体，是`具体的`，因此也称为`实例(instance)`
