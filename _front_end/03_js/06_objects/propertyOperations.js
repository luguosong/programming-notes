let user = {
  name: "John",
  age: 30,
  "likes birds": true,  // 多词属性名必须加引号
}

/*
* 设置属性
* */
user.isAdmin = true

/*
* 读取属性
* */
console.log(user.name) //John
console.log(user["age"]) //30
console.log(user["likes birds"]) //true,使用方括号读取属性

/*
* 删除属性
* */
delete user.age

/*
* 测试属性
*/
//in操作符要求左边是一个属性名，右边是一个对象。如果对象有包含相应名字的自有属性或继承属性，将返回true
console.log("name" in user) //true,user自有属性name
console.log("toString" in user) //true，user继承了toString属性
console.log("age" in user) //false,age是已删除的属性
//使用 !== undefined 替代 in
console.log(user.name !== undefined)
//hasOwnProperty()方法用于检查对象自身属性中是否存在指定属性。
console.log(user.hasOwnProperty("name")) //true,name是自有属性
console.log(user.hasOwnProperty("toString")) //false,toString是继承属性
//propertyIsEnumerable方法用于检查对象自身属性中是否存在指定属性，并且该属性是否可枚举。
console.log(user.propertyIsEnumerable("name")) //true,name是自有属性
console.log(user.propertyIsEnumerable("toString")) //false,toString是继承属性

/*
* 枚举属性
* */
//方式一：
for (let p in user) {
  console.log(p + ":" + user[p])
}
//方式二：
let keys1 = Object.keys(user)
for (let i = 0; i < keys1.length; i++) {
  console.log(keys1[i] + ":" + user[keys1[i]])
}



