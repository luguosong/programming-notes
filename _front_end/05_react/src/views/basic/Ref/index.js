import React from 'react';
import {Card} from "antd";
import CCom from "./CCom";
import FCom from "./FCom";

function Ref(props) {
    return (
        <div>
            <Card title={"函数组件"}>
                <FCom/>
            </Card>
            <Card title={"类组件"}>
                <CCom/>
            </Card>
        </div>
    );
}

export default Ref;
