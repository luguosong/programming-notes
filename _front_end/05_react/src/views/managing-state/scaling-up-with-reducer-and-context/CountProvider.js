// ************** 将Reducer和Context整合在一起 **************

import {createContext, useReducer} from "react";

export const CountContext = createContext(0);
export const CountDispatchContext = createContext(null);

export function CountProvider({children}) {
    const [count, dispatch] = useReducer(countReducer, 1);

    return (
        <CountContext.Provider value={count}>
            <CountDispatchContext.Provider value={dispatch}>
                {children}
            </CountDispatchContext.Provider>
        </CountContext.Provider>
    )
}


// reducer函数
function countReducer(count, action) {
    switch (action.type) {
        case "add":
            return count + 1;
        case "minus":
            return count - 1;
        default:
            return count;
    }
}
