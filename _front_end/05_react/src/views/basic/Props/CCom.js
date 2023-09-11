import React, {Component} from 'react';
import {Card, Input} from "antd";
import PropTypes from "prop-types";

// 子组件
class Child extends Component {

    // 属性验证
    static propTypes = {
        // 属性验证，name必须是字符串
        name: PropTypes.string
    };

    // 默认值
    static defaultProps = {
        name: "默认属性"
    }

    render() {
        return (
            <div>
                <Card title={"子组件"}>
                    子组件接收到来自父组件的属性：{this.props.name}
                </Card>
            </div>
        );
    }
}


// 父组件
class CCom extends Component {

    state = {
        name: undefined
    }

    render() {
        return (
            <div>
                <Input addonBefore={"父组件对属性进行改变："} onChange={(e) => {
                    this.setState({
                        name: e.target.value
                    })
                }}/>
                <Child name={this.state.name}></Child>
            </div>
        );
    }
}

export default CCom;
