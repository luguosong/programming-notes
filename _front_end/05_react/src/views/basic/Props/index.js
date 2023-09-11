import React, {Component} from 'react';
import CCom from "./CCom";
import {Card} from "antd";
import FCom from "./FCom";

class Props extends Component {
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

export default Props;
