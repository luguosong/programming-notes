import React from 'react';
import {Menu} from "antd";
import {useLocation, useNavigate} from "react-router-dom";

function SiderMenu(props) {
    const items = [
        // {
        //     label: "基础知识",
        //     key: "basic",
        //     children: [
        //         {key: "CreatingAndNestingComponents", label: "组件创建和使用"},
        //         {key: "ComponentStyles", label: "组件样式"},
        //         {key: "EventHandling", label: "事件绑定"},
        //         {key: "Ref", label: "引用"},
        //         {key: "State", label: "状态"},
        //         {key: "List", label: "列表渲染"},
        //         {key: "ConditionalRendering", label: "条件渲染"},
        //         {key: "Props", label: "属性"},
        //         {key: "ComponentCommunication", label: "组件通信"},
        //         {key: "ComponentCommunicationPubSub", label: "组件通信-订阅发布"},
        //         {key: "ComponentCommunicationContext", label: "组件通信-Context"},
        //         {key: "UseReducer", label: "UseReducer"},
        //         {key: "Slot", label: "插槽"},
        //         {key: "LifeCycle", label: "生命周期"},
        //         {key: "PureComponentDemo", label: "PureComponent"},
        //         {key: "UseEffect", label: "UseEffect"},
        //         {key: "UseCallBack", label: "useCallBack-缓存函数"},
        //         {key: "UseMemo", label: "useMemo-计算属性"},
        //         {key: "CustomHooks", label: "自定义hooks"},
        //     ]
        // },
        // {
        //     label: "路由",
        //     key: "router",
        //     children: [
        //         {key: "RouterDemo", label: "路由示例"},
        //     ]
        // },
        {
            label: "描述UI",
            key: "describing-the-ui",
            children: [
                {key: "defining-a-component", label: "定义组件"},
                {key: "using-a-component", label: "使用组件"},
                {key: "passing-props-to-a-component", label: "props"},
                {key: "slot", label: "插槽"},
                {key: "conditional-rendering", label: "条件渲染"},
                {key: "rendering-lists", label: "列表渲染"},
            ]
        },
        {
            label: "添加交互",
            key: "adding-interactivity",
            children: [
                {key: "responding-to-events", label: "响应事件"},
                {key: "state-a-components-memory", label: "State"},
                {key: "state-as-a-snapshot", label: "state如同一张快照"},
                {key: "queueing-a-series-of-state-updates", label: "state更新加入队列"},
                {key: "updating-objects-in-state", label: "state对象更新"},
                {key: "updating-arrays-in-state", label: "state数组更新"},
            ]
        },
        {
            label: "状态管理",
            key: "managing-state",
            children: [
                {key: "reacting-to-input-with-state", label: "State响应输入"},
                {key: "sharing-state-between-components", label: "状态提升"},
                {key: "preserving-and-resetting-state", label: "状态保留和重置"},
                {key: "extracting-state-logic-into-a-reducer", label: "Reducer"},
                {key: "passing-data-deeply-with-context", label: "Context"},
                {key: "scaling-up-with-reducer-and-context", label: "Context+Reducer"},
            ]
        },
        {
            label: "应急方案",
            key: "escape-hatches",
            children: [
                {key: "referencing-values-with-refs", label: "ref"},
                {key: "manage-a-list-of-refs", label: "ref应用于列表"},
                {key: "ref-accessing-another-component", label: "访问另一个组件的DOM节点"},
                {key: "synchronizing-with-effects", label: "Effect"},
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
                    }, "");
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
