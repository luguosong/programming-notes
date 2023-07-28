---
layout: note
title: 类型、值和变量
nav_order: 30
parent: JavaScript
create_time: 2023/5/3
---

# 数据类型分类

- 原始类型
  - 数值
  - 字符串
  - 布尔值
  - Symbol(符号)
  - null
  - undefined
- 对象类型
  - 对象
  - 数组
  - 函数
  - 类


# 数值

## 分类

- 整数字面量
- 浮点数字面量

## 运算

- 运算符
  - `+`
  - `-`
  - `*`
  - `/`
  - `%`
  - `**`:ES2016增加的取幂
- Math对象
  - [MDN中文文档](https://developer.mozilla.org/zh-CN/docs/Web/JavaScript/Reference/Global_Objects/Math)
  - [菜鸟教程](https://www.runoob.com/jsref/jsref-obj-math.html)

## Infinity

在 JavaScript 中，`Infinity`是一个全局属性，表示正无穷大的数值。它的初始值是 `Number.POSITIVE_INFINITY`。`Infinity` 大于任何值。

在 JavaScript 中，超出 `1.797693134862315E+308` 的数值即为 `Infinity`，小于 `-1.797693134862316E+308` 的数值为 `-Infinity`。

比较 JS 中的无穷值很容易: `Infinity === Infinity` 为 true。特殊的函数 `Number.isFinite()` 确定提供的参数是否是一个有限的数字。

## NaN

在 JavaScript 中，NaN 是一个全局属性，表示“非数字”（Not-a-Number）。它表示一个不是合法数字的值。全局 NaN 属性与 Number.NaN 属性相同。

有五种不同类型的操作会返回 NaN：
- 数字转换失败（例如，显式的转换，如 parseInt("blabla")、Number(undefined)，或隐式的转换，如 Math.abs(undefined)）。
- 数学运算结果不是实数（例如，Math.sqrt(-1)）。
- 不确定形式（例如，0 * Infinity、1 ** Infinity、Infinity / Infinity、Infinity - Infinity）。
- 操作数为 NaN 的方法或表达式（例如，7 ** NaN、7 * "blabla"）——这意味着 NaN 是具有传染性的。
- 其他需要将无效值表示为数字的情况（例如，无效日期 new Date("blabla").getTime()、"".charCodeAt(1)）。

要判断一个值是否为 NaN，可以使用 `Number.isNaN()` 或 `isNaN()` 函数。由于 NaN 是唯一一个与自身不相等的值，因此也可以通过自我比较来判断：如果 `x !== x `为真，则说明 x 是 NaN。

## 舍入错误

{% highlight js %}
{% include_relative rounding_error.html %}
{% endhighlight %}

## BigInt

BigInt 是 JavaScript 中的一个新的数字类型，可以用任意精度表示整数。使用 BigInt，即使超出 Number 的安全整数范围限制，也可以安全地存储和操作大整数。要创建一个BigInt，将`n`作为后缀添加到任何整数文字字面量。例如，`123` 变成 `123n`

## 日期和时间

时间戳用于表示1970年1月1日起至今的毫秒数

{% highlight js %}
{% include_relative date_and_time.html %}
{% endhighlight %}
