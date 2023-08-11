// 保存原始的console.log函数
const originalConsoleLog = console.log

// 重写console.log函数
console.log = function(message, color) {

  if (color === undefined) color = "#666"

  if (typeof message === "number") {
    color = "#9980ff"
  }

  if (typeof message === "boolean") {
    color = "#4181c6"
  }

  if (typeof message === "undefined") {
    color = "#da5656"
  }

  // 创建一个新的div元素来容纳输出内容
  const logDiv = document.createElement("div")

  logDiv.textContent = message + " "
  logDiv.style.color = color

  // 将新的div元素添加到body中
  document.body.appendChild(logDiv)

  // 如果你还想保留原始console.log行为，可以取消注释下面的代码
  originalConsoleLog.call(console, message)
}

window.onerror = function(message, source, lineno, colno, error) {
  console.log(message, "red")
}
