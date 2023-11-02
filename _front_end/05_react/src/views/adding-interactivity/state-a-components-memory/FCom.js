//{% raw %}
import React, {useState} from 'react';
import {Button, Tag} from "antd";

function FCom(props) {
    // 普通变量
    let index1 = 0;
    // state
    const [index2, setIndex2] = useState(0)

    return (
        <div>
            <div>
                <Tag>
                    变量 index1：{index1}
                </Tag>
                <Button onClick={() => {
                    index1 += 1
                    alert("当前index1为：" + index1 + ",组件没有重新渲染")
                }}>+1</Button>
            </div>
            <div>
                <Tag>
                    state index2:{index2}
                </Tag>
                <Button onClick={() => {
                    setIndex2(index2 + 1)
                    alert("组件重新渲染，变量index1被初始化为：" + index1)
                }}>+1</Button>
            </div>
        </div>
    );
}

export default FCom;
//{% endraw %}
