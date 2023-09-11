// react核心模块
import React from 'react';
// 用于将React组件渲染到页面中
import ReactDOM from 'react-dom/client';
// 全局样式
import './index.css';
// 根组件
import App from './App';
import reportWebVitals from './reportWebVitals';

// 根据public/index.html中的标签创建节点对象
const root = ReactDOM.createRoot(document.getElementById('root'));
// 将组件渲染到节点
root.render(
    <App/>
);


// 如果你想开始测量应用程序的性能，请传递一个函数
// 用于记录结果（例如：reportWebVitals(console.log)）
// 或发送到分析端点。了解更多：https://bit.ly/CRA-vitals
reportWebVitals();
