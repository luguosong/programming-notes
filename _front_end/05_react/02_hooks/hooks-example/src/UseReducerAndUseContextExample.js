import React from 'react';

const GlobalContext = React.createContext(undefined);

const prevState = (prevState, action) => {
    switch (action.type) {
        case "add":
            return {count: prevState.count + 1}
        case "sub":
            return {count: prevState.count - 1}
        default:
            return prevState
    }
}

const initState = {
    count: 0
}


/*
* useReducer+useContext结合使用
* */
function UseReducerAndUseContextExample(props) {

    const [state, dispatch] = React.useReducer(prevState, initState);

    return (
        <GlobalContext.Provider value={
            {
                state,
                dispatch
            }
        }>
            <div>
                <h1>useReducer和useContext结合使用</h1>
                <Child1/>
                <Child2/>
            </div>
        </GlobalContext.Provider>
    );
}

function Child1() {

    const {dispatch} = React.useContext(GlobalContext)

    return (
        <div>
            <button onClick={() => {
                dispatch({type: "add"})
            }}>add
            </button>
        </div>
    );
}

function Child2() {

    const {state} = React.useContext(GlobalContext)

    return (
        <div>
            {state.count}
        </div>
    );
}

export default UseReducerAndUseContextExample;
