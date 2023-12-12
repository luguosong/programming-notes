//{% raw %}
import React, {useReducer} from 'react';
import {Button, Space} from "antd";



function FCom(props) {

    // 3️⃣在组件中使用 reducer
    const [count, dispatch] = useReducer(countReducer, 0);

    return (
        <div>
            <p>{count}</p>
            <Space>
                <Button onClick={() => {
                    // 1️⃣将设置状态的逻辑修改成 dispatch 的一个 action
                    dispatch(
                        // "action" 对象
                        {type: "add"})
                }}>加一
                </Button>
                <Button onClick={() => {
                    // 1️⃣将设置状态的逻辑修改成 dispatch 的一个 action
                    dispatch(
                        // "action" 对象
                        {type: "sub"})
                }}>减一
                </Button>
                <Button onClick={() => {
                    // 1️⃣将设置状态的逻辑修改成 dispatch 的一个 action
                    dispatch(
                        // "action" 对象
                        {type: "reset"})
                }}>清零
                </Button>
            </Space>
        </div>
    );
}

/*
* 2️⃣编写一个 reducer 函数
*
* 由于 reducer 函数接受 state（tasks）作为参数，因此你可以 在组件之外声明它。这减少了代码的缩进级别，提升了代码的可读性。
* */
function countReducer(count, action) {
    switch (action.type) {
        case "add":
            return count + 1;
        case "sub":
            return count - 1;
        case "reset":
            return 0;
        default:
            return count;
    }
}

export default FCom;
//{% endraw %}
