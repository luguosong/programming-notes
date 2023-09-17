import React, {Component} from 'react';
import {Card} from "antd";
import CCom from "./CCom";
import FCom from "./FCom";

class ComponentCommunicationContext extends Component {
    render() {
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
}

export default ComponentCommunicationContext;
