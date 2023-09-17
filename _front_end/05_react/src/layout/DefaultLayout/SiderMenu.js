import React from 'react';
import {Menu} from "antd";
import {useLocation, useNavigate} from "react-router-dom";

function SiderMenu(props) {
    const items = [
        {
            label: "基础知识",
            key: "basic",
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
                {key: "UseReducer", label: "UseReducer"},
                {key: "Slot", label: "插槽"},
                {key: "LifeCycle", label: "生命周期"},
                {key: "PureComponentDemo", label: "PureComponent"},
                {key: "UseEffect", label: "UseEffect"},
                {key: "UseCallBack", label: "useCallBack-缓存函数"},
                {key: "UseMemo", label: "useMemo-计算属性"},
                {key: "CustomHooks", label: "自定义hooks"},
            ]
        },
        {
            label: "路由",
            key: "router",
            children: [
                {key: "RouterDemo", label: "路由示例"},
            ]
        }
    ]
    const navigate = useNavigate();
    const location = useLocation();

    // 默认展开选中项
    const selectedKeys = location.pathname.split("/").filter(items => items !== "")

    /*
    * 让只有
    * */
    const onOpenChange = (keys) => {
        if (keys.length > 1)
            keys.splice(0, 1)
    }

    return (
        <div>
            <Menu
                onClick={(item) => {
                    const path = item.keyPath.reduceRight((pre, cur) => {
                        return pre + "/" + cur
                    },"");
                    console.log(path)
                    navigate(path)
                }}
                defaultSelectedKeys={selectedKeys}
                defaultOpenKeys={selectedKeys}
                onOpenChange={onOpenChange}
                mode="inline"
                items={items}
            />
        </div>
    );
}

export default SiderMenu;
