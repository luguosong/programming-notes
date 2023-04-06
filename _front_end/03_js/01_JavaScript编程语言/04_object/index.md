---
layout: default
title: 对象
nav_order: 40
parent: JavaScript编程语言
grand_parent: javascript
---


# 对象创建和使用

```javascript
//创建对象方式一：使用字面量创建对象
let obj1 = {};
let obj2 = {
    name: '张三',
    age: 18,
    sex: '男',
    sayHello: function () {
        console.log("hello1");
    }
}

//创建对象方式二：利用Object创建对象
let obj3 = new Object();
obj3.name = "李四";
obj3.age = 20;
obj3.sex = "男";
obj3.sayHello = function () {
    console.log("hello2");
}


/**
 * 构造函数，类似于Java中的类
 * @param name
 * @param age
 * @param sex
 * @constructor
 */
function People(name, age, sex) {
    this.name = name;
    this.age = age;
    this.sex = sex;
    this.sayHello=function () {
        console.log("hello3")
    }
}
//创建对象方式三：通过构造函数创建对象
let obj4 = new People("王五",25,"男");


//对象使用
console.log(obj2.name);
obj2.sayHello()

console.log(obj3.name);
obj3.sayHello();

console.log(obj4.name);
obj4.sayHello();
```

# 内置对象

## 文档查询

[MDN](https://developer.mozilla.org/zh-CN/)

## Math对象

- `Math.PI`表示一个圆的周长与直径的比例，约为3.14159
- `Math.max()`函数返回作为输入参数的最大数字
- `Math.abs()`函数返回一个数字的绝对值。
- `Math.floor()`函数总是返回小于等于一个给定数字的最大整数。
- `Math.ceil()`函数总是四舍五入并返回大于等于给定数字的最小整数。
- `Math.round()`函数返回一个数字四舍五入后最接近的整数。
- `Math.random()`函数返回一个浮点数，伪随机数在范围从0 到小于1

```javascript
// 获取指定范围内的随机整数
function getRandom(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}
```

## Date对象

`Date`对象是用于处理日期和时间的对象。它允许您创建日期对象，设置日期和时间，以及执行各种日期操作。

## String对象

