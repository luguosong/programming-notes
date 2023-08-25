const http = require("http")

// 创建服务器实例
const server = http.createServer()

//启动服务器
server.listen(3000, () => {
  console.log("服务器已启动")
})

// 监听请求
server.on("request", (req, res) => {
  console.log("==========收到一条请求==========")
  console.log("收到请求，请求路径是：" + req.url + "，请求方法是：" + req.method )
  for (let key in req.headers) {
    console.log(key + "：" + req.headers[key])
  }

  // 响应请求
  res.statusCode = 200
  res.setHeader("Content-Type", "text/plain;charset=utf-8") //解决中文乱码
  res.end("来自服务器的响应")
})
