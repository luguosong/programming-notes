const path=require("path")

// 拼接路径
console.log(path.join(__dirname, "file.txt"))

// 获取文件后缀名
console.log(path.extname("file.txt")) //.txt

// 获取文件名
console.log(path.basename("file.txt")) //file.txt
console.log(path.basename("file.txt", ".txt")) //file

