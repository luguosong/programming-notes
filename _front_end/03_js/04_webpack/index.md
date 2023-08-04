---
layout: note
title: webpack
nav_order: 40
parent: JavaScript
create_time: 2023/8/3
---

# 文件划分方式模块化

每个文件就是一个独立的模块，使用script标签将模块引入到页面中。

存在的问题：

- 污染全局作用域，模块中的内容可以被任意访问和修改
- 命名冲突问题
- 无法管理模块依赖关系

页面调用：

{% highlight html %}
{% include_relative modularization_with_file/index.html %}
{% endhighlight %}

模块a：

{% highlight js %}
{% include_relative modularization_with_file/module_a.js %}
{% endhighlight %}

模块b:

{% highlight js %}
{% include_relative modularization_with_file/module_b.js %}
{% endhighlight %}

# 命名空间方式模块化

每个模块只暴露一个全局的对象，所有的成员都挂载到这个对象下面。

存在的问题：
- 依然污染全局作用域，模块中的内容可以被任意访问和修改
- 模块之间的依赖关系也没有得到解决

页面调用：

{% highlight html %}
{% include_relative modularization_with_global_objects/index.html %}
{% endhighlight %}

模块a：

{% highlight js %}
{% include_relative modularization_with_global_objects/module_a.js %}
{% endhighlight %}

模块b:

{% highlight js %}
{% include_relative modularization_with_global_objects/module_b.js %}
{% endhighlight %}

# IIFE方式模块化

使用立即执行函数为函数提供私有空间。将模块中每一个成员都放在一个函数提供的私有作用域当中。对于需要暴露给外部的成员，可以挂载到全局对象上。

页面调用：

{% highlight html %}
{% include_relative modularization_with_iife/index.html %}
{% endhighlight %}

模块a：

{% highlight js %}
{% include_relative modularization_with_iife/module_a.js %}
{% endhighlight %}

模块b:

{% highlight js %}
{% include_relative modularization_with_iife/module_b.js %}
{% endhighlight %}

# nodejs中的模块化

# AMD模块化规范
 

