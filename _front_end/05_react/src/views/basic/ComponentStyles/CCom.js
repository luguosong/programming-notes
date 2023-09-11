import React, {Component} from 'react';
import "./CCom.css"

class CCom extends Component {

    render() {
        // style对象
        const green = {
            color: "green"
        }
        return (
            <div>
                <div style={{color: "red"}}>直接在style中定义对象-红色</div>
                <div style={green}>外部定义style对象变量-绿色</div>
                <div className={"blue"}>使用外部样式：蓝色</div>
            </div>
        );
    }
}

export default CCom;
