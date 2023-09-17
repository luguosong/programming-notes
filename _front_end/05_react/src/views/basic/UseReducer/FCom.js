import React, {useReducer} from 'react';
import {Button, Card} from "antd";

const reducer = (state, action) => {
    switch (action.type) {
        case "add":
            return {count: state.count + 1}
        case "sub":
            return {count: state.count - 1}
        default:
            return state
    }
}

const initialState = {
    count: 100
};

const GlobalContext = React.createContext();

function FCom(props) {

    const [state, dispatch] = useReducer(reducer, initialState)

    return (
        <GlobalContext.Provider value={{state, dispatch}}>
            <div>
                <Child1/>
                <Child2/>
            </div>
        </GlobalContext.Provider>
    );
}

function Child1() {
    const {dispatch} = React.useContext(GlobalContext)
    return (
        <Card title={"子组件1"}>
            <Button onClick={() => dispatch({type: "add"})}>+</Button>
            <Button onClick={() => dispatch({type: "sub"})}>-</Button>
        </Card>
    );
}

function Child2() {
    const {state} = React.useContext(GlobalContext)
    return (
        <Card title={"子组件2"}>
            {state.count}
        </Card>
    );
}

export default FCom;
