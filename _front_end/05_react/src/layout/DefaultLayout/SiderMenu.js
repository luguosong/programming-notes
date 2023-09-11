import React from 'react';
import {Menu} from "antd";
import {useNavigate} from "react-router-dom";

function SiderMenu(props) {
    const items = [
        {
            label: "基础知识",
            children: [
                {key: "CreatingAndNestingComponents", label: "组件创建和使用"},
                {key: "ComponentStyles", label: "组件样式"},
                {key: "EventHandling", label: "事件绑定"},
                {key: "Ref", label: "引用"},
                {key: "State", label: "状态"},
                {key: "List", label: "列表渲染"},
                {key: "ConditionalRendering", label: "条件渲染"},
                {key: "Props", label: "属性"},
                {key: "ComponentCommunication", label: "组件通信"},
                {key: "ComponentCommunicationPubSub", label: "组件通信-订阅发布"},
                {key: "ComponentCommunicationContext", label: "组件通信-Context"},
                {key: "Slot", label: "插槽"},
                {key: "LifeCycle", label: "生命周期"},
                {key: "PureComponentDemo", label: "PureComponent"},
            ]
        }
    ]
    const navigate = useNavigate();
    return (
        <div>
            <Menu
                onClick={(item) => navigate(item.key)}
                style={{
                    width: "100%",
                }}
                defaultSelectedKeys={['1']}
                defaultOpenKeys={['sub1']}
                mode="inline"
                items={items}
            />
        </div>
    );
}

export default SiderMenu;
