---
layout: note
title: 词法结构
nav_order: 20
parent: JavaScript
create_time: 2023/4/25
---

# 程序的文本

- 区分大小写
- 忽略空格
- 将换行符，回车符和回车/换行序列识别为行终止符。

# 注释

{% highlight html %}
{% include_relative comments.html %}
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
