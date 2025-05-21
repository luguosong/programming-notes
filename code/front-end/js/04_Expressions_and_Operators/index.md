# 表达式与操作符

## 表达式概述

`表达式`是一个可被求值并产生一个值的Javascript短语。

比如下面这些都是表达式：

- 常量
- 变量名
- 数组访问表达式
- 函数调用表达式

## 主表达式

`主表达式`是指那些不包含其他表达式的表达式。

比如字面量值、某些语言关键字和变量引用等。

``` javascript
1.23 //数值字面量
"hello" //字符串字面量
/ pattern / //正则表达式字面量

true //布尔值
false //布尔值
null //null值
this //当前对象
```

## 对象和数组初始化表达式

``` javascript
let e = [] //空数组
let a = [1 + 2, 3 + 4] //数组初始化表达式
let p = { x: 2.3, y: -1.2 } //对象初始化表达式
```

## 函数定义表达式

``` javascript
let square = function(x) {
  return x * x;
} //函数定义表达式
```

## 属性访问表达式

``` javascript
let o = { x: 1, y: { z: 3 } }; //定义一个对象
let a = [o, 4, [5, 6]]; //定义一个数组
o.x // => 1: 属性访问表达式
o.y.z // => 3: 属性访问表达式
o["x"] // => 1: 属性访问表达式
a[1] // => 4: 数组元素访问表达式
a[2]["1"] // => 6: 数组元素访问表达式
a[0].x // => 1: 数组元素访问表达式
```

条件式属性访问:

``` javascript
let a = { b: 1 }
a?.b //如果a不是null或undefined，返回a.b，否则返回undefined

let a = [1, 2]
a?.[0] //如果a不是null或undefined，返回a[0]，否则返回undefined
```

## 调用表达式

调用函数或方法的一种语法

``` javascript
f(0) //调用函数f
Math.max(x, y, z) //调用Math.max方法
a.sort() //调用数组a的sort方法

log?.(message) //如果log是null或undefined，返回undefined，否则调用log方法
```

## 对象创建表达式

``` javascript
new Point(2, 3) //创建一个Point对象
```

## 操作符概述

操作符用于算术表达式、比较表达式、逻辑表达式、赋值表达式

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202403051545686.png){ loading=lazy }
  <figcaption>操作符</figcaption>
</figure>

## 算数表达式

``` javascript
/*基本算数操作符*/
3 ** 2 //9,幂运算
3 * 2 //6,乘法
3 / 2 //1.5,除法
3 % 2 //1,取模
3 + 2 //5,加法
3 - 2 //1,减法

/*加号可以拼接字符串*/
"hello " + "world" // "hello world",+操作符也可以拼接字符串

/*一元操作符*/
+ "12" //12,一元加法操作符，将操作数转为数值
- "12" //-12,一元减法操作符,将操作数转为数值并改变其符号
let i = 1, j = ++i //i和j都是2,前置递增
let n = 1, m = n++ //n是2，m是1,后置递增
let x = 2, y = --x //x和y都是1,前置递减
let a = 2, b = a-- //a是1，b是2,后置递减

/*位操作符*/
0b1010 & 0b1100 //0b1000,按位与
0b1010 | 0b1100 //0b1110,按位或
0b1010 ^ 0b1100 //0b0110,按位异或
~0b1010 //0b0101,按位非
0b1010 << 1 //0b10100,左移
0b1010 >> 1 //0b101,有符号右移
0b1010 >>> 1 //0b0101,零填充右移
```

## 关系表达式

``` javascript
/*相等和不相等操作符*/
1 == "1" //true,相等
1 === "1" //false,严格相等
1 === 1 //true,严格相等
1 != "1" //false,不相等
1 !== "1" //true,严格不相等
1 !== 1 //false,严格不相等

1 < 2 //true,小于
1 <= 2 //true,小于等于
1 > 2 //false,大于
1 >= 2 //false,大于等于

let p = { x: 1, y: 2 }
"x" in p //true,属性存在
"z" in p //false,属性不存在
"toString" in p //true,属性存在

let a = [1, 2, 3]
0 in a //true,索引存在
4 in a //false,索引不存在

let d = new Date()
d instanceof Date //true,对象是Date的实例
d instanceof Object //true,对象是Object的实例
d instanceof Number //false,对象不是Number的实例
```

## 逻辑表达式

``` javascript
true && false //false,逻辑与
true || false //true,逻辑或
!true //false,逻辑非
```

## 赋值表达式

``` javascript
let i, j
i = 1 //赋值

i += 1 //i = i + 1
```

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202403051747149.png){ loading=lazy }
  <figcaption>赋值操作符</figcaption>
</figure>

## 求值表达式

``` javascript
eval("3 + 2") //5,对字符串求值
```

## 条件操作符

``` javascript
let max = (x > y) ? x : y //条件操作符
```

## 先定义

``` javascript
// 缺值合并
let m1 = a ?? b //a不为null或undefined返回a，否则返回b

//相对于 ||
let m2 = a || b //a不为falsy返回a(❗0,空字符串都是假值)，否则返回b
```

## typeof操作符

用于表名操作符的`类型`

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202403060937105.png){ loading=lazy }
  <figcaption>typeof操作符</figcaption>
</figure>

## delete操作符

``` javascript title=""
--8<-- "code/front-end/js/04_Expressions_and_Operators/delete.js"
```

## await操作符

让异步编程更自然

``` javascript
// 模拟一个异步操作，比如从服务器获取数据
function fetchData() {
  return new Promise(resolve => {
    setTimeout(() => {
      resolve('这是从服务器获取的数据');
    }, 2000);
  });
}

// 使用 async/await 等待异步操作完成
async function fetchDataAsync() {
  const data = await fetchData();
  return data;
}

// 调用异步函数并打印结果
fetchDataAsync().then(data => {
  console.log('异步函数返回的数据:', data);
});
```

## void操作符

丢弃操作数的值，返回undefined

``` javascript
let counter = 0;
const increment = () => void counter++; //如果不使用void，返回的是0
console.log(increment()); // undefined
console.log(counter); // 1
```

相当于：

``` javascript
const increment = () => {
  counter++;
  return undefined;
}
```

## 逗号操作符

`左表达式,右表达式`

在期待单个表达式的地方，可以使用多个表达式，用逗号分隔。

只有当左表达式有副作用，才有必要使用逗号操作符。

``` javascript
// 逗号操作符
for (let i = 0, j = 10; i < j; i++, j--) {
  console.log(i + j);
}
```
