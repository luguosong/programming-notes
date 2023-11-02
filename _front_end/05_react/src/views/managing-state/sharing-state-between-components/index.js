//{% raw %}
import React from 'react';
import {Card} from "antd";
import FCom from "./FCom";

function SharingStateBetweenComponents(props) {
    return (
        <div>
            <Card >
                <FCom/>
            </Card>
        </div>
    );
}

export default SharingStateBetweenComponents;
//{% endraw %}
