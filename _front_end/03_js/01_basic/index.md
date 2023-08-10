---
layout: note
title: js基础
nav_order: 10
parent: JavaScript
create_time: 2023/8/2
---

# JavaScript 简介

## 什么是 JavaScript？

- 是一门动态、解释型语言
- 从Scheme借鉴了一类（first class）函数
- 从Self借鉴了基于原型（prototype）的继承

JavaScript最初被创建的目的是为了`使网页更生动`。这种编程语言写出来的程序被称为`脚本`
。脚本可以直接写在网页的HTML中，在页面加载时自动执行。脚本以纯文本的形式提供和执行，不需要特殊的准备或编译即可运行。

如今，JavaScript 不仅可以在浏览器中执行，也可以在服务端执行，甚至可以在任意搭载了 `JavaScript 引擎` 的设备中执行。

浏览器中嵌入了 `JavaScript 引擎`

Node.js也是是一个基于Chrome `V8引擎`的JavaScript运行环境。

## 引擎工作原理

1. 引擎读取脚本
2. 引擎将脚本转化为机器语言
3. 机器代码快速地执行

## JavaScript语言和宿主环境

- `语言本身`：可以操作数值、文本，数组、 集合、映射等，但`不包含任何输入和输出功能`
- `宿主环境`
    - `浏览器`
        - 可以从用户的鼠标和键盘或者通过发送HTTP请求获取输入
        - 也允许JavaScript代码通过HTML和CSS向用户显示输出
    - `Node`
        - 访问整个操作系统的权限
        - 允许JavaScript程序读写文件
        - 通过网络发送和接收数据，以及发送和处理HTTP请求
        - 实现Web服务器
        - 编写可以替代shell脚本的简单实用脚本

# JavaScript上层语言

在`JavaScript 引擎`执行前编译成JavaScript的语言

- CoffeeScript
- TypeScript
- Dart
- Bython
- Kotlin

# 注释

```javascript
// 单行注释

/*
多行注释
多行注释
 */
```

# 严格模式

{% highlight html %}
{% include_relative use_strict.html %}
{% endhighlight %}

<iframe src="use_strict.html"></iframe>

# 标识符

- 标识符命名
    - 必须以字母、下划线`_`或美元符号`$`开头
    - 后续字符可以是字母、数字，下划线或美元符号
    - 数字不能作为第一个字符,以便区分`标识符`和`数值`

# 保留字

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230505154522.png)

JavaScript也保留或限制对某些关键字的使用，这些关键字当前`并未被语言所用`，但将来某个版本有可能会用到

| enum | implements | interface | package | private | protected | public |

由于历史原因，某些情况下也不允许用`arguments`和`eval`作为标识符，因此最好不要使用。

# 可选的分号

如果两条语句分别写在两行，通常可以省略它们之间的分号

# 变量

{: .note-title}
> 变量
>
> 用于存储数据的容器

命名规范：

- 变量名称必须仅包含字母、数字、符号 $ 和 _。
- 首字符必须非数字。
- 常采用小驼峰式命名法
- 美元符号 `$` 和下划线 `_` 也可以用于变量命名
- 区分大小写
- 允许非英文字母，但不推荐

{: .note-title}
> let和var的区别
>
> - var可以先使用后声明，let不行
> - var声明过的变量可以重复声明，let不行
> - var有变量提升、全局变量污染的问题，let没有
> - let有块级作用域，var没有

{% highlight html %}
{% include_relative variant.html %}
{% endhighlight %}

<iframe src="variant.html"></iframe>

# 声明提升

{: .note-title}
> 声明提升
>
> JavaScript 中，函数及var变量的声明都将被提升到函数的最顶部。
>
> JavaScript 中，var变量可以在使用后声明，也就是变量可以先使用再声明。

源码：

```javascript
x = 5; // 初始化 x
console.log(x) // 5
fun1(); // 调用 fun1 函数

var x; // 声明 x

function fun1() {
  console.log('fun1')
}
```

实际执行：

```javascript
var x; // 声明会被提升到最前面

// 函数声明也会提升到前面
function fun1() {
  console.log('fun1')
}

x = 5; // 初始化 x
console.log(x) // 5
fun1(); // 调用 fun1 函数
```

{: .note}
> 变量提升仅限于var声明，let声明不会提升

# 常量

{% highlight html %}
{% include_relative const.html %}
{% endhighlight %}

# 数组

## 概述

{: .note-title}
> 数组
>
> 将一组数据存储在单个变量名下

{% highlight html %}
{% include_relative array.html %}
{% endhighlight %}

<iframe src="array.html"></iframe>

## forEach遍历

{% highlight html %}
{% include_relative array_forEach.html %}
{% endhighlight %}

<iframe src="array_forEach.html"></iframe>

## map遍历并返回新数组

{% highlight html %}
{% include_relative array_map.html %}
{% endhighlight %}

<iframe src="array_map.html"></iframe>

## filter过滤

{% highlight html %}
{% include_relative array_filter.html %}
{% endhighlight %}

<iframe src="array_filter.html"></iframe>

## reduce累加

{% highlight html %}
{% include_relative array_reduce.html %}
{% endhighlight %}

<iframe src="array_reduce.html"></iframe>

## 数组展开

{% highlight html %}
{% include_relative expanding_the_array.html %}
{% endhighlight %}

<iframe src="expanding_the_array.html"></iframe>

# 数据类型

## 数据类型概述

- 基本数据类型
    - number 数字型
    - string 字符串型
    - boolean 布尔型
    - undefined 未定义型
    - null 空类型
- 引用数据类型
    - 对象
    - 函数
    - 数组

{% highlight html %}
{% include_relative data_type.html %}
{% endhighlight %}

## 字面量

`字面量（literal）`是一种直接出现在程序中的数据值。

| 类型    | 示例值           |
|-------|---------------|
| 数值    | 12            |
| 数值    | 1.2           |
| 字符串   | "hello world" |
| 字符串   | 'Hi'          |
| 布尔值   | true          |
| 布尔值   | false         |
| 无对象   | null          |
| 未定义对象 | undefined     |

## typeof 运算符

{% highlight html %}
{% include_relative typeof.html %}
{% endhighlight %}

<iframe src="typeof.html"></iframe>

## 数据类型转换

{% highlight html %}
{% include_relative data_type_conversion.html %}
{% endhighlight %}

<iframe src="data_type_conversion.html"></iframe>

# 运算符

## 运算符小结

- 赋值运算符
    - `=`
    - `+=`
    - `-=`
    - `*=`
    - `/=`
    - `%=`
- 一元运算符
    - `++`: 自增
    - `--`: 自减
- 比较运算符
    - `==` : 左右两边值是否相等
    - `===`: 左右两边值和类型是否相等
    - `!=` : 左右两边值是否不等
    - `!==`: 左右两边值和类型是否不等
    - `>`  : 左边值是否大于右边值
    - `>=` : 左边值是否大于等于右边值
    - `<`  : 左边值是否小于右边值
    - `<=` : 左边值是否小于等于右边值
- 逻辑运算符
    - `&&` : 逻辑与
    - `||` : 逻辑或
    - `!`  : 逻辑非

## 运算符优先级

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202308041607740-js%E8%BF%90%E7%AE%97%E7%AC%A6%E4%BC%98%E5%85%88%E7%BA%A7.png)

# 语句

## 表达式和语句

`表达式`：可以被求值的代码，JavaScript引擎会将其计算出一个结果。

`语句`：语句是一段可以执行的代码

## 分支语句

{% highlight html %}
{% include_relative branching_statements.html %}
{% endhighlight %}

<iframe src="branching_statements.html"></iframe>

## 循环语句

{% highlight html %}
{% include_relative loop_statements.html %}
{% endhighlight %}

<iframe src="loop_statements.html"></iframe>

# 函数

## 函数定义和调用

{: .note-title}
> 函数
>
> 可以重复使用的代码块

{% highlight html %}
{% include_relative function.html %}
{% endhighlight %}

<iframe src="function.html"></iframe>

## 逻辑中断做参数检查

{% highlight html %}
{% include_relative logical_interrupts.html %}
{% endhighlight %}

<iframe src="logical_interrupts.html"></iframe>

## 默认参数

{% highlight html %}
{% include_relative function_default_arguments.html %}
{% endhighlight %}

<iframe src="function_default_arguments.html"></iframe>

## 匿名函数

- 匿名函数使用方式分类
    - 函数表达式
    - 立即执行函数

{: .note-title}
> 函数表达式
>
> 将匿名函数赋值给一个变量，并且通过变量名称进行调用
>
> 函数表达式需要先定义后使用

{% highlight html %}
{% include_relative function_expressions.html %}
{% endhighlight %}

<iframe src="function_expressions.html"></iframe>

{: .note-title}
> 立即执行函数(IIFE)
>
> Immediately Invoked Function Expression
>
>

{% highlight html %}
{% include_relative iife.html %}
{% endhighlight %}

<iframe src="iife.html"></iframe>

## 箭头函数

箭头函数是ES6新增的语法，用于简化函数的定义。

适用于那些需要匿名函数的地方

{: .warning}
> 箭头函数中没有`arguments对象`，但是可以使用`剩余参数`代替

## ⭐匿名函数和箭头函数中的this

- 普通函数中，this指向调用者
- 箭头函数中，this指向定义时所在的对象

{% highlight html %}
{% include_relative arrow_function.html %}
{% endhighlight %}

<iframe src="arrow_function.html"></iframe>

## arguments对象

{: .note-title}
> arguments对象
>
> 是一个对应于传递给函数的参数的类数组对象。

{% highlight html %}
{% include_relative arguments.html %}
{% endhighlight %}

<iframe src="arguments.html"></iframe>

## 剩余参数

{% highlight html %}
{% include_relative remaining_parameters.html %}
{% endhighlight %}

<iframe src="remaining_parameters.html"></iframe>

# 对象

## 对象概述

{: .note-title}
> 对象
>
> 一种用于存储数据的引用数据类型，内存可以存放属性和方法

对象与数组的比较：

- 对象使用键值对存储，语义更明了
- 对象是无序的，数组是有序的

{% highlight html %}
{% include_relative object.html %}
{% endhighlight %}

<iframe src="object.html"></iframe>

## 使用Object创建对象

{% highlight html %}
{% include_relative object_create.html %}
{% endhighlight %}

<iframe src="object_create.html"></iframe>

## 构造函数创建对象

使用构造函数创建对象的好处是可以通过一个函数创建多个对象，达到复用的目的。

new实例化的过程：
1. 创建一个新对象
2. 构造函数中的this指向新创建的对象
3. 执行构造函数中的代码

{: .note-title}
> 构造函数与普通函数的区别：
>
> 1. 构造函数的函数名首字母大写
> 2. 构造函数无法直接调用，需要使用new关键字调用
> 3. 构造函数中的this指向新创建的对象
> 4. 构造函数中不需要return

{% highlight html %}
{% include_relative object_constructor.html %}
{% endhighlight %}

<iframe src="object_constructor.html"></iframe>

## 遍历对象

{% highlight html %}
{% include_relative object_Iterating.html %}
{% endhighlight %}

<iframe src="object_Iterating.html"></iframe>

## 属性和方法简写

{% highlight html %}
{% include_relative property_method_shorthand.html %}
{% endhighlight %}

<iframe src="property_method_shorthand.html"></iframe>

# 解构赋值

{% highlight html %}
{% include_relative deconstruct_an_assignment.html %}
{% endhighlight %}

<iframe src="deconstruct_an_assignment.html"></iframe>

# 内置对象

## 内置对象概述

{: .note-title}
> 内置对象
>
> JavaScript内部提供的对象，包含各种属性和方法给开发者调用

[内置对象文档](https://developer.mozilla.org/zh-CN/docs/Web/JavaScript/Reference/Global_Objects)

## Date对象

{% highlight html %}
{% include_relative date.html %}
{% endhighlight %}

<iframe src="date.html"></iframe>

## Object对象

{% highlight html %}
{% include_relative object_object.html %}
{% endhighlight %}

<iframe src="object_object.html"></iframe>

## String对象

{% highlight html %}
{% include_relative string.html %}
{% endhighlight %}

<iframe src="string.html"></iframe>

# 作用域

## 概述

{: .note-title}
> 作用域
>
> 变量或值在代码中可用性范围

- 全局作用域
    - script标签内部
    - 一个独立的js文件
- 局部作用域
    - 函数作用域
    - 块级作用域，`{}`大括号内部

{: .warning}
> 在函数中定义未声明的变量，会自动提升为全局变量。但一般不建议这么做

## 作用域链

嵌套关系的作用域，形成的链式结构

作用：作用域链本质上是底层的变量查找机制（就近原则）

- 在函数被执行时，会优先查找当前函数作用域中的变量
- 如果当前作用域中没有找到，会向上一级作用域查找，直到找到全局作用域

# 垃圾回收机制

- 全局变量一般不会回收（关闭页面回收）
- 一般情况下局部变量的值，不用了，会被自动回收调

{: .note-title}
> 内存泄露
>
> 程序中分配的内存由于某种原因，未释放或无法释放

# 闭包

- 闭包可以实现数据的私有化
- 闭包会导致内存泄露

{% highlight html %}
{% include_relative closure.html %}
{% endhighlight %}

<iframe src="closure.html"></iframe>


# 原型对象

## 概述

每一个构造函数都有一个`prototype属性`，指向原型对象

解决`构造函数`封装时，函数会多次创建，占用内存的问题。

原型对象可以挂载函数，对象实例不会多次创建原型对象里的函数，而是通过原型链查找。节约了内存

不使用原型存在的问题：

{% highlight html %}
{% include_relative without_prototype.html %}
{% endhighlight %}

<iframe src="without_prototype.html"></iframe>

使用原型存在的问题：

{% highlight html %}
{% include_relative with_prototype.html %}
{% endhighlight %}

<iframe src="with_prototype.html"></iframe>

## 原型对象方法原理

- 每个构造函数都有一个`prototype属性`，指向原型对象
- 当使用构造函数创建实例对象时，对象会自动拥有原型对象中的属性和方法
- 实例对象会先在自身中查找，如果没有，会去原型对象中查找
- 原型对象中的属性和方法被所有实例对象共享

## 原型对象中的this

- 构造函数和原型对象中的this都指向实例对象

{: .warning}
> 箭头函数不能作为构造函数使用，因为箭头函数中的this指向的是其定义时所在的对象，而不是实例对象
> 
> 同样的，箭头函数也不能作为原型对象中的方法使用

## constructor属性

- 每个原型对象都有一个`constructor属性`，指向构造函数

{% highlight html %}
{% include_relative constructor_attribute.html %}
{% endhighlight %}

<iframe src="constructor_attribute.html"></iframe>
