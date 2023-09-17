import React, { useMemo, useState} from 'react';
import {Button} from "antd";

function FCom(props) {
    const [count, setCount] = useState(0);

    /*
    * ⭐相当于
    * */
    const result = useMemo(() => {
        return ( <div>计算属性:{count * 2}</div>)
    }, [count]);

    return (
        <div>
            <div>{result}</div>
            <Button onClick={() => {
                setCount(count + 1)
            }}>
                增加
            </Button>
        </div>
    );
}

export default FCom;
