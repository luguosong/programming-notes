//{% raw %}
import React, {useEffect, useRef, useState} from 'react';
import {Button, Card, message, Space} from "antd";

function Child({num1}) {

    const [num2, setNum2] = useState(0)

    useEffect(() => {
        message.info("渲染组件");
        return () => {
            // 卸载函数
        }
    });

    useEffect(() => {
        message.info("挂载组件");
        return () => {
            // 卸载函数
        }
    }, []);

    useEffect(() => {
        message.info("num1更新");
        return () => {
            // 卸载函数
        }
    }, [num1]);

    useEffect(() => {
        message.info("num2更新");
        return () => {
            // 卸载函数
        }
    }, [num2]);


    return (
        <Card title={"子组件"}>
            <div>num1:{num1}</div>
            <div>num2:{num2}</div>
            <Button onClick={() => setNum2(Math.random())}>更新num2，状态变化，重新渲染组件</Button>
        </Card>
    )
}

function FCom(props) {
    const [show, setShow] = useState(true)
    const [num, setNum] = useState(0)

    return (
        <div>
            <Space>
                <Button onClick={() => setShow(!show)}>挂载/卸载</Button>
                <Button onClick={() => setNum(num + 1)}>属性变化，重新渲染组件,num1+1</Button>
            </Space>
            {show && <Child num1={num}/>}
        </div>
    );
}

export default FCom;
//{% endraw %}
