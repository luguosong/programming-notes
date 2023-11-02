//{% raw %}
import React, {useState} from 'react';
import style from "./FCom.module.css"
import {Button} from "antd";

function Counter({remark}) {
    const [score, setScore] = useState(0);

    return (
        <div className={style.counter}>
            <h2>{score}</h2>
            <p>{remark}</p>
            <Button onClick={() => setScore(score + 1)}>加一</Button>
        </div>
    )
}

function FCom(props) {
    const [show1, setShow1] = useState(true)
    const [show2, setShow2] = useState(true)
    const [show3, setShow3] = useState(true)
    const [show4, setShow4] = useState(true)
    const [show5, setShow5] = useState(true)

    return (
        <div>
            <h1>每个组件都有完全独立的 state</h1>
            <Counter/>
            <Counter/>

            <h1>组件停止渲染，state状态也会消失</h1>
            {show1 && <Counter/>}
            <input
                type="checkbox"
                checked={show1}
                onChange={e => {
                    setShow1(e.target.checked)
                }}
            />渲染

            <h1>相同位置的相同组件（仅切换属性），状态会被保留</h1>
            {show2 ? <Counter remark={"组件一"}/> : <Counter remark={"组件二"}/>}
            <input
                type="checkbox"
                checked={show2}
                onChange={e => {
                    setShow2(e.target.checked)
                }}
            />切换

            <h1>相同位置的不同组件会使 state 重置</h1>
            {show3 ?
                <div><Counter/></div> :
                <section><Counter/></section>}
            <input
                type="checkbox"
                checked={show3}
                onChange={e => {
                    setShow3(e.target.checked)
                }}
            />切换

            <h1>相同位置相同组件重置组件方式一：将组件渲染在不同的位置</h1>
            {show4 && <Counter/>}
            {!show4 && <Counter/>}
            <input
                type="checkbox"
                checked={show4}
                onChange={e => {
                    setShow4(e.target.checked)
                }}
            />切换

            <h1>相同位置相同组件重置组件方式二：使用 key 来重置 state</h1>
            {show5 ? <Counter key={1}/> : <Counter key={2}/>}
            <input
                type="checkbox"
                checked={show5}
                onChange={e => {
                    setShow5(e.target.checked)
                }}
            />切换
        </div>
    );
}

export default FCom;
//{% endraw %}
