import React, {Component} from 'react';

/*
* 父组件
* */
class ParentChildCommunication extends Component {

    state = {
        show: true
    }

    render() {
        return (
            <div>
                <h1>父子通讯</h1>
                <Child1 isShow={this.state.show} event={() => {
                    this.setState({
                        show: !this.state.show
                    })
                }}/>
                {/*父组件将值传给子组件2*/}
                <Child2 isShow={this.state.show}/>
            </div>
        );
    }


}

/*
* 子组件通过事件控制父组件的状态
* */
class Child1 extends Component {

    render() {
        return (
            <div>
                <h2>子组件1</h2>
                子组件1控制父组件的状态：
                <button onClick={() => {
                    this.props.event()
                }}>{this.props.isShow ? "隐藏" : "显示"}</button>
            </div>
        );
    }
}

/*
* 受控组件
*
* 父组件通过属性控制子组件的状态
* */
class Child2 extends Component {
    myInput = React.createRef();

    render() {
        console.log(this.props.isShow)
        return (
            <div>
                <h2>子组件2</h2>
                子组件2作为一个受控组件，父组件通过属性控制其状态：
                {this.props.isShow && <span>父组件状态控制的内容</span>}
            </div>
        );
    }
}

export default ParentChildCommunication;
