import React, {Component} from 'react';
import {Alert, Button, Card} from "antd";

class CCom extends Component {

    state = {
        show: true
    }

    render() {
        return (
            <Card>
                <Button onClick={() => {
                    this.setState({
                        show: !this.state.show
                    })
                }}>点击{this.state.show ? "隐藏" : "显示"}</Button>
                {/*使用三元运算符*/}
                {this.state.show ? <Alert message={"使用三元运算符控制显示"}/> : null}
                {/*使用逻辑与运算符*/}
                {this.state.show && <Alert message={"使用逻辑与运算符&&控制显示"}/>}
            </Card>
        );
    }
}

export default CCom;
