import React, {Component} from 'react';
import FCom from "./FCom";
import {Card} from "antd";

class CustomHooks extends Component {
    render() {
        return (
            <div>
                <Card>
                    <FCom/>
                </Card>
            </div>
        );
    }
}

export default CustomHooks;
