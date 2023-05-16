/*
* 简写属性
* */
let x = 1, y = 2
let o = { x, y }
console.log(o) //{x:1,y:2}

/*
* 符号作为属性名
* */
const extension = Symbol("extension symbol")

// 创建一个对象，并使用计算属性语法将符号作为属性名
o = {
  [extension]: { /* 这个对象中存储扩展数据 */ },
}
// 设置符号属性的值
o[extension].x = 0 // 这个属性不会与o的其他属性冲突
console.log(o[extension].x) // 输出结果: 0
// 使用Object.getOwnPropertySymbols()找到对象的符号属性
const symbols = Object.getOwnPropertySymbols(o)
console.log(symbols) // 输出结果: [ Symbol(extension symbol) ]
