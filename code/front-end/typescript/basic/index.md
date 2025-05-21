# 基本语法

## 环境搭建

- 下载、安装Node.js
- 全局安装TypeScript：`npm install -g typescript@next`
- 编写测试ts代码：

``` typescript title="hello_world.ts"
--8<-- "code/front-end/typescript/basic/hello_world.ts"
```

- 编译ts代码：`tsc hello_world.ts`，得到js文件`hello_world.js`

!!! 编译细节

    - 默认情况下，即使编译出错，仍然会生成js文件。
    - 声明的let变量会编译成var，以确保兼容任意版本的JavaScript标准。

## 基本数据类型

```typescript
// 布尔值类型
let isDone: boolean = false;

// 数值类型
let decLiteral: number = 6;

// 字符串类型
let myName: string = 'Tom';

// 空值
function alertName(): void {
    alert('My name is Tom');
}

// 声明一个 void 类型的变量没有什么用，因为你只能将它赋值为 undefined 和 null
let unusable: void = undefined;

// null 和 undefined
let u: undefined = undefined;
let n: null = null;
```

!!! warning

    `undefined`和`null`是所有类型的子类型,因此可以赋值给任何类型。

    比如：`let num: number = undefined;`

    而`void`则不能赋值给其它类型的变量。

## 任意值

任意值（Any）用来表示允许赋值为任意类型。

```typescript
// 允许被赋值为任意类型。
let myFavoriteNumber: any = 'seven';
myFavoriteNumber = 7;
```

## 类型推论

如果没有明确的指定类型，那么 TypeScript 会依照类型推论（Type Inference）的规则推断出一个类型。

```typescript
// TypeScript 会在没有明确的指定类型的时候推测出一个类型，这就是类型推论。
let myFavoriteNumber = 'seven';
myFavoriteNumber = 7; // ❌index.ts(2,1): error TS2322: Type 'number' is not assignable to type 'string'.
```

```typescript
// ❗如果定义的时候没有赋值，不管之后有没有赋值
// 都会被推断成 any 类型而完全不被类型检查
let myFavoriteNumber;
myFavoriteNumber = 'seven';
myFavoriteNumber = 7; // ✅
```

## 联合类型

联合类型（Union Types）表示取值可以为多种类型中的一种。

```typescript
let myFavoriteNumber: string | number;
myFavoriteNumber = 'seven';
myFavoriteNumber = 7;
```

只能访问此联合类型的所有类型里共有的属性或方法：

```typescript 
function getLength(something: string | number): void {
    // something.length; // ❌length 不是 string 和 number 的共有属性，所以会报错。
    something.toString(); //✅toString 是 string 和 number 的共有方法，所以不会报错。
}

// 联合类型的变量在被赋值的时候，会根据类型推论的规则推断出一个类型：
let myFavoriteNumber: string | number;
myFavoriteNumber = 'seven';
console.log(myFavoriteNumber.length); // 5
```

## 对象类型-接口

在 TypeScript 中，我们使用接口（Interfaces）来定义对象的类型。

### 基本使用

接口一般首字母大写。

可以通过`?`定义任意属性。

```  typescript
--8<-- "code/front-end/typescript/basic/interfaces.ts"
```

通过`tsc`编译发现，编译后的结果只剩对象，并不会保留接口：

```  javascript
--8<-- "code/front-end/typescript/basic/interfaces.js"
```

### 任意属性

```typescript
interface Person {
    name: string;
    age?: number;
    [propName: string]: string | number;
}

let tom: Person = {
    name: 'Tom',
    age: 25,
    gender: 'male'
};
```

!!! note

    一旦定义了`任意属性`，那么`确定属性`和`可选属性`的类型都必须是它的类型的`子集`

### 只读属性

```typescript
interface Person {
    readonly id: number;
}

let tom: Person = {
    id: 8975
};

// tom.id = 9527; //❌报错，因为id是只读
```

## 数组类型

在 TypeScript 中，数组类型有多种定义方式。

### 方式1:类型+方括号

```typescript
let fibonacci: number[] = [1, 1, 2, 3, 5];
```

### 方式2:数组泛型

```typescript
let fibonacci: Array<number> = [1, 1, 2, 3, 5];
```

### 方式3:接口表示数组

```typescript
interface NumberArray {
    [index: number]: number;
}
let fibonacci: NumberArray = [1, 1, 2, 3, 5];
```

## 函数类型

### 函数声明方式

```typescript
function sum(x: number, y: number): number {
    return x + y;
}
```

### 函数表达式方式

```typescript
let mySum: (x: number, y: number) => number = function (x: number, y: number): number {
    return x + y;
};
```

### 用接口定义函数的形状

```typescript
interface SearchFunc {
    (source: string, subString: string): boolean;
}

let mySearch: SearchFunc;
mySearch = function(source: string, subString: string) {
    return source.search(subString) !== -1;
}
```

### 可选参数

```typescript
function buildName(firstName: string, lastName?: string) {
    if (lastName) {
        return firstName + ' ' + lastName;
    } else {
        return firstName;
    }
}
let tomcat = buildName('Tom', 'Cat');
let tom = buildName('Tom');
```

!!! warning

    可选参数必须接在必需参数后面。换句话说，可选参数后面不允许再出现必需参数了

### 参数默认值

TypeScript 会将添加了默认值的参数识别为可选参数。

```typescript
function buildName(firstName: string, lastName: string = 'Cat') {
    return firstName + ' ' + lastName;
}
let tomcat = buildName('Tom', 'Cat');
let tom = buildName('Tom');
```

### 剩余参数

```typescript
function push(array: any[], ...items: any[]) {
    items.forEach(function(item) {
        array.push(item);
    });
}

let a = [];
push(a, 1, 2, 3);
```

### 重载

```typescript
// 函数定义
function reverse(x: number): number;
// 函数定义
function reverse(x: string): string;
// 函数实现
function reverse(x: number | string): number | string | void {
    if (typeof x === 'number') {
        return Number(x.toString().split('').reverse().join(''));
    } else if (typeof x === 'string') {
        return x.split('').reverse().join('');
    }
}
```

## 类型断言

用来手动指定一个值的类型。

### 将一个联合类型断言为其中一个类型

```typescript
interface Cat {
    name: string;
    run(): void;
}
interface Fish {
    name: string;
    swim(): void;
}

function isFish(animal: Cat | Fish) {
    // 不使用断言❌报错：属性'swim'在类型'Cat'上不存在。
    // if (typeof animal.swim === 'function') {
    //     return true;
    // }
    
    // 使用断言将
    if (typeof (animal as Fish).swim === 'function') {
        return true;
    }
    return false;
}
```


