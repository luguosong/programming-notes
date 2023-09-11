import React, {Component} from 'react';
import {Button, Input} from "antd";
import Compact from "antd/es/space/Compact";

class CCom extends Component {
    myRef = React.createRef();

    render() {
        return (
            <div>
                <Compact>
                    <Input ref={this.myRef}/>
                    <Button onClick={() => {
                        // 通过ref获取input的值
                        alert(this.myRef.current.input.value)
                    }}>获取input内容
                    </Button>
                </Compact>
            </div>
        );
    }
}

export default CCom;
