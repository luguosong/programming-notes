import React from 'react';

// 处理函数
const render = (prevState, action) => {
    switch (action.type) {
        case "add":
            return {count: prevState.count + 1}
        case "sub":
            return {count: prevState.count - 1}
        default:
            return prevState
    }
}

// 初始状态
const intialState = {
    count: 0
}

function UseReducerExample(props) {

    /*
    * 定义Reducer
    * 参数一：处理函数
    * 参数二：初始状态
    * */
    const [state, dispatch] = React.useReducer(render, intialState)

    return (
        <div>
            <h1>useReducer</h1>
            <button onClick={() => {
                dispatch({type: "add"})
            }}>加
            </button>
            <button onClick={() => {
                dispatch({type: "sub"})
            }}>减
            </button>
            {state.count}

        </div>
    );
}

export default UseReducerExample;
