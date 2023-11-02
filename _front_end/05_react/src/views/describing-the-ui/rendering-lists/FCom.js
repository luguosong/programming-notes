//{% raw %}
import React from 'react';

/*
* 使用map从数组种渲染数据
* */
function List1({list}) {
    const listItems = list.map(person =>
        <li key={person.id}>{person.name}---{person.profession}</li>
    );

    return <ul>{listItems}</ul>
}

/*
* 对数组项进行过滤
* */
function List2({list}) {
    const chemists = list.filter(person =>
        person.profession === '化学家'
    );

    const listItems = chemists.map(person =>
        <li key={person.id}>{person.name}---{person.profession}</li>
    );

    return <ul>{listItems}</ul>
}

function FCom(props) {

    const people = [
        {
            id: 0,
            name: '凯瑟琳·约翰逊',
            profession: '数学家',
        },
        {
            id: 1,
            name: '马里奥·莫利纳',
            profession: '化学家',
        },
        {
            id: 2,
            name: '穆罕默德·阿卜杜勒·萨拉姆',
            profession: '物理学家',
        },
        {
            id: 3,
            name: '珀西·莱温·朱利亚',
            profession: '化学家',
        },
        {
            id: 4,
            name: '苏布拉马尼扬·钱德拉塞卡',
            profession: '天体物理学家',
        },
    ];


    return (
        <div>
            <h1>从数组中渲染数据</h1>
            <List1 list={people}/>
            <h1>对数组项进行过滤 </h1>
            <List2 list={people}/>
        </div>
    );
}

export default FCom;
//{% endraw %}
