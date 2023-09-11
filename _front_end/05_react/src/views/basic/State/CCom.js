import React, {Component} from 'react';
import {Button, Input, List} from "antd";
import Compact from "antd/es/space/Compact";

class CCom extends Component {

    // state是组件内部的状态，当state发生变化时，组件会重新渲染
    state = {
        val: "",
        list: []
    }

    render() {
        return (
            <div>
                <Compact>
                    <Input value={this.state.val} onChange={(e) => {
                        // 当输入框的值发生变化时，将输入框的值赋值给state
                        this.setState({
                            val: e.target.value
                        })
                    }}/>
                    <Button onClick={() => {
                        // 当点击按钮时，将输入框的值添加到list中，并清空输入框的值
                        this.setState({
                            list: [...this.state.list, this.state.val],
                            val: ""
                        })
                    }}>add
                    </Button>
                </Compact>
                <List
                    dataSource={this.state.list}
                    renderItem={(item) => <List.Item>{item}</List.Item>}/>
            </div>
        );
    }
}

export default CCom;
