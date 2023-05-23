---
layout: note
title: 基础知识
nav_order: 10
parent: JavaSE
latex: true
create_time: 2023/5/22
---

# 关键字

{: .note-title}
> 关键字
>
> 被Java语言赋予了特殊含义，用于专门用途的字符串（单词）


| abstract | continue | for | new | switch |
| assert | default | goto（未使用） | package | synchronized |
| boolean | do | if | private | this |
| break | double | implements| protected| throw |
| byte | else | import | public | throws |
| case | enum | instanceof| return | transient |
| catch | extends | int | short | try |
| char | final | interface | static | void |
| class | finally | long | strictfp | volatile |
| const（未使用） | float | native | super | while |

`ture`，`false`，`null`不是关键字，而是字面量。

# 标识符

{: .note-title}
> 标识符
>
> `变量`、`方法`、`类`等要素命名时使用的字符序列，称为`标识符`

- 命名规则
    - 区分大小写，长度无限制
    - 不能包含空格
    - 由`字母`、`数字`、`下划线`、`$`符号组成，其中不能以`数字`开头
    - 不可以使用`关键字`作为标识符


- 规范
    - `包名`：全部小写
    - `类名`、`接口名`：首字母大写
    - `变量名`、`方法名`：首字母小写
    - `常量名`：全部大写,单词之间用下划线连接

# 变量

{: .note-title}
> 变量
>
> 用于存储数据的`内存空间`，该区域的数据可以在同一类型范围内不断变化

变量三要素：`数据类型 变量名=数据值`

基本数据类型：

| 数据类型    | 描述                        | 取值范围                                 | 默认值      | 长度   |
|---------|---------------------------|--------------------------------------|----------|------|
| byte    | 8位有符号的二进制补码整数             | $-2^7$ 到 $2^7-1$                     | 0        | 1 字节 |
| short   | 16位有符号的二进制补码整数            | $-2^{15}$ 到 $2^{15}-1$               | 0        | 2 字节 |
| int     | 32位有符号的二进制补码整数            | $-2^{31}$ 到 $2^{31}-1$（约21亿）         | 0        | 4 字节 |
| long    | 64位有符号的二进制补码整数            | $-2^{63}$ 到 $2^{63}-1$               | 0L       | 8 字节 |
| float   | 单精度32位IEEE 754浮点数         | $3.40282347 \times 10^{38}$          | 0.0f     | 4 字节 |
| double  | 双精度64位IEEE 754浮点数         | $1.7976931348623157 \times 10^{308}$ | 0.0      | 8 字节 |
| boolean | 布尔类型，只有两个可能值：true 和 false | true 或 false                         | false    | 不适用  |
| char    | 单个16位Unicode字符            | '\u0000' 到 '\uffff' （0 到 65,535）     | '\u0000' | 2 字节 |

{: .warning}
> 只有当变量作为`字段`时，才会被初始化为默认值，`局部变量`不会被初始化为默认值

## 浮点数计算精度问题

{% highlight java %}
{% include_relative AccuracyDemo.java %}
{% endhighlight %}

## 类型自动提升和强制转换

当容量小的数据类型的变量与容量大的数据类型的变量做运算时，结果`自动提升`为容量大的数据类型

当容量大的数据类型的变量与容量小的数据类型的变量做运算时，结果`强制转换`为容量小的数据类型

容量：byte < short < int < long < float < double

{: .warning-title}
> 特殊情况
>
> 当`byte`、`short`、`char`三种类型的变量做运算时，结果为`int`型

## 进制

{% highlight java %}
{% include_relative Hexadecimal.java %}
{% endhighlight %}

# 运算符

## 算数运算符

| 运算符  | 描述 | 示例      |
|------|----|---------|
| `+ ` | 加法 | `a + b` |
| `- ` | 减法 | `a - b` |
| `* ` | 乘法 | `a * b` |
| `/ ` | 除法 | `a / b` |
| `% ` | 取模 | `a % b` |
| `++` | 自增 | `a++  ` |
| `--` | 自减 | `a--`   |

## 赋值运算符

| 运算符  | 描述    | 示例          |
|------|-------|-------------|
| `= ` | 赋值    | `c = a + b` |
| `+=` | 加且赋值  | `c += a`    |
| `-=` | 减且赋值  | `c -= a`    |
| `*=` | 乘且赋值  | `c *= a`    |
| `/=` | 除且赋值  | `c /= a`    |
| `%=` | 取模且赋值 | `c %= a`    |

## 比较运算符

| 运算符  | 描述   | 示例       |
|------|------|----------|
| `==` | 相等   | `a == b` |
| `!=` | 不相等  | `a != b` |
| `> ` | 大于   | `a > b ` |
| `< ` | 小于   | `a < b ` |
| `>=` | 大于等于 | `a >= b` |
| `<=` | 小于等于 | `a <= b` |

## 位运算符

| 运算符   | 描述    | 示例        |
|-------|-------|-----------|
| `&  ` | 按位与   | `a & b  ` |
| \|    | 按位或   | a\|b      |
| `^  ` | 按位异或  | `a ^ b  ` |
| `~  ` | 按位取反  | `~a     ` |
| `<< ` | 左移    | `a << b ` |
| `>> ` | 右移    | `a >> b ` |
| `>>>` | 无符号右移 | `a >>> b` |

## 条件运算符

| 运算符 | 描述    | 示例                        |
|-----|-------|---------------------------|
| ?:  | 条件运算符 | condition ? expr1 : expr2 |

# 流程控制

## if-else

{% highlight java %}
{% include_relative IfElseExample.java %}
{% endhighlight %}

## switch-case

{% highlight java %}
{% include_relative SwitchCaseExample.java %}
{% endhighlight %}

## for

{% highlight java %}
{% include_relative ForLoopExample.java %}
{% endhighlight %}

## while

{% highlight java %}
{% include_relative WhileLoopExample.java %}
{% endhighlight %}

## do-while

{% highlight java %}
{% include_relative DoWhileLoopExample.java %}
{% endhighlight %}

## 模拟goto

{% highlight java %}
{% include_relative GotoExample.java %}
{% endhighlight %}

