class Range {
  constructor(from, to) {
    // 存储此新的范围对象的起始点和结束点（状态）。
    // 这些是非继承属性，是此对象独有的。
    this.from = from
    this.to = to
  }

  // 如果 x 在范围内，则返回 true，否则返回 false。
  // 此方法适用于文本、日期范围以及数值范围。
  includes(x) {
    return this.from <= x && x <= this.to
  }

  // 一个生成器函数，用于使类的实例可迭代。
  // 注意，它仅适用于数值范围。
  * [Symbol.iterator]() {
    for (let x = Math.ceil(this.from); x <= this.to; x++) yield x
  }

  // 返回范围的字符串表示形式
  toString() {
    return `(${this.from}...${this.to})`
  }

  static parse() {
    console.log("静态方法")
  }
}


let r = new Range(1, 3)     // 创建一个Range对象
console.log(r.includes(2))  // => true: 2 在这个范围内
console.log(r.toString())   // => "(1...3)"
// r.parse()   // 报错：TypeError: r.parse is not a function

// 静态方法可以通过类来调用，而不是通过实例来调用
Range.parse() // => "静态方法"
