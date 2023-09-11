import React, {Component} from 'react';
import {Alert, Card, Input} from "antd";

class Child extends Component {
    render() {
        return (
            <div>
                <Card title={"子组件：使用this.props.children获取插槽内容"}>
                    {this.props.children[0]}
                    {this.props.children[1]}
                    {this.props.children[2]}
                </Card>
            </div>
        );
    }
}

class CCom extends Component {

    state = {
        msg: "初始状态"
    }

    render() {
        return (
            <Card title={"父组件"}>
                <div>
                    <Alert message={"父组件中的状态：" + this.state.msg}/>
                </div>
                <Child>
                    <Alert message="插入内容1" type="success"/>
                    <Alert message="插入内容2" type="success"/>
                    <Input addonBefore={"插槽中的内容可以直接修改父组件中的状态，减少父子通讯："}
                           value={this.state.msg}
                           onChange={(e) => {
                               this.setState({
                                   msg: e.target.value
                               })
                           }}></Input>
                </Child>
            </Card>
        );
    }
}

export default CCom;
