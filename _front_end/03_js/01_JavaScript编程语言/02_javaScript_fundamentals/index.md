---
layout: default
title: 基础知识
nav_order: 20
parent: JavaScript编程语言
grand_parent: javascript
latex: true
---

# use strict

`ES5`
规范增加了新的语言特性并且修改了一些已经存在的特性。为了保证旧的功能能够使用，大部分的修改是默认不生效的。你需要一个特殊的指令`use strict`
来明确地激活这些特性。

现代JavaScript支持`class`和`module`—— 高级语言结构，它们会自动启用`use strict`
。因此，如果我们使用它们，则无需添加`use strict`指令。

```javascript
"use strict";

// your code here
```

```javascript
function myFunction() {
    "use strict";
    // your code here
}
```

# 变量

{: .note-title}
> 命名规范
>
> - 变量名称必须仅包含字母、数字、符号 $ 和 _。
> - 首字符必须非数字。

{: .warning-title}
> let和var的区别
>
> - 作用域：var是`函数作用域`，let是`块级作用域`
> - 变量提升：var声明的变量在声明前即可使用，let需要先声明再使用
> - 变量声明：var可以重复声明变量，let不可以

# 数据类型

javascript是`动态类型（dynamically typed）`的编程语言。虽然编程语言中有不同的数据类型，但是你定义的变量并不会在定义后，被限制为某一数据类型。

## Number类型

`Number类型`可以分为整数和浮点数

`Infinity`表示无穷大

`NaN`代表一个计算错误

## BigInt类型

在 JavaScript 中，`number`类型无法安全地表示大于 ($2^{53}$-1)（即 9007199254740991），或小于 -($2^{53}$-1) 的整数。

通过将`n`附加到整数字段的末尾来创建`BigInt值`

## String类型

包含字符串的三种方式：

- 双引号：\""Hello\"
- 单引号：\'Hello\'
- 反引号：\`Hello\`

`反引号`是功能扩展引号。它们允许我们通过将变量和表达式包装在`${…}`中，来将它们嵌入到字符串中。

## Boolean类型（逻辑类型）

boolean类型仅包含两个值：`true` 和 `false`。

## null值

JavaScript中的null不是一个`对不存在的object的引用`或者`null指针`。

JavaScript 中的null仅仅是一个代表`无`、`空`或`值未知`的特殊值。

## undefined值

如果一个变量已被声明，但`未被赋值`，那么它的值就是`undefined`

## Object类型

object用于储存`数据集合`和`更复杂的实体`。

## Symbol类型

symbol类型用于创建对象的`唯一标识符`。

# 类型转换

## 字符串转换

```javascript
// 字符串转换
let value = true;
console.log(typeof value);
value=String(value);
console.log(typeof value);
```

```shell
boolean
string
```

## 数字类型转换

```javascript
alert( "6" / "2" ); // 3, string 类型的值被自动转换成 number 类型后进行计算
```

```javascript
// 数字类型转换
let str = "123";
console.log(typeof str); // string
let num = Number(str); // 变成 number 类型 123
console.log(typeof num); // number
console.log(parseInt("123.1"));
console.log(parseFloat("123.1"));
```

## 布尔类型转换

直观上为`空`的值（如 0、空字符串、null、undefined和NaN）将变为`false`。
其他值变成`true`。

# typeof运算符

typeof运算符返回`参数的类型`。

# 操作符

```javascript
console.log("=== 数学运算符 ===")
console.log("加法：3 + 4 = " + (3 + 4)) //加法：3 + 4 = 7
console.log("减法：3 - 4 = " + (3 - 4)) //减法：3 - 4 = -1
console.log("乘法：3 * 4 = " + (3 * 4)) //乘法：3 * 4 = 12
console.log("除法：3 / 4 = " + (3 / 4)) //除法：3 / 4 = 0.75
console.log("取余：3 % 4 = " + (3 % 4)) //取余：3 % 4 = 3
console.log("求幂：3 ** 4 = " + (3 ** 4)) //求幂：3 ** 4 = 81

console.log("=== 自增/自减 ===");
let i = 1;
//前置递增运算符：i = 1,++i = 2,运算后的i：2
console.log("前置递增运算符：i = 1,++i = " + ++i + ",运算后的i：" + i);
i = 1;
//后置递增运算符：i = 1,i++ = 1,运算后的i：2
console.log("后置递增运算符：i = 1,i++ = " + i++ + ",运算后的i：" + i);
i = 1;
//前置递减运算符：i = 1,--i = 0,运算后的i：0
console.log("前置递减运算符：i = 1,--i = " + --i + ",运算后的i：" + i);
i = 1;
//后置递减运算符：i = 1,i-- = 1,运算后的i：0
console.log("后置递减运算符：i = 1,i-- = " + i-- + ",运算后的i：" + i);


console.log("=== 比较运算符 ===");
console.log("小于号：1 < 2 = " + (1 < 2)); //小于号：1 < 2 = true
console.log("大于号：1 > 2 = " + (1 > 2)); //大于号：1 > 2 = false
console.log("小于等于号：1 <= 2 = " + (1 <= 2)); //小于等于号：1 <= 2 = true
console.log("大于等于号：1 >= 2 = " + (1 >= 2)); //大于等于号：1 >= 2 = false
console.log("判等号：1 == 2 = " + (1 == 2)); //判等号：1 == 2 = false
console.log("不等号：1 != 2 = " + (1 != 2)); //不等号：1 != 2 = true
console.log("全等：1 === '1' = " + (1 === '1')); //全等：1 === '1' = false

console.log("=== 逻辑运算符 ===");
console.log("逻辑与运算符：true && false = " + (true && false)); //false
console.log("逻辑或运算符：true || false = " + (true || false)); //true
console.log("逻辑非运算符：!true = " + !true) //false

console.log("=== 赋值运算符 ===");
console.log("num = 10");
console.log("num += 10");
console.log("num -= 10");
console.log("num *= 10");
console.log("num /= 10");
```

# 控制流

## 顺序结构

按照代码的书写顺序依次执行，没有跳过或重复的步骤。

## 分支结构

根据条件判断的结果选择执行不同的代码块。

### if-else

```javascript
// if条件语句
let year = 2023;
if (year < 2015) {
    console.log('Too early...');
} else if (year > 2015) {
    console.log('Too late');
} else {
    console.log('Exactly!');
}
```

### 三元表达式

```javascript
year < 2015 ? console.log('Too early...') : console.log('Too late');
```

### switch语句

```javascript
let a = 4;
switch (a) {
    case 3:
        console.log('Too small');
        break;
    case 4:
        console.log('Exactly!');
        break;
    case 5:
        console.log('Too big');
        break;
    default:
        console.log("I don't know such values");
}
```

## 循环结构

反复执行一段代码，直到满足某个条件才停止。

### for循环

```javascript
for (let i = 0; i < 3; i++) { // 结果为 0、1、2
  alert(i);
}
```

### while循环

```javascript
let i = 0;
while (i < 3) { // 依次显示 0、1 和 2
  alert( i );
  i++;
}
```

### do-while循环

```javascript
let i = 0;
do {
  alert( i );
  i++;
} while (i < 3);
```

### continue break

当程序执行到`continue`语句时，它会立即跳过本次循环中剩余的语句，然后执行下一次循环。

`break`语句用于跳出循环或者switch语句。

# 数组

```javascript
//创建数组
let arr1 = new Array();  //方式一
let arr2 = []; //方式二
```

```javascript
// 数组中可以存放不同类型的元素
let arr3 = [123,123.1,'张三',true];
```

# 函数

## 函数基础

```javascript
/**
 * 函数声明方式一
 * @param num1 参数一
 * @param num2 参数二
 */
function sum1(num1, num2) {
    return num1 + num2;
}

/**
 * 函数声明方式二
 * @param num1
 * @param num2
 * @returns {*}
 */
let sum2 = function (num1, num2) {
    return num1 + num2;
}

/**
 * 函数调用
 */
console.log(sum1(2, 3));
console.log(sum2(2, 3));
```

{: .warning}
> 函数如果没有返回值，返回的是`undefined`

## arguments对象

`arguments`表示函数被调用时传递给函数的参数列表。arguments对象可以在函数内部使用，用于访问函数的参数，而不需要明确地声明这些参数。

{: .warning}
> `arguments`不是一个真正的数组对象，因为它没有数组对象的所有属性和方法，如`length`、`forEach()`等。

```javascript
/**
 * arguments对象
 * @returns {number}
 */
function sum2() {
    let sum = 0;
    for (let i = 0; i < arguments.length; i++) {
        sum += arguments[i];
    }
    return sum;
}

console.log("1+2+3=" + sum2(1, 2, 3));
```

## 作用域

- 全局作用域：在整个script标签或一个单独的js文件中可以使用
- 局部作用域：在函数内部可以使用

# 预解析

- 变量声明预解析
- 函数声明预解析

{: .warning}
> 变量声明预解析仅仅是变量声明，并不会对变量进行初始化。
