// 默认导入
import other1 from "./other1.js"
// 按需导入
import { sayHi, sayHello } from "./other2.js"
// 直接导入
import "./other3.js"

other1.sayHi()
other1.sayHello()

sayHi()
sayHello()
