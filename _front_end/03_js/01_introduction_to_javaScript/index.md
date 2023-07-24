---
layout: note
title: JavaScript 简介
nav_order: 10
parent: JavaScript
create_time: 2023/4/25
---

# 特点

- 是一门动态、解释型语言
- 从Scheme借鉴了一类（first class）函数
- 从Self借鉴了基于原型（prototype）的继承

# JavaScript语言和宿主环境

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

# Hello World

[示例](hello_world.html)

{% highlight html %}
{% include_relative hello_world.html %}
{% endhighlight %}
