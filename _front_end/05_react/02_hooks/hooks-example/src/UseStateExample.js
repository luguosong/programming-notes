import React from "react";

export default function UseStateExample() {

    // useState 返回一个数组，数组的第一个元素是状态，第二个元素是一个函数，用于更新状态
    const [count, setCount] = React.useState(0)

    return (
        <div>
            <h1>useState-状态</h1>
            {count}
            <button onClick={() => {
                setCount(count + 1)
            }}>状态更新
            </button>
        </div>
    )
}
