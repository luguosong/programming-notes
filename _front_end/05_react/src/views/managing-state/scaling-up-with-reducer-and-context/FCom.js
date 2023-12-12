//{% raw %}
import React, {useContext} from 'react';
import {Button, Card} from "antd";
import {CountContext, CountDispatchContext, CountProvider} from "./CountProvider";

function FCom(props) {
    return (
        // ⭐将组件用Provider包裹起来
        <CountProvider>
            <Child1/>
        </CountProvider>

    );
}


// 子组件
function Child1() {

    const count = useContext(CountContext)

    return (
        <div>
            <Card title={"子组件1"}>
                全局状态：{count}
                <Child11/>
                <Child12/>
            </Card>
        </div>
    )
}

// 孙子组件
function Child11() {
    const dispatch = useContext(CountDispatchContext);

    return (
        <div>
            <Card title={"子组件1-1"}>
                <Button onClick={() => dispatch({type: "add"})}>add</Button>
            </Card>
        </div>
    )
}

// 孙子组件
function Child12() {

    const dispatch = useContext(CountDispatchContext);

    return (
        <div>
            <Card title={"子组件1-2"}>
                <Button onClick={() => dispatch({type: "minus"})}>minus</Button>
            </Card>
        </div>
    )
}


export default FCom;
//{% endraw %}
