---
layout: note
title: 语言基础
nav_order: 20
parent: JavaSE
create_time: 2023/8/7
---

# 关键字

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202308072148041-Java%E5%85%B3%E9%94%AE%E5%AD%97.png)

`保留字`：暂未使用的关键字，const、goto

true、false、null是`字面量`，不是`关键字`



# 标识符

{: .note}
> 标识符
>
> Java中变量、方法、类等要素命名时使用的字符序列

标识符命名规则：

1. 由字母大小写、0-9、_或$组成
2. 不能以数字开头
3. 不能是Java中的关键字
4. 区分大小写
5. 不能包含空格

# 变量

## 定义

{: .note-title}
> 变量
>
> 内存中的一个存储区域，可以在程序运行过程中被修改

## 按数据类型分类

- 基本数据类型
    - 整型
        - byte
        - short
        - int
        - long
    - 浮点型
      - float
      - double
    - 字符型
      - char
    - 布尔型
      - boolean
- 引用数据类型
    - 类
    - 数组
    - 接口
    - 枚举
    - annotation
    - record

## 按声明位置分类

- 实例变量
- 类变量
- 局部变量
- 参数

## 自动提升和强制转换

自动提升：当容量小的变量与容量大的变量做运算时，结果自动提升为容量大的变量类型

强制转换：当容量大的数据类型赋值给容量小的数据类型时，需要强制转换

# 运算符

- 算数运算符
- 赋值运算符
- 比较运算符
- 逻辑运算符
- 位运算符
- 条件运算符

# 流程控制

## 分支结构

- if...else
- switch...case
- 三元运算符

## 循环结构

- while
- do...while
- for
- 增强for

## 跳转控制

- break
- continue
- return

# 数组

## 定义

{: .note-title}
> 数组
> 
> 多个相同类型数据按一定顺序排列的集合，并使用一个名字命名，并通过编号的方式对这些数据进行统一管理

{% highlight java %}
{% include_relative ArrayExample.java %}
{% endhighlight %}


# 面试题

<details markdown="block">
<summary>高效的方式计算2*8的值</summary>
使用左移运算符 `<<`

`8 << 1`
</details>
<hr>

<details markdown="block">
<summary>&和&&的区别？</summary>
&&是短路与，一旦左边不满足条件后，右边将不再执行
</details>
<hr>

<details markdown="block">
<summary>Java中有哪些基本数据类型，String是不是？</summary>
8种基本数据类型，byte,short,int,long,float,double,boolean,char

String不是
</details>
<hr>

<details markdown="block">
<summary>Java开发中计算金额时使用什么数据类型？</summary>
不能使用float或double，因为精度不高。

使用`BigDecimal类`替换
</details>
<hr>

<details markdown="block">
<summary>char变量能不能存储一个中文汉字，为什么？</summary>
可以

因为Java采用的时Unicode编码
</details>
<hr>

<details markdown="block">
<summary>boolean占几个字节</summary>
编译时不谈占几个字节

在JVM给boolean分配内存空间时，boolean占一个槽位（slot,等于4个字节）
</details>
<hr>

<details markdown="block">
<summary>break和continue的作用</summary>
break：跳出当前循环

continue：结束本次循环，继续下次循环
</details>
<hr>
