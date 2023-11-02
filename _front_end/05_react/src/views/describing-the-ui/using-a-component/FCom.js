import React from 'react';

// 组件定义
function Profile() {
    return (
        <img
            src="/images/MK3eW3Am.jpg"
            alt="Katherine Johnson"
        />
    );
}

// 使用组件
function FCom(props) {
    return (
        <section>
            <h1>了不起的科学家</h1>
            <Profile/>
            <Profile/>
            <Profile/>
        </section>
    );
}

export default FCom;
