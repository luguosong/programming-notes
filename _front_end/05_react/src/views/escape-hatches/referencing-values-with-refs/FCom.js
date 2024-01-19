//{% raw %}
import React, {forwardRef, useRef, useState} from 'react';
import {Button, Input, message, Space, Tag} from "antd";

function FCom(props) {

    message.info("组件渲染了");

    const count1 = useRef(0);

    const [count2, setCount2] = useState(0)

    const inputRef = useRef(null);

    return (
        <div>

            <h2>ref可以保留组件的状态，但是不会触发组件的重新渲染</h2>
            <div>
                <Tag>{count2}</Tag>
                <Button onClick={() => {
                    setCount2(count2 + 1);
                    message.info("state状态改变");
                }}>state加1（state改变，组件重新渲染）
                </Button>
            </div>

            <div>
                {/*❗违背了ref的使用原则：不要在渲染过程中读取或写入 ref.current*/}
                <Tag>{count1.current}</Tag>
                {count1.current % 2 === 0 ? <Tag>count1为偶）</Tag> : <Tag>count1为奇数</Tag>}
                <Button onClick={() => {
                    count1.current += 1;
                    message.info("ref.current改变:" + count1.current)
                }}>ref加1（ref改变，❌组件不会重新渲染）
                </Button>
            </div>

            <Space direction={"vertical"}>
                <h2>使用ref操作DOM</h2>
                <Space>
                    <Input ref={inputRef}/>
                    <Button onClick={() => {
                        inputRef.current.focus();
                    }}>聚焦(ref一般用来操作DOM元素)
                    </Button>
                </Space>

            </Space>
        </div>
    );
}


export default FCom;
//{% endraw %}
