//{% raw %}
import React from 'react';
import FCom from "./FCom";
import {Card} from "antd";

function SynchronizingWithEffects(props) {
    return (
        <div>
            <Card title={"函数式组件"}>
                <FCom/>
            </Card>
        </div>
    );
}

export default SynchronizingWithEffects;
//{% endraw %}
