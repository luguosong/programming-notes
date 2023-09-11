import React, {Component} from 'react';
import {Card} from "antd";
import CCom from "./CCom";

class Index extends Component {
    render() {
        return (
            <div>
                <Card>
                    <CCom/>
                </Card>
            </div>
        );
    }
}

export default Index;
