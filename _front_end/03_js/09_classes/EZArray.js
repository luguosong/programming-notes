// 一个简单的 Array 子类，添加了获取第一个和最后一个元素的 getter 方法。
class EZArray extends Array {
  get first() {
    return this[0]
  }

  get last() {
    return this[this.length - 1]
  }
}

let a = new EZArray()
console.log(a instanceof EZArray)  // => true：a 是子类的实例
console.log(a instanceof Array)  // => true：a 也是超类的实例。
console.log(a.push(1, 2, 3, 4))  // a.length == 4；我们可以使用继承的方法
console.log(a.pop())  // => 4：另一个继承的方法
console.log(a.first)  // => 1：子类定义的第一个 getter
console.log(a.last)  // => 3：子类定义的最后一个 getter
console.log(a[1])  // => 2：常规数组访问语法仍然适用。
console.log(Array.isArray(a))  // => true：子类实例确实是一个数组
console.log(EZArray.isArray(a))  // => true：子类也继承了静态方法！

// EZArray 继承了实例方法，因为 EZArray.prototype 继承自 Array.prototype
console.log(Array.prototype.isPrototypeOf(EZArray.prototype)) // => true

// EZArray 继承了静态方法和属性，因为 EZArray 继承自 Array。这是 extends 关键字的特殊功能，在 ES6 之前是不可能实现的。
console.log(Array.isPrototypeOf(EZArray)) // => true
