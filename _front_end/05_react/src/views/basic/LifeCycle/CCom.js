import React, {Component} from 'react';
import {Badge, Button, Card, notification, Tag} from "antd";

function message(type, msg, description) {
    notification[type]({
        message: msg,
        description: description,
        duration: 60,
        style: {
            margin: "2px",
            width: 600,
            padding: "5px"
        }
    })
}

class Child extends Component {

    state = {
        count: 0
    }

    /*
    * 初始化阶段
    * */
    constructor() {
        super();
        message("success", "构造函数执行-constructor", "组件创建时调用，一般用于初始化state")
    }

    // componentWillMount() {
    //     message("success", "初始化阶段-componentWillMount", "组件将要挂载（已弃用）。")
    // }

    componentDidMount() {
        message("success", "初始化阶段-componentDidMount", "组件已经挂载。")
    }

    /*
    * 更新阶段
    * */
    // componentWillReceiveProps(nextProps) {
    //     message("info", "更新阶段-componentWillReceiveProps",
    //         <div>
    //             <p>组件属性发生变化,可以在组件渲染前对属性进行定制。</p>
    //             <Card>新属性为:{nextProps.count}</Card>
    //         </div>)
    // }

    // 更新和初始化都会执行
    static getDerivedStateFromProps(nextProps, prevState) {
        message("warning", "getDerivedStateFromProps", <div>
            <p>getDerivedStateFromProps：组件属性和状态发生变化和初始化都会执行</p>
            <p>getDerivedStateFromProps不能与老的生命周期共存</p>
            <Card>
                <div>新属性为：{nextProps.count}</div>
                <div>新状态为：{prevState.count}</div>
            </Card></div>)
        // 更新状态
        return {
            //这里可以更新状态
        }
    }

    shouldComponentUpdate(nextProps, nextState) {
        // 当属性发生变化，直接更新
        if (nextProps.count !== this.props.count) {
            message("info", "更新阶段-shouldComponentUpdate",
                <div>
                    <p>shouldComponentUpdate：组件是否需要更新DOM。返回true表示需要更新，返回false表示不需要更新。</p>
                    <Card>老属性为：{this.props.count},新属性为：{nextProps.count}</Card>
                </div>)
            return true;
        }
        // 当状态发生变化，为奇数则更新
        const beUpdate = nextState.count % 2 !== 0;
        message(beUpdate ? "info" : "error", beUpdate ? "更新阶段-shouldComponentUpdate" : "更新阶段:组件未更新",
            <div>
                <p>shouldComponentUpdate：组件是否需要更新DOM。返回true表示需要更新，返回false表示不需要更新。</p>
                <Card>老状态为：{this.state.count},新状态为：{nextState.count}</Card>
            </div>)
        return beUpdate;
    }

    // componentWillUpdate() {
    //     message("info", "更新阶段-componentWillUpdate", "组件将要更新（已弃用）。")
    // }

    getSnapshotBeforeUpdate(prevProps, prevState) {
        message("info", "更新阶段-getSnapshotBeforeUpdate",
            <div>
                <p>getSnapshotBeforeUpdate：在更新前获取DOM状态。</p>
                <p>⭐在render之后执行，在Dom更新之前执行</p>
                <Card>老状态为：{prevState.count}，新状态为：{this.state.count}</Card>
            </div>)
        return 100;
    }

    componentDidUpdate(prevProps, prevState,value) {
        message("info", "更新阶段-componentDidUpdate",
            <div>
                <p>组件已经更新</p>
                <p>接收来自getSnapshotBeforeUpdate的返回值：{value}</p>
                <Card>老状态为：{prevState.count}，新状态为：{this.state.count}</Card>
            </div>)
    }

    /*
    * 卸载阶段
    * */
    componentWillUnmount() {
        message("error", "卸载阶段-componentWillUnmount", "组件将要卸载。")
    }


    render() {
        message("warning", "render", "组件正在渲染")
        return (
            <Card title={"子组件"}>

                <div style={{margin: "20px"}}>
                    <Badge count={this.props.count} showZero>
                        <Tag>属性</Tag>
                    </Badge>
                </div>
                <div style={{margin: "20px"}}>
                    <Badge count={this.state.count} showZero>
                        <Tag>状态:会在奇数的时候进行更新，而为偶数时不进行更新</Tag>
                    </Badge>
                </div>


                <Button onClick={() => {
                    this.setState({
                        count: this.state.count + 1
                    })
                }}>更新状态</Button>
            </Card>
        );
    }

}


class CCom extends Component {

    myref = React.createRef();

    state = {
        show: true,
        count: 0
    }

    render() {
        return (
            <div>
                <Button onClick={() => {
                    this.setState({
                        show: !this.state.show
                    })
                }}>{this.state.show ? "删除" : "创建"}</Button>
                <Button onClick={() => {
                    this.setState({
                        count: this.state.count + 1
                    })
                }}>更新属性</Button>
                <Button type={"text"} onClick={() => {
                    notification.destroy()
                }} danger>清除消息</Button>


                {this.state.show && <Child count={this.state.count}/>}
            </div>
        );
    }
}

export default CCom;
