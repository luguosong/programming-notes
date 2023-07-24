import React from "react";

export default function UseEffectExample() {

    const [show, setShow] = React.useState(true)
    const [buttonName, setButtonName] = React.useState('')

    /**
     * 参数1：回调函数
     * 参数2：依赖项数组，当数组中的值发生变化时，才会执行回调函数
     *
     * useEffect 可以模拟 componentDidMount，componentDidUpdate 和 componentWillUnmount 这三个生命周期方法。
     */


    /*
    * 组件创建
    *
    * useEffect模拟componentDidMount
    *
    * 第二个参数传递一个空数组，表示只执行一次
    * */
    React.useEffect(() => {
        console.log("组件加载")
        /*
        * 模拟数据请求
        * */
        if (show) {
            setButtonName('隐藏')
        } else {
            setButtonName('显示')
        }
    }, [])


    /*
    * 组件更新
    *
    * useEffect模拟componentDidUpdate
    * */
    React.useEffect(() => {
        console.log("组件更新")
        if (show) {
            setButtonName('隐藏')
        } else {
            setButtonName('显示')
        }
    }, [show])

    /*
    * 组件销毁
    *
    * useEffect模拟componentWillUnmount
    * */
    React.useEffect(() => {
        /*
        * 在useEffect中返回一个函数，将会在组件销毁时执行这个函数就是componentWillUnmount
        *
        * 注意，此时第二个参数为空，否则每次更新都会执行这个函数
        * */
        return () => {
            console.log('组件销毁')
        }

    }, [])

    return (
        <div>
            <h1>useEffect-副作用</h1>
            <button onClick={() => {
                setShow(!show)
            }}>{buttonName}</button>
        </div>
    )
}
