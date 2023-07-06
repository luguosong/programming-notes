import React, {Component} from 'react';

class Status extends Component {
    state = {
        show: true,
        count: 1
    }

    render() {
        return (
            <div>
                <h1>状态</h1>

                {/*
                入门案例
                */}
                <h2>入门案例</h2>
                <button onClick={() => {
                    this.setState({
                        show: !this.state.show
                    })
                }}>{this.state.show ? "收藏" : "取消收藏"}</button>

                {/*
                状态更新并不是同步的
                */}
                <h2>异步状态更新</h2>
                {this.state.count}
                <button onClick={this.handleAdd1}>同步环境下状态异步更新</button>
            </div>
        );
    }

    /**
     * 状态更新并不是同步的
     *
     * 执行三次setState，但是只更新了一次
     */
    handleAdd1 = () => {
        this.setState({
            count: this.state.count + 1
        })
        this.setState({
            count: this.state.count + 1
        })
        this.setState({
            count: this.state.count + 1
        })
    }

}

export default Status;
