import React from "react"; //引入react核心包
import ReactDOM from "react-dom"; //引入ReactDOM，可以将react元素渲染到页面上

/**
 * 将内容渲染到DOM中去
 */
ReactDOM.render(
    <div>
        <h1>hello react</h1>
        <p>hello world</p>
    </div>,
    document.getElementById("root")
);
