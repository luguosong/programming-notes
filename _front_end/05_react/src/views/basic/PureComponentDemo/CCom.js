import React, {Component, PureComponent} from 'react';
import {Card, message} from "antd";


class Child1 extends Component {
    state = {
        name: "张三"
    }

    render() {
        message.info("组件正在渲染")
        return (
            <Card title={"继承自Component的组件"}>
                <h1>姓名：{this.state.name}</h1>
                <button onClick={() => {
                    this.setState({name: "李四"})
                }}>修改姓名，组件会反复渲染
                </button>
            </Card>
        );
    }
}

class Child2 extends PureComponent {
    state = {
        name: "张三"
    }

    render() {
        message.info("组件正在渲染")
        return (
            <Card title={"继承自PureComponent的组件"}>
                <h1>姓名：{this.state.name}</h1>
                <button onClick={() => {
                    this.setState({name: "李四"})
                }}>修改姓名，当状态没有变化时，组件不会渲染
                </button>
            </Card>
        );
    }
}

class CCom extends Component {
    render() {
        return (
            <div>
                <Child1/>
                <Child2/>
            </div>
        );
    }
}

export default CCom;
