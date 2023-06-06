// 导入全局样式文件
// import './assets/main.css'

// 导入Vue的createApp函数
import { createApp } from "vue"

// 导入Pinia的createPinia函数
import { createPinia } from "pinia"

// 导入Element Plus
import ElementPlus from "element-plus"
import "element-plus/dist/index.css"

// 导入根组件App.vue
import App from "./App.vue"

// 导入路由配置
import router from "./router"

// 创建Vue应用程序实例
const app = createApp(App)

// 使用Element Plus
app.use(ElementPlus)

// 使用Pinia状态管理库
app.use(createPinia())

// 使用路由配置
app.use(router)

// 将应用程序挂载到指定的DOM元素上（id为app的元素）
app.mount("#app")
