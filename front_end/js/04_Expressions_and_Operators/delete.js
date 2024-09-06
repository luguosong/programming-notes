/*
  * 删除对象中的属性，属性就不存在了
  * */
let o = { x: 1, y: 2 }
delete o.x //true,删除x属性
console.log(JSON.stringify(o)) // {y: 2}

/*
* 删除数组，会留下一个坑
* */
let a = [1, 2, 3]
delete a[1] //true,删除索引为2的元素
console.log(a) // [1, empty, 3]
