---
layout: note
title: 基础
nav_order: 10
parent: 算法
grand_parent: 数据结构与算法
create_time: 2023/5/18
latex: true
---

# 斐波那契数

写一个函数，输入 n ，求斐波那契（Fibonacci）数列的第 n 项（即 F(N)）。斐波那契数列的定义如下：

```text
F(0) = 0,   F(1) = 1
F(N) = F(N - 1) + F(N - 2), 其中 N > 1.
```

斐波那契数列由 0 和 1 开始，之后的斐波那契数就是由之前的两数相加而得出。

{% highlight java %}
{% include_relative Fibonacci.java %}
{% endhighlight %}

# 复杂度大O表示法

- 忽略常数：$2n+2$=O(n)
- 有高阶直接忽略低阶: $n^2+2n+1$=O($n^2$)

