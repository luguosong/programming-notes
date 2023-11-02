import React from 'react';
import {Card} from "antd";
import CCom from "./CCom";

function Slot(props) {
    return (
        <div>
            <Card title={"函数式组件"}>
                <CCom/>
            </Card>
        </div>
    );
}

export default Slot;
