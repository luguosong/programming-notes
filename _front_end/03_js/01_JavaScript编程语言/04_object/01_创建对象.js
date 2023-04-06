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
    this.sayHello = function () {
        console.log("hello3")
    }
}

//创建对象方式三：通过构造函数创建对象
let obj4 = new People("王五", 25, "男");


//对象使用
console.log(obj2.name);
obj2.sayHello()

console.log(obj3.name);
obj3.sayHello();

console.log(obj4.name);
obj4.sayHello();


// 获取指定范围内的随机整数
function getRandom(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

Date
