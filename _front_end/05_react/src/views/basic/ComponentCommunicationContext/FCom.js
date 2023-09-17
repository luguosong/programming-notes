import React, {createContext, useContext, useState} from 'react';
import {Card, Input} from "antd";

const context = createContext();

const {Provider} = context;

function Child1(props) {
    // 使用useContext可以简化代码
    const value = useContext(context);

    return (
        <div>
            <Card title={"子组件1"}>
                <Input addonBefore={"子组件1修改内容"} onChange={(e) => {
                    value.setMsg(e.target.value)
                }}></Input>
            </Card>
        </div>
    );
}


function Child2(props) {
    // 使用useContext可以简化代码
    const value = useContext(context);

    return (
        <div>
            <Card title={"子组件2"}>
                接收来自子组件1的内容：{value.msg}
            </Card>
        </div>
    );
}

function FCom(props) {

    const [msg, setMsg] = useState("");

    return (
        <Provider value={{
            msg: msg,
            setMsg: setMsg
        }}>
            <div>
                <Child1></Child1>
                <Child2></Child2>
            </div>
        </Provider>
    );
}

export default FCom;
