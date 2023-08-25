const fs = require("fs")

// 写入文件内容
fs.writeFile("./file.txt", "hello world", (err) => {
  if (err) {
    return console.log("文件写入失败")
  }
  console.log("文件写入成功")
})

// 读取文件内容
fs.readFile("./file.txt", (err, data) => {
  if (err) {
    return console.log("文件读取失败")
  }
  console.log("文件读取：", data.toString("utf8"))
})

