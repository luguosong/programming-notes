---
layout: note
title: 对象
nav_order: 60
parent: JavaScript
create_time: 2023/5/5
---

# 对象简介

对象是一个属性的`无序集合`，每个属性都有`名字`和`值`，属性名通常是字符串，即将字符串映射为值。

对象可以从其他对象`继承`属性，这个对象称为其`原型`，并且方法通常是继承来的属性。JavaScript使用术语`自有属性`指代非继承属性。

JavaScript对象是`动态的`，即可以动态添加和删除属性。

每个属性还有3个属性特性：
- `writable（可写）`特性指定是否可以设置属性的值；
- `enumerable（可枚举）`特性指定是否可以在for/in循环中返回属性的名字；
- `configurable（可配置）`特性指定是否可以删除属性，以及是否可修改其特性。

# 全局对象

- 全局常量：undefined、Infinity、NaN
- 全局函数：isNaN()、parseInt()、eval()
- 构造函数：Date()、RegExp()、String()、Object()、Array()、Function()
- 全局对象：Math、JSON

# 原型

JavaScript中所有的对象都有一个`内置属性`，称为它的`prototype（原型）`。它本身是一个对象，故原型对象也会有它自己的原型，逐渐构成了`原型链`。原型链终止于拥有null作为其原型的对象上。

有个对象叫`Object.prototype`，它是最基础的原型，所有对象默认都拥有它。`Object.prototype`的原型是`null`，所以它位于原型链的终点：

# 创建对象

{% highlight js %}
{% include_relative objectCreate.js %}
{% endhighlight %}

# 属性操作

{% highlight js %}
{% include_relative propertyOperations.js %}
{% endhighlight %}

# 扩展对象

把一个对象的属性复制到另一个对象上

{% highlight js %}
{% include_relative assign.js %}
{% endhighlight %}

# 序列化对象

`对象序列化`是将对象转为字符串的过程，之后可以从中恢复对象的状态。

{% highlight js %}
{% include_relative serializingObjects.js %}
{% endhighlight %}

# 对象方法

{% highlight js %}
{% include_relative objectMethods.js %}
{% endhighlight %}

# 对象字面量扩展语法

{% highlight js %}
{% include_relative extendedObjectLiteralSyntax.js %}
{% endhighlight %}
