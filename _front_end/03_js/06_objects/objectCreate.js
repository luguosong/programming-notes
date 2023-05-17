/*
* 方式一：
* */
let user1 = new Object() // “构造函数” 的语法

/*
* 方式二：
* */
let user2 = {}  // “字面量” 的语法

/*
* 方式三：
* */
//o3 与｛｝或 new ObjectO 类似
let user3 = Object.create(Object.prototype) //参数为对象原型
//以创建一个没有原型的新对象
//user4不继承任何属性或方法
let user4 = Object.create(null)
