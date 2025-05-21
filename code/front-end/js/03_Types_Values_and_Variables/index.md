# 类型、值和变量

## 类型概述

JavaScript类型分类：

- 原始类型（primitive type）
    - 数值（number）
    - 字符串（string）
    - 布尔值（boolean）
    - undefined
    - null
    - 符号（symbol）
- 对象类型（object type）
    - 普通对象（object）
    - 数组（array）
    - Set对象
    - Map对象
    - RegExp对象
    - Date对象
    - Error对象
    - Function对象
    - 类对象


## 数值

### 数值字面量

``` javascript
/*
* 数值字面量
* */
/*整数*/
0
3
1000000
0xff // 16进制
0Xff // 16进制
//ES6之后也可以使用二进制和八进制
0b1111 // 二进制
0o777 // 八进制

/*浮点字面量*/
3.14
.33333333
6.02e23 //6.2*10^23
1.4738223E-32 //1.4738223*10^-32
```

### 无穷值

在数值操作的结果超出了JavaScript的数值范围时，会返回`Infinity`或`-Infinity`。

``` javascript
1 / 0 // Infinity
```

### 非数值

在数值操作的结果不是数值时，会返回`NaN`。

``` javascript
0 / 0 // NaN
Math.sqrt(-1) // 负数的平方根也是NaN

/*非数值与任何值比较都不相等，包括它自己*/
NaN === NaN // false
Number.isNaN(NaN) // true,可以通过Number.isNaN()来判断一个值是否是NaN
```

### 负零

``` javascript
-0 === 0 // true
1 / -0 === 1 / 0 // false,Infinty不等于-Infinity
```

### 二进制浮点数与舍入错误

由于js采用的是二进制表示法，只能精确表示二进制的1/2、1/4、1/8等分数，而不能精确表示1/10这样的分数。因此，js中的浮点数很多都是近似值，运算可能会产生舍入错误。

``` javascript
let x = 0.3 - 0.2;
let y = 0.2 - 0.1;
x === y // false
```

### BigInt

ES2020引入了BigInt类型，可以表示任意大的整数。

BigInt字面量是一个整数字后面加上`n`。

!!! warning 

    算数运算时，不能将`BigInt类型`与`普通数值`混合运算。
    
    `Math对象`的方法不支持BigInt类型。

``` javascript
const bigInt = 1234567890123456789012345678901234567890n;

//可以通过BigInt()函数将其他类型的值转为BigInt
BigInt(123) // 123n
BigInt('123456789123456789') // 123456789123456789n
```

### 时间戳

时间戳表示从1970年1月1日0时0分0秒（UTC）开始的毫秒数。

``` javascript
Date.now(); // 返回当前时间的时间戳
new Date().getTime(); // 返回当前时间的时间戳
```

## 文本

### 字符串字面量

``` javascript
const str1 = "hello" // 单引号
const str2 = "hello" // 双引号
const str3 = `hello` // 反引号
const str4 = `1+2=${1 + 2}` // 1+2=3，可以在反引号字符串中插入表达式
const str5 = "hello\nworld" // 换行
const str6 = "hello\
world" //这表示一行，当字符串内容比较多可以这么干
```

### 转义字符

反斜杠与后面的字符组合在一起，可以表示一个无法直接表示的字符。

| 转义字符 | 含义    |
|------|-------|
| \0   | 空字符   |
| \b   | 退格    |
| \t   | 水平制表符 |
| \n   | 换行    |

### 字符串使用

``` javascript
"hello " + "world" // hello world,使用+号拼接字符串
"hello".length // 5,字符串长度

/*获得字符串的一部分*/
"hello,world".substring(1, 4) // ell,截取子字符串[1,4)
"hello,world".slice(1, 4) // ell,截取子字符串[1,4)
"hello,world".slice(-3) // rld,截取最后3个字符
"hello,world".split(",") // ["hello","world"],按逗号分割字符串

/*字符串搜索*/
"hello,world".indexOf("l") // 4,返回第一个o的位置
"hello,world".indexOf("l", 3) // 3,从第3个位置开始搜索
"hello,world".indexOf("zz") // -1,未找到
"hello,world".lastIndexOf("l") // 9,返回最后一个o的位置

/*ES6及之后版本中的布尔值搜索函数*/
"hello,world".includes("l") // true,字符串中是否包含l
"hello,world".startsWith("hell") // true,字符串是否以hell开头
"hello,world".endsWith("d") // true,字符串是否以d结尾

/*创建字符串的修改版本*/
"hello,world".replace("l", "L") // heLLo,world,将第一个l替换为L
"hello,world".toLowerCase() // hello,world,转为小写
"hello,world".toUpperCase() // HELLO,WORLD,转为大写
"hello,world".normalize() // hello,world,Unicode NFC归一化
"hello,world".normalize("NFD") // hello,world,Unicode NFD归一化，还有NFKC和NFKD

/*访问字符串中的个别字符*/
"hello,world".charAt(0) // h,返回指定位置的字符
"hello,world".charAt("hello,world".length - 1) // d,返回最后一个字符
"hello,world".charCodeAt(0) // 104,返回指指定位置的16位数值
"hello,world".codePointAt(0) // 104,返回指定位置的32位数值，适用于码点大于0xFFFF的字符

/*ES2017增加的字符串填充函数*/
"x".padStart(3) // "  x",在字符串前面填充空格
"x".padEnd(3) // "x  ",在字符串后面填充空格
"x".padStart(3, "0") // "00x",在字符串前面填充0
"x".padEnd(3, "0") // "x00",在字符串后面填充0

//删除空格
"  hello,world  ".trim() // hello,world,删除两端的空格
"  hello,world  ".trimStart() // hello,world  ,删除前面的空格
"  hello,world  ".trimEnd() //   hello,world,删除后面的空格

"<>".repeat(3) // <><><>,重复字符串
```

### 正则表达式

``` javascript
let text = "testing: 1, 2, 3";
let pattern = /\d+/g; // 匹配数字
pattern.test(text) // true,测试字符串是否包含数字
text.search(pattern) // 9,返回第一个匹配的位置
text.match(pattern) // ["1","2","3"],返回所有匹配的字符串
text.replace(pattern, "#") // "testing: #, #, #",替换匹配的字符串
text.split(/\D+/) // ["","1","2","3"],按非数字分割字符串
```

## 布尔值

布尔值只有两个值：`true`和`false`。

JavaScript中的任何值都可以转为布尔值。下面这些值转为布尔值`false`:

- undefined
- null
- 0
- -0
- NaN
- ""（空字符串）

其他值都转为`true`。

## null和undefined

`null`: 表示空值，即该处的值现在为空。表示程序级别、正常或意料之中的没有值。

`undefined`: 表示未定义，即该处的变量没有被赋值。既变量的值未被初始化。表示一种系统级别、意料之外或类似的没有值。

``` javascript
typeof null // "object"

typeof undefined // "undefined"

null == undefined // true,它们都是假性值，都表示false
```

## 符号⛏️

ES6引入了一种新的原始数据类型`Symbol`，表示独一无二的值。

可以将调用`Symbol()`取得的符号值安全地用于对象的属性名，确保不会与其他属性名冲突。

``` javascript
/*
  * 即使每次传入相同的参数，Symbol会返回不同的值
  * */
const symbol1 = Symbol("propname")
const symbol2 = Symbol("propname")
console.log("symbol1 === symbol2 =" + (symbol1 === symbol2)) // false

/*
* 全局符号注册表
* */
const symbol3 = Symbol.for("propname")
const symbol4 = Symbol.for("propname")
console.log("symbol3 === symbol4 =" + (symbol3 === symbol4)) // true

/*
* 通过符号值得到符号名
* */
console.log("Symbol.keyFor(symbol3) =" + Symbol.keyFor(symbol3)) // propname
```

## 全局对象⭐

全局对象的属性是`全局性定义的标识符`，可以在程序的任何地方使用。

在Node中，全局对象是`global`，在浏览器中，全局对象是`window`。

ES2020最终定义了`globalThis`作为任何上下文中引用全局对象的标准方式。

## 修改数据

原始类型是不可以修改的，但是对象类型是可以修改的。

``` javascript
/*
* 原始类型不可以修改
* */
let str = "hello";
// JavaScript不会报错，但也不会产生任何效果。原始字符串 str 的值保持不变，仍然是 "hello"。
str[0] = "H"; // 不会改变str的值
str.toUpperCase(); // 不会改变str的值,而是返回
console.log(str); // hello

/*
* 对象类型可以修改
* */
let arr = [1, 2, 3, 4, 5];
arr[0] = 100;
arr[1] = 200;
// 使用数组的方法修改第三个元素
arr.splice(2, 1, 300);
console.log(arr); // 输出: [100, 200, 300, 4, 5]
```

## 类型转换

``` javascript
/*
* 显式类型转换
* */
Number("3") // 3,将字符串转为数值
String(false) // "false",将布尔值转为字符串
Boolean([]) // true,将对象转为布尔值

/*
* 数值转字符串指定进制
* */
let n = 17;
"0b"+n.toString(2) // "0b10001",将数值转为二进制字符串
"0o"+n.toString(8) // "0o21",将数值转为八进制字符串
"0x"+n.toString(16) // "0x11",将数值转为十六进制字符串

let n2 = 123456.789;
n2.toFixed(0) // "123457",四舍五入为整数
n2.toFixed(2) // "123456.79",四舍五入为小数点后两位
n2.toExponential(1) // "1.2e+5",转为科学计数法，小数点后1位
n2.toPrecision(4) // "1.235e+5",转为科学计数法，总长度为4

/*
* 字符串转数值
* */
parseInt("123") // 123,将字符串转为整数
parseFloat("123.456") // 123.456,将字符串转为浮点数
```

## 变量声明与赋值

!!! note "区别"

    - const声明的是常量，不能修改。let和var声明的是变量，可以修改。
    - const和let声明的全局变量在node和浏览器中都是文件级的作用域。var声明的全局变量则是全局作用域即可以通过globalThis引用。
    - 作用域提升：var声明的变量会被提升到函数体的顶部，而let和const声明的变量不会被提升。
    - 重复声明：var声明的变量可以重复声明，let和const声明的变量不可以重复声明。
    - let和const声明的变量在声明之前使用会报错，而var声明的变量则会返回undefined。

``` javascript
// 常量声明
const H0 = 74
// H0 = 75 //❌常量的值不能改变

// 变量声明
let i = 0
i = 1

/*
* 如果在函数体外部声明，则会成为全局变量，既可以通过globalThis引用
* */
var v = 10

/*
* 直接对一个未声明的变量赋值
* ❗非常容易导致错误
* */
x = 10 // x是全局变量
```

## 解构赋值⭐

ES6实现的一种复合声明与赋值语法。

等号右边是数组或对象。

等号左边是模拟数组或对象字面量语法指定一个或多个变量。

```  javascript title="解构赋值"
--8<-- "code/front-end/js/03_Types_Values_and_Variables/destructuring_assignment.js"
```

