//{% raw %}
import React, {forwardRef, useImperativeHandle, useRef} from 'react';
import {Button, Card, message, Space} from "antd";

function FCom(props) {
    const myInputRef = useRef(null);
    const myInputRef2 = useRef(null);

    return (
        <Space direction={"vertical"}>

            <Card title={"ref访问另一个组件中的DOM"}>
                {/*1️⃣告诉 React 将对应的 DOM 节点放入 inputRef.current 中。但是，这取决于 MyInput 组件是否允许这种行为， 默认情况下是不允许的。*/}
                <MyInput ref={myInputRef}/>
                <Button onClick={() => {
                    myInputRef.current.focus();
                }}>聚焦
                </Button>
                <Button onClick={() => {
                    myInputRef.current.setAttribute("value", myInputRef.current.value + "1");
                }}>插入数据
                </Button>
                <Button onClick={() => {
                    myInputRef.current.setAttribute("value", "");
                }}>清空数据
                </Button>
            </Card>

            <Card title={"对ref允许访问的DOM api进行限制"}>
                <MyInput2 ref={myInputRef2}/>
                <Button onClick={() => {
                    myInputRef2.current.focus();
                }}>聚焦
                </Button>
                <Button onClick={() => {
                    myInputRef2.current.setAttribute("value", myInputRef2.current.value + "1");
                }}>❌插入数据
                </Button>
                <Button onClick={() => {
                    myInputRef2.current.setAttribute("value", "");
                }}>❌清空数据
                </Button>
            </Card>
        </Space>
    );
}

// 2️⃣MyInput 组件是使用 forwardRef 声明的。 这让从上面接收的 inputRef 作为第二个参数 ref 传入组件，第一个参数是 props 。
const MyInput = forwardRef((props, ref) => {
    return (
        <Card title={"MyInput自定义组件"}>
            {/*3️⃣MyInput 组件将自己接收到的 ref 传递给它内部的 <input>。*/}
            <input {...props} ref={ref}/>
        </Card>
    );
});


// 对ref允许访问的DOM api进行限制
const MyInput2 = forwardRef((props, ref) => {

    const realInputRef = useRef(null);
    /*
    * 使用useImperativeHandle对ref允许访问的DOM api进行限制
    * */
    useImperativeHandle(ref, () => ({
        // 只暴露 focus，没有别的
        focus() {
            realInputRef.current.focus();
        },
        setAttribute() {
            // 给出友好提示
            message.info("禁止访问")
        }
    }));

    return (
        <Card title={"MyInput自定义组件"}>
            {/*3️⃣MyInput 组件将自己接收到的 ref 传递给它内部的 <input>。*/}
            <input {...props} ref={realInputRef}/>
        </Card>
    );
});


export default FCom;
//{% endraw %}
