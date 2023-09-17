import React, {Component, createContext} from 'react';
import {Card, Input} from "antd";

const context = createContext();

const {Provider, Consumer} = context;

// {% raw %}

class Child1 extends Component {
    render() {
        return (
            <Consumer>
                {
                    (value) =>
                        <div>
                            <Card title={"子组件1"}>
                                <Input addonBefore={"子组件1修改内容"} onChange={(e) => {
                                    value.setMsg(e.target.value)
                                }}></Input>
                            </Card>
                        </div>
                }
            </Consumer>

        );
    }
}

class Child2 extends Component {
    render() {
        return (
            <Consumer>
                {
                    (value) =>
                        <div>
                            <Card title={"子组件2"}>
                                接收来自子组件1的内容：{value.msg}
                            </Card>
                        </div>
                }
            </Consumer>


        );
    }
}

class Child3 extends Component {
    render() {
        return (
            <div>
                <Card title={"子组件2"}>
                    接收来自子组件1的内容：{this.context.msg}
                </Card>
            </div>
        );
    }
}

Child3.contextType = context

class CCom extends Component {

    //只有通过状态的改变，才会刷新组件
    state = {
        msg: ""
    }

    render() {
        return (
            <Provider value={{
                msg: this.state.msg,
                setMsg: (value) => {
                    this.setState({
                        msg: value
                    })
                }
            }}>
                <div>
                    <Child1></Child1>
                    <Child2></Child2>
                    <Child3></Child3>
                </div>
            </Provider>
        );
    }
}

export default CCom;

//{% endraw %}
