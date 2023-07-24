import React, {Component} from 'react';

/*
* 生命周期
* */
class LifeCycle extends Component {

    /*
    * **********初始化阶段**********
    * */

    /*
    * ❌已弃用
    * */
    componentWillMount() {
        console.log("componentWillMount:组件将要挂载,已弃用🗑️")
    }

    /**
     * 构造，组件创建时执行
     */
    constructor() {
        super();
        console.log("constructor:组件正在构造")
    }

    state = {
        count: 0
    }

    render() {
        console.log("render:组件正在挂载")
        return (
            <div>
                <h1>生命周期</h1>
                {this.state.count}

                <button onClick={() => {
                    this.setState({
                        count: this.state.count + 1
                    })
                }}>状态更新
                </button>
            </div>
        );
    }

    /**
     * 组件挂载完成
     */
    componentDidMount() {
        console.log("componentDidMount:组件已经挂载")
    }


    /*
    * **********更新阶段**********
    * */

    /**
     * 控制组件是否更新，返回true表示更新，返回false表示不更新
     * @param nextProps
     * @param nextState
     * @param nextContext
     * @returns {boolean}
     */
    shouldComponentUpdate(nextProps, nextState, nextContext) {
        console.log("shouldComponentUpdate:组件是否需要更新")
        return true;
    }

    /*
    * 这当中不能使用this.setState()方法，否则会造成死循环
    *
    * ❌已弃用
    * */
    componentWillUpdate(nextProps, nextState, nextContext) {
        console.log("componentWillUpdate:组件将要更新,已弃用🗑️")
    }


    /*
    * ♻️执行render()方法
    * */


    /**
     * render之后，dom渲染之前执行
     *
     * ✅替代componentWillUpdate()方法
     *
     * @param prevProps 上一次的props
     * @param prevState 上一次的state
     * @returns {null} 返回值会作为componentDidUpdate()方法的第三个参数
     */
    getSnapshotBeforeUpdate(prevProps, prevState) {
        console.log("getSnapshotBeforeUpdate:组件将要更新")
        return null;
    }

    /**
     * 组件更新完成
     * @param prevProps 上一次的props
     * @param prevState 上一次的state
     * @param snapshot getSnapshotBeforeUpdate()方法的返回值
     */
    componentDidUpdate(prevProps, prevState, snapshot) {
        console.log("componentDidUpdate:组件已经更新")
    }


    /**
     * 父组件更新时，会促发子组件的componentWillReceiveProps()方法
     *
     * ❌已弃用
     *
     * @param nextProps 下一次的props
     * @param nextContext 下一次的context
     */
    componentWillReceiveProps(nextProps, nextContext) {
        console.log("componentWillReceiveProps:组件将要接收新的props,已弃用🗑️")
    }

    /**
     * 初始化和状态更新都会执行
     *
     * ✅取代componentWillReceiveProps()方法
     *
     * @param nextProps 表示组件将要接收的新的props
     * @param nextState 表示组件将要接收的新的state
     * @returns {{count}} 返回一个对象，表示将要更新的状态
     */
    static getDerivedStateFromProps(nextProps, nextState) {
        console.log("getDerivedStateFromProps:组件将要接收新的props，nextProps-->", nextProps, "prevState-->", nextState)

        return {
            /*
            * 返回一个对象，表示将要更新的状态
            * */
            count: nextState.count
        }
    }


    /*
    * **********卸载阶段**********
    **/

    /**
     * 组件卸载之前执行
     */
    componentWillUnmount() {
        console.log("componentWillUnmount:组件将要卸载")
    }

}

export default LifeCycle;
