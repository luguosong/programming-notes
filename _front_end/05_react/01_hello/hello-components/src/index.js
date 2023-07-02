import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App'; //引入自定义组件，⭐首字母必须大写，以区分于HTML标签

// 使用ReactDOM.createRoot()方法创建根组件
const root = ReactDOM.createRoot(document.getElementById('root'));
// 使用ReactDOM.render()方法将自定义组件渲染到页面上
root.render(<App/>);

// 以上代码也可以一句话搞定
// ReactDOM.render(<App/>,document.getElementById('root'));
