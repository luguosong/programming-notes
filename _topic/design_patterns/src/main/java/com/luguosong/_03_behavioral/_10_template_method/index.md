---
layout: note
title: 模板方法（Template Method）
nav_order: 100
parent: 行为型模式
grand_parent: 设计模式
create_time: 2023/6/15
---

# 意图

在父类中定义一个算法骨架，算法的具体实现在子类中完成

# 结构

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202306151016691-%E6%A8%A1%E6%9D%BF%E6%96%B9%E6%B3%95%E7%BB%93%E6%9E%84.png)

1. `抽象类（AbstractClass）` 会声明作为算法步骤的方法， 以及依次调用它们的实际模板方法。 算法步骤可以被声明为抽象类型，也可以提供一些默认实现。
2. `具体类（ConcreteClass）`可以重写所有步骤，但不能重写模板方法自身。

# 示例

{% highlight java %}
{% include_relative TemplateMethodExample.java %}
{% endhighlight %}
