import React from 'react';

function UseMemoExample(props) {

    const [count, setCount] = React.useState(0)

    /*
    * 类似于计算属性
    * */
    const handleClick = React.useMemo(() => {
        console.log("useMemo执行，")
        return count + 1
    }, [count])

    return (
        <div>
            <h1>useMemo</h1>
            <button onClick={() => {
                setCount(handleClick)
            }}>add
            </button>
            {count}
        </div>
    );
}

export default UseMemoExample;
