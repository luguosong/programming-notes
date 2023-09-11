import React from 'react';
import {Card} from "antd";
import CCom from "./CCom";
import FCom from "./FCom";

function State(props) {
    return (
        <div>
            <Card title={"函数式组件"}>
                <FCom/>
            </Card>
            <Card title={"类组件"}>
                <CCom/>
            </Card>
        </div>
    );
}

export default State;
