import React from 'react';
import {Card} from "antd";
import CCom from "./CCom";
import CssModuleCCom from "./CssModuleCCom";

function ComponentStyles(props) {
    return (
        <div>
            <Card title={"传统方式"}>
                <CCom/>
            </Card>
            <Card title={"使用CSS Module防止污染"}>
                <CssModuleCCom/>
            </Card>
        </div>
    );
}

export default ComponentStyles;
