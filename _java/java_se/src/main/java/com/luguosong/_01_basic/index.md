---
layout: note
title: 基础概念
nav_order: 10
parent: JavaSE
latex: true
create_time: 2023/9/15
---

# 变量（Variables）

## 变量分类

- `实例变量（非静态字段）`:从技术上讲，对象将它们的个体状态存储在`非静态字段`中，也就是在没有使用`static`
  关键字声明的字段。非静态字段也被称为`实例变量`，因为它们的值对于类的每个实例（换句话说，对于每个对象）都是唯一的。
- `类变量（静态字段）`:类变量是使用 static
  修饰符声明的任何字段；这告诉编译器，无论类已实例化多少次，该变量都只有一个副本存在。此外，关键字`final`
  可以添加，以指示齿轮数永远不会改变(`常量`)。
- `局部变量`:类似于对象在字段中存储其状态的方式，方法通常会将其临时状态存储在`局部变量`
  中。变量是否为局部变量完全取决于变量`声明的位置`——即在方法的大括号之间。因此，局部变量仅对其声明的方法可见；它们无法从类的其他部分访问。
- `参数`:参数始终被分类为`变量`而不是`字段`。

## 变量命名规则

- 变量名`区分大小写`。变量的名称可以是`任何合法标识符`，惯例是始终用字母开头作为变量名,不允许使用空白字符开头。
- 后续字符可以是`字母`、`数字`、`美元符号`或`下划线`
  字符。在选择变量名时，使用完整的单词，而不是晦涩的缩写。`变量名不能是关键字或保留字`。
- 如果你选择的名称只包含`一个单词`，将该单词`全部小写`。如果它由`多个单词组成`，大写每个后续单词的第一个字母。
- 如果你的变量存储一个`常量值`，例如 `static final int NUM_GEARS = 6`，那么约定会稍微改变，`大写每个字母`，并用`下划线`
  字符分隔后续单词。

## 原始数据类型

Java编程语言是`静态类型`的，这意味着在使用变量之前，必须首先`声明`它们。

| 类型      | 说明                   | 长度   | 最小值                            | 最大值                        | 字段默认值  |
|---------|----------------------|------|--------------------------------|----------------------------|--------|
| byte    | 有符号的二进制补码整数          | 8位   | -128                           | 127                        | 0      |
| short   | 有符号的二进制补码整数          | 16位  | -32,768                        | 32,767                     | 0      |
| int     | 有符号的二进制补码整数          | 32位  | $-2^{31}$                      | $2^{31}-1$                 | 0      |
| long    | 有符号的二进制补码整数          | 64位  | $-2^{63}$                      | $2^{63}-1$                 | 	0L    |
| float   | 单精度 IEEE 754浮点数      | 32位  | 正数部分最小值：$1.4 \times 10^{-45}$  | $3.4028235 \times 10^{38}$ | 	0.0f  |
| double  | 双精度 IEEE 754 浮点      | 64位  | 正数部分最小值：$4.9 \times 10^{-324}$ | $1.8 \times 10^{308}$      | 	0.0d  |
| boolean | 代表真或假                |      | -                              | -                          | false  |
| char    | 单个字符，16 位 Unicode 编码 | 16 位 | `\u0000`(0)                    | `\uffff`(65,535)           | \u0000 |

{: .warning}
> 编译器不会为未初始化的局部变量分配默认值。如果您无法在声明时初始化局部变量，请确保在尝试使用它之前为其分配一个值。
>
> 访问未初始化的局部变量将导致编译错误。

# 操作符

一下为操作符按照优先级进行排序：

| 优先级     | 操作符                                   | 说明    |
|---------|---------------------------------------|-------|
| 后缀操作符   | `expr++ expr--`                       | 自增，自减 |
| 一元操作符   | `++expr --expr +expr -expr ~ !`       |       |
| 乘性操作符   | `* / %`                               |       |
| 加性操作符   | `+ -`                                 |       |
| 移位操作符   | `<< >> >>>`                           |       |
| 关系操作符   | `< > <= >= instanceof`                |       |
| 相等操作符   | `== !=`                               |       |
| 按位与操作符  | `&`                                   |       |
| 按位异或操作符 | `^`                                   |       |
| 按位或操作符  | &#124;                                |       |
| 逻辑与操作符  | `&&`                                  |       |
| 逻辑或操作符  | &#124;&#124;                          |       |
| 三元运算符   | `?:`                                  |       |
| 赋值操作符   | `= += -= *= /= %= &= ^= <<= >>= >>>=` |       |

# 表达式、语句和块

`运算符`可以用于构建`表达式`，`表达式`可以计算数值；`表达式`是`语句`的核心组成部分；`语句`可以组合成`块`。

`表达式`是由`变量`、`运算符`和`方法调用`构成的构造，根据语言的语法构造，最终会求值为一个单一的值。

`语句`大致相当于自然语言中的句子。语句构成一个完整的执行单元。

`语句`分类：

- 表达式语句
- 声明语句
- 控制流语句

一个`块`是由成对的花括号之间的零个或多个语句组成的，可以在任何允许单个语句的地方使用。

{: .warning-title}
> 区分表达式和语句
>
> 表达式（Expressions）和语句（Statements）是两个不同的概念
>
> - 表达式计算出一个值，而语句执行一个操作。
> - 表达式可以嵌套在语句中，例如，你可以在条件语句中使用表达式来决定执行哪个分支。
> - 总之表达式用于计算值，而语句用于执行操作。

# 流程控制语句

## if-then语句

`if-then`语句是所有控制流语句中最基本的。它告诉您的程序仅在特定测试评估为 true 时执行某个代码部分。

```java
class Demo {
    void applyBrakes() {
        // "if" 语句块：自行车必须在运动中
        if (isMoving) {
            // "then" 语句块：减小当前速度
            currentSpeed--;
        }
    }
}
```

## if-then-else语句

`if-then-else`语句在`if`子句评估为`false`时提供了一个备用的执行路径。

以下的程序 IfElseDemo 根据测试分数的值分配一个等级：如果分数达到 90% 或以上则为 A，达到 80% 或以上则为 B，以此类推。

```java
class IfElseDemo {
    public static void main(String[] args) {

        int testscore = 76;
        char grade;

        if (testscore >= 90) {
            grade = 'A';
        } else if (testscore >= 80) {
            grade = 'B';
        } else if (testscore >= 70) {
            grade = 'C';
        } else if (testscore >= 60) {
            grade = 'D';
        } else {
            grade = 'F';
        }
        System.out.println("Grade = " + grade);
    }
}
```

## switch语句

与 `if-then` 和 `if-then-else` 语句不同，`switch`语句可以有多个可能的执行路径。`switch`语句适用于`字节`、`短整型`、`字符`
和`整数`这些原始数据类型。它还适用于`枚举类型`、`String类`
以及一些特殊的包装了某些原始数据类型的类：`Character`、`Byte`、`Short` 和 `Integer`。

```java
public class SwitchDemo {
    public static void main(String[] args) {

        int month = 8;
        String monthString;
        switch (month) {
            case 1:
                monthString = "January";
                break;
            case 2:
                monthString = "February";
                break;
            case 3:
                monthString = "March";
                break;
            case 4:
                monthString = "April";
                break;
            case 5:
                monthString = "May";
                break;
            case 6:
                monthString = "June";
                break;
            case 7:
                monthString = "July";
                break;
            case 8:
                monthString = "August";
                break;
            case 9:
                monthString = "September";
                break;
            case 10:
                monthString = "October";
                break;
            case 11:
                monthString = "November";
                break;
            case 12:
                monthString = "December";
                break;
            default:
                monthString = "Invalid month";
                break;
        }
        System.out.println(monthString);
    }
}
```

{: .note-title}
> `if-then-else`还是`switch`？
>
> 决定是使用`if-then-else`语句还是`switch`语句基于`可读性`和`语句正在测试的表达式`。
>
> `if-then-else`语句可以根据值`范围`或`条件`测试表达式，
>
> 而`switch`语句仅基于`单个整数、枚举值或 String 对象`测试表达式。


{: .warning}
> `break语句`是必需的，因为如果没有它们，switch块中的语句会穿透：匹配的case标签后面的所有语句会按顺序执行，而不考虑后续case标签的表达式，直到遇到break语句。

## while语句

当特定条件为`true`时，`while语句`持续执行一个语句块。

```java
class WhileDemo {
    public static void main(String[] args) {
        int count = 1;
        while (count < 11) {
            System.out.println("Count is: " + count);
            count++;
        }
    }
}
```

do-while 和 while 的区别在于 do-while 在循环底部评估其表达式，而不是在顶部。因此，do 块内的语句总是至少执行一次。

```java
class DoWhileDemo {
    public static void main(String[] args) {
        int count = 11;
        do {
            System.out.println("Count is: " + count);
            count++;
        } while (count < 11);
    }
}
```

## for语句

`for语句`提供了一种紧凑的方式来迭代一系列的值。程序员通常将其称为`for 循环`，因为它会根据特定条件反复循环。

```
for(初始化表达式;终止表达式;递增表达式){
    语句(s)
}
```

- `初始化表达式`用于初始化循环；它在循环开始时执行一次。
- 当`终止表达式`评估为 false 时，循环终止。
- `递增表达式`在每次循环迭代之后被调用；这个表达式完全可以用于增加或减少一个值。

```java
class ForDemo {
    public static void main(String[] args) {
        for (int i = 1; i < 11; i++) {
            System.out.println("Count is: " + i);
        }
    }
}
```

for语句还有另一种形式，专门用于遍历集合和数组。这种形式有时被称为`增强型for语句`，可以使您的循环更加紧凑和易于阅读。

```java
class EnhancedForDemo {
    public static void main(String[] args) {
        int[] numbers =
                {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        for (int item : numbers) {
            System.out.println("Count is: " + item);
        }
    }
}
```

## break语句

`break语句`有两种形式：带标签和不带标签。

`不带标签的break语句`终止`最内层`的switch、for、while 或 do-while语句，`但带标签的break语句`会终止`外部语句`。

```java
class BreakWithLabelDemo {
    public static void main(String[] args) {

        int[][] arrayOfInts = {
                {32, 87, 3, 589},
                {12, 1076, 2000, 8},
                {622, 127, 77, 955}
        };
        int searchfor = 12;

        int i;
        int j = 0;
        boolean foundIt = false;

        // 这是一个带标签的语句块，标记为 "search"
        search:
        for (i = 0; i < arrayOfInts.length; i++) {
            for (j = 0; j < arrayOfInts[i].length;
                 j++) {
                if (arrayOfInts[i][j] == searchfor) {
                    foundIt = true;
                    // 带标签的 break 语句用于终止 "search" 标记的外部循环
                    break search;
                }
            }
        }

        if (foundIt) {
            System.out.println("Found " + searchfor + " at " + i + ", " + j);
        } else {
            System.out.println(searchfor + " not in the array");
        }
    }
}
```

## continue语句

`continue`语句用于跳过for、while 或 do-while循环的当前迭代。

`不带标签`的形式会跳到最内层循环体的末尾，并评估控制循环的布尔表达式。

`带标签`的continue语句用于跳过带有给定标签的外部循环的当前迭代。

## return语句

`return语句`退出当前方法，并将控制流返回到调用方法的位置。

return 语句有两种形式：一种返回一个值，另一种不返回值。要返回一个值，只需在 return 关键字之后放置该值（或计算该值的表达式）。
