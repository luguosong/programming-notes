// 这是一个构造函数，用于初始化新的Range对象。
// 注意，它不会创建或返回对象，只是初始化当前对象。
function Range(from, to) {
  // 存储这个新的range对象的起始点和结束点（状态）。
  // 这些是非继承属性，是该对象特有的。
  this.from = from
  this.to = to
}

// 所有的Range对象都继承自这个对象。
// 注意，属性名必须为"prototype"才能起作用。
Range.prototype = {
  // 如果x在范围内，返回true，否则返回false。
  // 该方法适用于文本范围、日期范围和数值范围。
  includes: function(x) {
    return this.from <= x && x <= this.to
  },

  // 一个生成器函数，使得类的实例可以迭代。
  // 注意，它只适用于数值范围。
  [Symbol.iterator]: function* () {
    for (let x = Math.ceil(this.from); x <= this.to; x++) yield x
  },

  // 返回范围的字符串表示形式
  toString: function() {
    return "(" + this.from + "..." + this.to + ")"
  },
}

/*
* 测试
* */

let r = new Range(1, 3) // 创建一个范围对象
console.log(r.includes(2)) // => true: 2 在这个范围内
console.log(r.toString()) // => "(1...3)"
console.log([...r]) // => [1, 2, 3]; 通过迭代器迭代这个范围对象
