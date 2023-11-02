//{% raw %}
import React, {useState} from 'react';
import {Alert, Card, Input} from "antd";

function Child({children}) {
    return (
        <div>
            <Card title={"子组件：使用this.props.children获取插槽内容"}>
                {children[0]}
                {children[1]}
                {children[2]}
            </Card>
        </div>
    )
}

function CCom(props) {
    const [msg, setMsg] = useState("默认值")

    return (
        <Card title={"父组件"}>
            <div>
                <Alert message={"父组件中的状态：" + msg}/>
            </div>
            <Child>
                <Alert message="插入内容1" type="success"/>
                <Alert message="插入内容2" type="success"/>
                <Input addonBefore={"插槽中的内容可以直接修改父组件中的状态，减少父子通讯："}
                       value={msg}
                       onChange={(e) => {
                           setMsg(e.target.value)
                       }}></Input>
            </Child>
        </Card>
    );
}

export default CCom;
//{% endraw %}
