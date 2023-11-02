//{% raw %}
import React, {useState} from 'react';
import {Card} from "antd";

function Child({title,isActive, onShow, children}) {
    return (
        <div>
            <Card title={title}>
                {isActive ? (
                    <p>{children}</p>
                ) : (
                    <button onClick={onShow}>显示</button>
                )}
            </Card>
        </div>
    )
}

function FCom(props) {
    const [activeIndex, setActiveIndex] = useState(0)

    return (
        <div>
            <p>父组件状态：{activeIndex}</p>
            <Child title={"组件1"} isActive={activeIndex === 0} onShow={() => {
                setActiveIndex(0)
            }}>
                子组件1内容
            </Child>
            <Child title={"组件2"} isActive={activeIndex === 1} onShow={() => {
                setActiveIndex(1)
            }}>
                子组件2内容
            </Child>
        </div>
    );
}

export default FCom;
//{% endraw %}
