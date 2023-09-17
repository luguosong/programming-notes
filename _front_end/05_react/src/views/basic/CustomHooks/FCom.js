import React, {useState} from 'react';
import {Button} from "antd";

// 自定义 Hook：用于管理计数器
function useCounter(initialValue = 0) {
    const [count, setCount] = useState(initialValue);

    const increment = () => {
        setCount(count + 1);
    };

    const decrement = () => {
        setCount(count - 1);
    };

    return {
        count,
        increment,
        decrement,
    };
}



function FCom(props) {

    // 使用自定义 Hook 来管理计数器状态
    const { count, increment, decrement } = useCounter(0);

    return (
        <div>
            <h1>计数器</h1>
            <p>Count: {count}</p>
            <Button onClick={increment}>增加</Button>
            <Button onClick={decrement}>减少</Button>
        </div>
    );
}

export default FCom;
