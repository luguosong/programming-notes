import React, {useCallback, useState} from 'react';
import {Button} from "antd";

function FCom(props) {
    const [count, setCount] = useState(0);

    /*
    * ⭐只有当count变化时，才会重新定义handleClick函数
    * */
    const handleClick = useCallback(() => {
        alert(count)
    }, [count]);

    return (
        <div>
            <div>count:{count}</div>
            <Button onClick={() => {
                setCount(count + 1)
            }}>
                增加
            </Button>

            <Button onClick={() => {
                handleClick()
            }}>点击
            </Button>
        </div>
    );
}

export default FCom;
