//{% raw %}
import React, {useState} from 'react';
import {Button} from "antd";

function FCom(props) {
    const [count, setCount] = useState(0);

    return (
        <div>
            <Button onClick={() => {
                setCount(count + 1)
                setCount(count + 1)
                setCount(count + 1)
                alert("count并不会立即改变，而是被拍摄成快照，在下一次渲染时改变：" + count)

                setTimeout(() => {
                    alert("一个 state 变量的值永远不会在一次渲染的内部发生变化， 即使其事件处理函数的代码是异步的：" + count);
                }, 1000);
            }}>点击{count}</Button>
        </div>
    );
}

export default FCom;
//{% endraw %}
