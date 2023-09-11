import React, { useState } from 'react';
import { Card, Input } from "antd";
import PropTypes from "prop-types";

// 子组件
function FChild({ name }) {
    return (
        <div>
            <Card title={"子组件"}>
                子组件接收到来自父组件的属性：{name}
            </Card>
        </div>
    );
}

FChild.propTypes = {
    // 属性验证，name必须是字符串
    name: PropTypes.string
};

FChild.defaultProps = {
    name: "默认属性"
};

// 父组件
function FCom() {
    const [name, setName] = useState(undefined);

    return (
        <div>
            <Input addonBefore={"父组件对属性进行改变："} onChange={(e) => {
                setName(e.target.value);
            }} />
            <FChild name={name} />
        </div>
    );
}

export default FCom;
