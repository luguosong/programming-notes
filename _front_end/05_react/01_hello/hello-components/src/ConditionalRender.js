import React, {Component} from 'react';

class ConditionalRender extends Component {
    state={
        isLogin:true
    }

    render() {
        return (
            <div>
                <h1>条件渲染</h1>
                {/*通过三元表达式实现条件渲染*/}
                {this.state.isLogin?<div>已登录</div>:<div>未登录</div>}
                {/*通过&&实现条件渲染*/}
                {this.state.isLogin&&<div>已登录</div>}
            </div>
        );
    }
}

export default ConditionalRender;
