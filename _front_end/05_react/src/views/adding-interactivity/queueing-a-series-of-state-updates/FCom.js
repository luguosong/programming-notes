//{% raw %}
import React, {useState} from 'react';
import {Button} from "antd";

function FCom(props) {
    const [number, setNumber] = useState(0);

    return (
        <div>
            <h1>{number}</h1>
            <Button onClick={() => {
                // 当setNumber接收的是一个函数而不是一个值时，React会将这个函数放入一个队列中，等到下一次渲染时再执行
                setNumber(n => n + 1);
                setNumber(n => n + 1);
                setNumber(n => n + 1);
            }}>+3</Button>
        </div>
    );
}

export default FCom;
//{% endraw %}
