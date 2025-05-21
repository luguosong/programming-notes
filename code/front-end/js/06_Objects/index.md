# 对象

## 概述

`对象`是一个`属性`的`无序集合`。

## 创建对象

### 对象字面量创建对象

``` javascript
let empty = {}; 

let point = {
    x: 10,
    y: 20
};

// 属性名包含空格或连字符
let book = {
    "main title": "JavaScript",
    "sub-title":"The Definitive Guide"
};
```

### new 创建对象

``` javascript
let o = new Object();
let a = new Array();
let d = new Date();
```

### Object.create()创建对象⛏️

通过`Object.create()`创建对象，可以指定对象的原型。

``` javascript
// Object.create使用第一个参数作为新对象的原型
// 因为参数为字面量，因此继承自Object.prototype
// 因此对象o1也继承Object.prototype
let o1 = Object.create({x: 1, y: 2});

// 创建一个没有原型的对象
// 这意味着该对象不会继承任何方法，连toString这种最基本的方法都没有。
let o2 = Object.create(null);

// 创建一个普通的空对象
// 这个对象等价于 {} 对象
let o3 = Object.create(Object.prototype);
```

??? question "Object.create(null)和{}的区别"

    使用`Object.create(null)`创建的对象是一个纯净的空对象，不继承任何属性或方法，适合用于需要一个干净的对象作为存储数据的容器。

    而使用`{}`创建的对象则是继承自Object.prototype的普通对象，可能会包含一些默认的属性和方法。

``` javascript
let o = {x: "不要修改这个值"};
// 这样做可以有效防止对象o在函数中被意外修改
library.function(Object.create(o));
```

## 查询和设置属性

### 查询属性

``` javascript
// 使用点.操作符访问属性
let author = book.author;

// 使用方括号[]操作符访问属性
let title = book["main title"];
```

### 创建或设置属性

``` javascript
book.edition = 7;

book["main title"] = "JavaScript: The Definitive Guide";
```

## 属性继承

对象的属性可以通过`原型链`实现继承效果。

``` javascript
let o = {};
o.x = 1;
// 对象p的原型为对象o
let p = Object.create(o);
p.y = 2;
// 对象q的原型为对象p
let q = Object.create(p);
q.z = 3;

// toString继承自Object.prototype
console.log(q.toString());

// 3;x和y属性继承自o和p
console.log(q.x + q.y); 

// 修改原始对象属性并不会影响原型链中的属性
q.x=5;
console.log(o.x) // 1
```

## 属性访问错误

### 访问属性错误

``` javascript title="属性访问错误问题"
let book = {
    title: "JavaScript: The Definitive Guide"
}

// ✅访问不存在的属性并不会报错
let subtitle = book.subtitle; // undefined

// ❌ 查询不存在属性的属性会报错
let len = book.subtitle.length; //❌报错
```

``` javascript title="解决方案"
let len = undefined;

// 方式一
if (book) {
    if (book.subtitle) {
        len = book.subtitle.length;
    }
}

// 方式二
let len = book && book.subtitle && book.subtitle.length;

//方式三：条件式属性访问
let len = book?.subtitle?.length;
```

### 设置属性错误

同上，尝试在null或undefined对象上设置属性会报错。

其它情况，在对象o上设置属性p会失败：

- o有一个只读自有属性p:不可能设置`只读属性`。
- o有一个只读继承属性p:不可能用`同名自有属性`隐藏`只读继承属性`。
- o的`extensible`属性为false,且o中不存在属性p，此时无法创建新属性p。

## 删除属性

使用`delete`操作符移除属性。

!!! note

    delete操作符不会删除configurable特性为false的属性。

``` javascript
delete book.author; // book对象现在没有author属性了
delete book["main title"]; // book对象现在没有main title属性了

/*
* 删除全局属性
* */
delete globalThis.x;
delete x; // 非严格模式下可以直接删除全局属性
```

!!! warning

    `delete`操作符只能删除对象自身的属性，不能删除原型链上的继承属性。


## 测试属性

``` javascript
let o = {x: 1};

/*
* 使用in操作符
* */
"x" in o; // true
"y" in o; // false
"toString" in o; // true,o继承了toString属性

/*
* hasOwnProperty方法
* 判断是否存在自有属性
* */
o.hasOwnProperty("x"); // true
o.hasOwnProperty("y"); // false
o.hasOwnProperty("toString"); // false

/*
* propertyIsEnumerable方法
* 判断是否存在自有且可枚举属性
* */
o.propertyIsEnumerable("x"); // true
o.propertyIsEnumerable("toString"); // false,o继承了toString属性,且toString不可枚举
Object.prototype.propertyIsEnumerable("toString"); // false,toString不可枚举
```

## 枚举属性

### for...in

``` javascript
for (let p in o) {
    // 跳过继承属性
    if (!o.hasOwnProperty(p)) continue;
    
    // 跳过方法
    if (typeof o[p] === "function") continue;
    
    console.log(p, o[p]);
}
```

### 先获取属性名数组

``` javascript
/*
* 获取可枚举自有属性名数组
* */
const keys = Object.keys(o);
for (key of keys){
    console.log(key, o[key]);
}
```

还有一些方法，功能也是类似的：

- `Object.getOwnPropertyNames()`:获取可枚举自有属性名数组
- `Object.getOwnPropertySymbols()`:获取可枚举继承属性名数组
- `Reflect.ownKeys()`:获取可枚举属性名数组

## 扩展对象

### 手动扩展

``` javascript
let target = {x: 1}, source = {y: 2, z: 3};
for (let key in source) {
    target[key] = source[key];
}
console.log(target);
```

### Object.assign()

``` javascript
let target = {x: 1}, source = {y: 2, z: 3};

Object.assign(target, source);
console.log(target);
```

### ...操作符

``` javascript
let target = {x: 1}, source = {y: 2, z: 3};
let result = {...target, ...source};
console.log(result);
```

## 序列化对象

``` javascript
let o= {x: 1, y: null};

/*
* 序列化对象
* */
let s = JSON.stringify(o);
console.log(s)

/*
* 反序列化字符串为对象
* */
let p = JSON.parse(s);
console.log(p)
```

## 对象方法

### toString()

返回调用它的对象的值的字符串。

``` javascript
let o = {x: 1, y: 2};
console.log(o.toString()); //[object Object]

/*
* 重写toString方法
* */
let point = {
    x:1,
    y:2,
    toString: function () {
        return `(${this.x}, ${this.y})`;
    }
}
console.log(point.toString());
```

### toLocaleString()

返回对象的本地化字符串表示。

Object定义的默认toLocaleString()方法本身没有实现任何本地化，而是简单的调用toString()并返回该值。

``` javascript
console.log(new Date().toLocaleString())
```

### valueOf()

将对象转为某些非字符串原始值。

``` javascript
new Date().valueOf() //1716320593562
```

### toJSON()

`Object.prototype`并没有定义`toJSON()`方法。

但`JSON.stringify()`方法会从序列化对象上寻找`toJSON()`方法。存在就会调用它。

``` javascript
console.log(new Date().toJSON())
// 因为Date对象自己实现了toJSON()，因此JSON.stringify的结果为toJSON方法的返回值
console.log(JSON.stringify(new Date()))
```

## 对象字面量新语法

### 简写属性

``` javascript
let x = 1, y = 2;
let o = {x: x, y: y}

/*
* ES6之后，可以简写为:
* */
o = {x, y}
```

### 计算的属性名

当属性名保存在一个变量中，或是调用的某个函数的返回值。可以直接在对象声明时用方括号表示这些属性名：

同理，`计算属性语法`也可以将`符号`作为`属性名`。

``` javascript
const PROPERTY_NAME = "p1";

function computePropertyName() {
    return "p" + 2;
}

const extension = Symbol("my extension symbol");

let o ={}
o[PROPERTY_NAME] = 1;
o[computePropertyName()] = 2;
o[extension] = 3;

/*
* ES6之后，可以简写为:
* */
let p ={
    [PROPERTY_NAME]: 1,
    [computePropertyName()]: 2,
    [extension]: 3
}
console.log(p) // { p1: 1, p2: 2 }
```

### ...扩展操作符

ES2018及之后，可以使用扩展操作符`...`把已有对象复制到新对象中。

``` javascript
let target = {x: 1}, source = {y: 2, z: 3};
let result = {...target, ...source};
console.log(result);
```

### 简写方法

``` javascript
/*
* ES6之前方法的定义
* */
let square ={
    area: function () {
        return this.side * this.side
    },
    side:10
}

/*
* ES6及之后方法的简写定义
* */
let square = {
    area() {
        return this.side * this.side
    },
    side:10
}
```

### 属性的获取方法与设置方法

ES5中引入`获取方法`与`设置方法`。

``` javascript
let o ={
    // 通过一对函数定义的一个访问器属性
    get x() {
        return this._x;
    },
    set x(value) {
        this._x = value;
    }
}

o.x="hello"
console.log(o.x)
```
