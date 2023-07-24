import React from 'react';

const GlobalContext = React.createContext(undefined);

export default function UseContextExample(props) {


    const [count, setCount] = React.useState(0)

    return (
        <GlobalContext.Provider value={{
            count: count,
            changeCount: () => {
                setCount(count + 1)
            }
        }}>
            <div>
                < div>
                    < h1> useContext < /h1>
                    <Child1/>
                    <Child2/>
                </div>
            </div>
        </GlobalContext.Provider>
    );
}


function Child1(props) {

    const context = React.useContext(GlobalContext)

    return (
        <div>{context.count}</div>
    );
}

function Child2(props) {

    const context = React.useContext(GlobalContext)

    return (
        <div>
            <button onClick={() => {
                context.changeCount(context.count + 1)
            }}>add
            </button>
        </div>
    );
}
