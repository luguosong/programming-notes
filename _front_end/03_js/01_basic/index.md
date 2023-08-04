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

JavaScript最初被创建的目的是为了`使网页更生动`。这种编程语言写出来的程序被称为`脚本`。脚本可以直接写在网页的HTML中，在页面加载时自动执行。脚本以纯文本的形式提供和执行，不需要特殊的准备或编译即可运行。

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

# 常量

{% highlight html %}
{% include_relative const.html %}
{% endhighlight %}

# 数组

{: .note-title}
> 数组
>
> 将一组数据存储在单个变量名下

{% highlight html %}
{% include_relative array.html %}
{% endhighlight %}

<iframe src="array.html"></iframe>

# 数据类型

- 基本数据类型
  - number 数字型
  - string 字符串型
  - boolean 布尔型
  - undefined 未定义型
  - null 空类型
- 引用数据类型
  - object 对象

{% highlight html %}
{% include_relative data_type.html %}
{% endhighlight %}

# 字面量

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


# typeof 运算符

{% highlight html %}
{% include_relative typeof.html %}
{% endhighlight %}

<iframe src="typeof.html"></iframe>

# 数据类型转换

{% highlight html %}
{% include_relative data_type_conversion.html %}
{% endhighlight %}

<iframe src="data_type_conversion.html"></iframe>


