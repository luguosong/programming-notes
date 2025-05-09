/*
* 数组解构
* */
let [a, , b, ...c] = [1, 2, 3, 4, 5, 6, 7, 8, 9]
console.log(a) // 1
console.log(b) // 3
console.log(c) // [4, 5, 6, 7, 8, 9]

/*
* 对象解构
* */
let { x, y, z: zz } = { x: 1, y: 2, z: 3 }
console.log(x) // 1
console.log(y) // 2
console.log(zz) // 3,重命名属性名
