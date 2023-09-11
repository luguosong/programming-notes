import React, {Component} from 'react';
import {Card, Input} from "antd";

class Child1 extends Component {
    render() {
        return (
            <div>
                <Card title={"子组件1"}>
                    <Input addonBefore={"子组件1修改内容"} onChange={(e) => {
                        // ⭐子组件向父组件发送事件，将内容传递给父组件
                        this.props.onChange(e.target.value)
                    }}></Input>
                </Card>
            </div>
        );
    }
}

class Child2 extends Component {
    render() {
        return (
            <div>
                <Card title={"子组件2"}>
                    接收来自子组件1的内容：{this.props.msg}
                </Card>
            </div>
        );
    }
}

class CCom extends Component {

    state = {
        msg: ""
    }

    render() {
        return (
            <div>
                <Child1 onChange={(value) => {
                    this.setState({
                        msg: value
                    })
                }}></Child1>
                <Child2 msg={this.state.msg}></Child2>
            </div>
        );
    }
}

export default CCom;
