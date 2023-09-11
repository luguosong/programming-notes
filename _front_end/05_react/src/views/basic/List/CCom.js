import React, {Component} from 'react';
import {Alert} from "antd";

class CCom extends Component {

    state = {
        list: [{id: 1, name: "张三"},
            {id: 2, name: "李四"},
            {id: 3, name: "王五"},]
    }

    render() {
        return (
            <div>
                {
                    this.state.list.map(item =>
                        <Alert key={item.id} message={item.name}/>)
                }
            </div>
        );
    }
}

export default CCom;
