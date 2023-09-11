const name2 = "李四"

// 使用exports导出，只能导出一个方法，不能像module.exports一样导出一个对象
exports.sayHi2 = function () {
  console.log(`hi,i am ${name2}`)
}
