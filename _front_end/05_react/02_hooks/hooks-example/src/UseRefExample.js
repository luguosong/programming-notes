import React from 'react';

function UseRefExample(props) {

    const myText = React.useRef(null)

    return (
        <div>
            <h1>useRef</h1>
            <input type="text" ref={myText}/>

            <button onClick={() => {
                alert(myText.current.value)
            }}>获取input的值
            </button>
        </div>
    );
}

export default UseRefExample;
