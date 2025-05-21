# 语句

## 概述

语句是Javascript中的句子或命令。

语句在被执行后会导致某些事件发生。

## 表达式语句

`有副作用的表达式`是一种最简单的语句。

``` javascript
// 赋值语句
i = 0

// 递增递减语句
i++

// 函数调用语句
console.log("函数调用")
// 没有副作用的函数调用语句需要配合赋值

// delete语句
delete o.x
```

## 复合语句和空语句

复合语句允许我们在期待一个语句的地方使用多个语句

``` javascript
// 复合语句
{
  x = Math.PI
  cx = Math.cos(x)
  console.log("cos(π) = " + cx)
}

// 空语句
;
```

## 条件语句

``` javascript
//if语句
if (username === "admin") {
  console.log("管理员")
} else if (username === "user") {
  console.log("普通用户")
} else {
  console.log("未知用户")
}

//switch语句
switch (username) {
  case "admin":
    console.log("管理员")
    break
  case "user":
    console.log("普通用户")
    break
  default:
    console.log("未知用户")
}
```

## 循环语句

``` javascript
//while语句
let i = 0
while (i < 10) {
  console.log(i)
  i++
}
```

``` javascript
//do-while语句
let i = 0
do {
  console.log(i)
  i++
} while (i < 10)
```

``` javascript
//for语句
for (let i = 0; i < 10; i++) {
  console.log(i)
}
```

``` javascript
//for-of语句
let arr = [1, 2, 3]
for (let i of arr) {
  console.log(i)
}
```

``` javascript
/*
* for-in语句
* foc/in语句循环指定对象的属性名
* */
let obj = { a: 1, b: 2, c: 3 }
for (let i in obj) {
  console.log(i) // a b c
  console.log(obj[i]) // 1 2 3
}
```

## 跳出语句

``` javascript title="break语句"
// break语句
for (let i = 0; i < 10; i++) {
  if (i === 5) {
    break
  }
  console.log(i)
}

//嵌套循环中使用标签
outer: for(let i = 0; i < 10; i++) {
  for(let j = 0; j < 10; j++) {
    if (i === 5 && j === 5) {
      break outer
    }
    console.log(i, j)
  }
}
```

``` javascript title="continue语句"
// continue语句
for (let i = 0; i < 10; i++) {
  if (i === 5) {
    continue
  }
  console.log(i)
}
```

``` javascript title="return语句"
// return语句
function f() {
  return 1
}

// return后面如何不带表达式，则返回undefined
function f2() {
  return
}
```

``` javascript title="yield语句"

/*
* 类似于return。
* 
* 与迭代器和生成器有关
* 
* 
* */

// yield语句
function* f() {
  yield 1
  yield 2
  yield 3
}

let g = f()
console.log(g.next()) // { value: 1, done: false }
console.log(g.next()) // { value: 2, done: false }
console.log(g.next()) // { value: 3, done: false }
```

``` javascript title="throw语句"
// throw
function f() {
  throw new Error("出错了")
}

// try-catch-finally语句
try {
  f()
} catch (e) {
  console.log(e)
} finally {
  console.log("finally")
}
```

## with语句

with的作用是方便访问深层嵌套的对象

!!! warning
        
    with在严格模式下被禁用

``` javascript
// 不使用with的情况
let obj = { x: { y1: 1, y2: 2, y3: 3 } }
console.log(obj.x.y1)
console.log(obj.x.y2)
console.log(obj.x.y3)


// 使用with的情况
with (obj.x) {
  console.log(y1)
  console.log(y2)
  console.log(y3)
}
```

## debugger语句

`debugger`语句用于在代码中设置断点，调试代码时会在这里停下来。

``` javascript
function f() {
  debugger
  console.log("调试")
}
```

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202403071659494.png)

## "use strict"语句

`"use strict"`是一种指令，用于告诉浏览器在严格模式下执行代码。
