//{% raw %}
import React from 'react';

function Button({onSmash, children}) {
    return (
        // 子组件向父组件发送事件
        <button onClick={onSmash}>
            {children}
        </button>
    );
}

function FCom(props) {
    return (
        <div>
            {/*父组件处理子组件的事件*/}
            <Button onSmash={() => alert('Smashed!')}>Smash</Button>
        </div>
    );
}

export default FCom;
//{% endraw %}
