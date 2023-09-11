import React, {Component} from 'react';
import {Card, Input} from "antd";

// 创建一个简单的发布订阅管理器
const eventManager = {
    events: {}, // 存储事件及其回调函数的对象

    // 订阅事件
    subscribe: function (event, callback) {
        if (!this.events[event]) {
            this.events[event] = [];
        }
        this.events[event].push(callback);
    },

    // 发布事件
    publish: function (event, data) {
        if (this.events[event]) {
            this.events[event].forEach(callback => callback(data));
        }
    },
};

// 发布者
class Child1 extends Component {
    handleChange = (e) => {
        // 子组件1修改内容时触发发布事件
        eventManager.publish('child1InputChange', e.target.value);
    }

    render() {
        return (
            <div>
                <Card title={"子组件1-发布者"}>
                    <Input addonBefore={"子组件1修改内容"} onChange={this.handleChange}></Input>
                </Card>
            </div>
        );
    }
}

// 订阅者1
class Child2 extends Component {
    constructor(props) {
        super(props);

        this.state = {
            msg: "",
        };

        // 订阅事件，当子组件1发布事件时更新状态
        eventManager.subscribe('child1InputChange', this.handleInputChange);
    }

    handleInputChange = (value) => {
        this.setState({
            msg: value,
        });
    };

    render() {
        return (
            <div>
                <Card title={"子组件2-订阅者"}>
                    接收来自子组件1的内容：{this.state.msg}
                </Card>
            </div>
        );
    }
}

//订阅者2
class Child3 extends Component {
    constructor(props) {
        super(props);

        this.state = {
            msg: "",
        };

        // 订阅事件，当子组件1发布事件时更新状态
        eventManager.subscribe('child1InputChange', this.handleInputChange);
    }

    handleInputChange = (value) => {
        this.setState({
            msg: value,
        });
    };

    render() {
        return (
            <div>
                <Card title={"子组件3-订阅者"}>
                    接收来自子组件1的内容：{this.state.msg}
                </Card>
            </div>
        );
    }
}

class CCom extends Component {
    render() {
        return (
            <div>
                <Child1/>
                <Child2/>
                <Child3/>
            </div>
        );
    }
}

export default CCom;
