import React, {useState} from 'react';
import Compact from "antd/es/space/Compact";
import {Button, Input, List} from "antd";

function FCom(props) {

    // 使用 useState 来声明组件内部的状态
    const [val, setVal] = useState('');
    const [list, setList] = useState([]);

    return (
        <div>
            <Compact>
                <Input value={val} onChange={(e) => {
                    // 当输入框的值发生变化时，将输入框的值赋值给state
                    setVal(e.target.value)
                }}/>
                <Button onClick={() => {
                    // 当点击按钮时，将输入框的值添加到list中，并清空输入框的值
                    setList([...list, val]);
                    setVal("");
                }}>add
                </Button>
            </Compact>
            <List dataSource={list} renderItem={(item) => <List.Item>{item}</List.Item>}/>
        </div>
    );
}

export default FCom;
