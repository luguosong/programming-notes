---
layout: default
title: vue组件封装到npm
nav_order: 50
parent: npm
---

- 创建Vue项目
- 在src下面新建一个package文件夹用来存放所有需要上传的组件。
- 在package文件夹下新建`index.js`用于组件注册

```js
//package/index.js
import xxx1 from "xxx1"; // 引入封装好的组件
import xxx2 from "xxx2"; // 引入封装好的组件
const coms = [xxx1,xxx2]; // 将来如果有其它组件,都可以写到这个数组里

// 批量组件注册
const install = function (Vue) {
  coms.forEach((com) => {
    Vue.component(com.name, com);
  });
};

export default install; // 这个方法以后再使用的时候可以被use调用
```
