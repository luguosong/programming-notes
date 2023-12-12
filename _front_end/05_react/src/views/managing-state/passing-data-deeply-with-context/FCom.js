//{% raw %}
import React, {createContext, useContext, useState} from 'react';
import {Button, Card} from "antd";

// 1️⃣创建 context
const Context = createContext(0);

function FCom(props) {

    const [count, setCount] = useState(0)

    return (
        <div>
            {/*3️⃣提供 context*/}
            <Context.Provider value={count}>
                <Button onClick={() => setCount(count + 1)}>+1</Button>
                <p>{count}</p>
                <Child1/>
            </Context.Provider>
        </div>
    );
}

function Child1() {
    return (
        <Card title={"子组件1"}>
            <Child2/>
        </Card>
    );
}

function Child2() {
    // 2️⃣使用 Context
    const count = useContext(Context)

    return (
        <Card title={"子组件2"}>
            {count}
        </Card>
    );
}


export default FCom;
//{% endraw %}
