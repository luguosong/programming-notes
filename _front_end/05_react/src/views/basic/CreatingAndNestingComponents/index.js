import React from 'react';
import CCom from "./CCom";
import FCom from "./FCom";
import {Card} from "antd";

function CreatingAndNestingComponents(props) {
    return (
        <div>
            <Card title="函数式组件">
                <FCom/>
            </Card>
            <hr/>
            <Card title="类组件">
                <CCom/>
            </Card>

        </div>
    );
}

export default CreatingAndNestingComponents;
