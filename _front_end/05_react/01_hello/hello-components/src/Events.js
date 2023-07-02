import React, {Component} from 'react';

/*
* 事件
* */
class Events extends Component {
    render() {
        return (
            <div>
                <h1>事件绑定</h1>
                <button onClick={() => {
                    alert("内部定义处理函数")
                }}>内部定义处理函数
                </button>
                <button onClick={this.handleClick1}>外部定义处理函数</button>
                <button onClick={() => this.handleClick2(1)}>传参的情况(推荐使用这种方式👍)</button>

            </div>
        );
    }

    handleClick1 = () => {
        alert("外部定义处理函数")
    }

    handleClick2 = (num) => {
        alert(num)
    }
}

export default Events;
