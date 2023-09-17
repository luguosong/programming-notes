import React, {useEffect, useState} from 'react';
import {Button, Card} from "antd";

/*
* ❌不使用useEffect的情况
* */
function Child1() {

    const [count, setCount] = useState(0);

    // 不使用useEffect，setCount会触发组件重新渲染
    // 从而导致又调用setTimeout，从而导致死循环
    setTimeout(() => {

        setCount(count < 20 ? count + 1 : 0);
    }, 1000);

    return (
        <Card title={"不使用useEffect"}>
            <p>不使用useEffect,组件会进入死循环</p>
            <div>count: {count}</div>
        </Card>
    );
}

/*
* 使用useEffect解决死循环问题
* */
function Child2() {

    const [count, setCount] = useState(0);

    // 当数组为空，相当于类组件中的componentDidMount，只会执行一次
    useEffect(() => {
            setTimeout(() => {
                setCount(count + 1);
            }, 1000);
        },
        // eslint-disable-next-line
        []);

    return (
        <Card title={"使用useEffect"}>
            <p>使用useEffect,组件不会进入死循环</p>
            <div>count: {count}</div>
        </Card>
    );
}

/*
* 模拟componentDidUpdate
* */
function Child3() {
    const [count, setCount] = useState(0);
    const [msg, setMsg] = useState("");

    // 这类似于类组件中的componentDidUpdate
    useEffect(() => {
        setMsg(count % 2 === 0 ? "偶数" : "奇数")
    }, [count]);


    return (
        <Card title={"使用useEffect监听状态的更新"}>
            <div>count:{count},是一个{msg}</div>
            <Button onClick={() => setCount(count + 1)}>更新状态</Button>
        </Card>
    )
}

/*
* 模拟componentWillUnmount
* */
function Child4() {

    useEffect(() => {
        return () => {
            alert("组件被销毁")
        }
    });

    return (
        <Card title={"useEffect模拟销毁函数"}>
            <p>组件被销毁时，会执行return中的函数</p>
        </Card>
    )
}

function FCom(props) {

    const [show, setShow] = useState(true);

    return (
        <div>
            <Child1/>
            <Child2/>
            <Child3/>
            <Button onClick={() => {
                setShow(!show);
            }}>{show ? "销毁" : "创建"}</Button>
            {show && <Child4/>}
        </div>
    );
}

export default FCom;
