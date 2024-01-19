//{% raw %}
import React, {useRef} from 'react';
import {Button} from "antd";

function FCom(props) {
    const itemRefs = useRef();

    function getMap() {
        if (!itemRefs.current) {
            itemRefs.current = new Map();
        }
        return itemRefs.current;
    }

    return (
        <div>
            <ul>
                {list.map(item => {
                    return (
                        <li>
                            {item.value}
                            {/*使用回调函数设置ref*/}
                            <input ref={node => {
                                const map = getMap();
                                if (node) {
                                    map.set(item.id, node);
                                } else {
                                    map.delete(item.id);
                                }
                            }}/>
                        </li>
                    )
                })}
            </ul>
            <Button onClick={() => {
                getMap().get(1).focus();
            }}>姓名获取焦点</Button>
            <Button onClick={() => {
                getMap().get(2).focus();
            }}>年龄获取焦点</Button>
            <Button onClick={() => {
                getMap().get(3).focus();
            }}>身高获取焦点</Button>
        </div>
    );
}

/*
* 一个不确定数量，且随时会发生改变的列表
* */
const list = [
    {id: 1, value: "姓名"},
    {id: 2, value: "年龄"},
    {id: 3, value: "身高"}
]

export default FCom;
//{% endraw %}
