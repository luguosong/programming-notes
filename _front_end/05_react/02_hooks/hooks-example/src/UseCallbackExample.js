import React from 'react';

function UseCallbackExample(props) {
    const [count, setCount] = React.useState(0)

    /*
    * 使用useCallback让组件在刷新时，只要第二个参数数组中的值变化，函数才会更新
    *
    * */
    const handleAdd = React.useCallback(() => {
        console.log("useCallback执行，")
        setCount(count + 1)
    },[count])

    return (
        <div>
            <h1>useCallback-记忆函数</h1>
            <button onClick={handleAdd}>add</button>
            {count}
        </div>
    );
}

export default UseCallbackExample;
