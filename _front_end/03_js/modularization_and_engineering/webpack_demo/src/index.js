// 由于package.json中并没有配置type: "module"，因此这段代码并不能直接运行，需要打包后才能执行。
import "./other1.js"
import "./other2.js"
import "./css/index.css" //引入css文件
import "./less/index.less" //引入less文件
import img1 from "./img/site-logo.c0e60df418e04f58.svg" //引入图片

// 向页面插入内容
const contentContainer = document.getElementById("content")
const paragraph = document.createElement("p")
paragraph.textContent = "js插入的内容"
contentContainer.appendChild(paragraph)

// 创建img标签,并导入到html中
const img = document.createElement("img")
img.src = img1
contentContainer.appendChild(img)
