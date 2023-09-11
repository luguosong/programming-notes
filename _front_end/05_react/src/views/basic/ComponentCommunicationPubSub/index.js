import React, {Component} from 'react';
import {Card} from "antd";
import CCom from "./CCom";

class ComponentCommunicationPubSub extends Component {
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

export default ComponentCommunicationPubSub;
