import React, {Component} from 'react';
import {Button, Card} from "antd";

// 创建组件
class MyButton extends Component {
    render() {
        return (
            <Button>子组件按钮</Button>
        );
    }
}

// 使用组件
class CCom extends Component {
    render() {
        return (
            <Card title="父组件">
                <MyButton/>
            </Card>
        );
    }
}

export default CCom;
