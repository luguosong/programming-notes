//{% raw %}
import React from 'react';

/*
* 方式一：采用if判断语句
* */
function Item1({ name, isPacked }) {
    if (isPacked) {
        return <li className="item">{name} ✔</li>;
    }
    return <li className="item">{name}</li>;
}

/*
* 方式二：采用三元运算符
* */
function Item2({ name, isPacked }) {
    return (
        <li className="item">
            {isPacked ? name + ' ✔' : name}
        </li>
    )
}

/*
* 方式三：采用与运算符（&&）
* */
function Item3({ name, isPacked }) {
    return (
        <li className="item">
            {name} {isPacked && '✔'}
        </li>
    )
}

function FCom(props) {
    return (
        <div>
            <section>
                <h1>采用if判断语句</h1>
                <ul>
                    <Item1 isPacked={true} name={"宇航服"}/>
                    <Item1 isPacked={false} name={"带金箔的头盔"}/>
                </ul>
            </section>
            <section>
                <h1>采用三元运算符</h1>
                <ul>
                    <Item2 isPacked={true} name={"宇航服"}/>
                    <Item2 isPacked={false} name={"带金箔的头盔"}/>
                </ul>
            </section>
            <section>
                <h1>采用与运算符（&&）</h1>
                <ul>
                    <Item3 isPacked={true} name={"宇航服"}/>
                    <Item3 isPacked={false} name={"带金箔的头盔"}/>
                </ul>
            </section>
        </div>
    );
}

export default FCom;
//{% endraw %}
