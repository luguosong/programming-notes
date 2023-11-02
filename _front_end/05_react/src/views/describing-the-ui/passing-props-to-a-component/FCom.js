// {% raw %}
import React, {useEffect, useState} from 'react';

// ⭐子组件通过对象的解构赋值获取父组件传递的props
function Clock({ color, time }) {
    return (
        <h1 style={{ color: color }}>
            {time}
        </h1>
    );
}

function FCom(props) {
    const time = useTime();
    const [color, setColor] = useState('lightcoral');
    return (
        <div>
            <p>
                选择一个颜色:{' '}
                <select value={color} onChange={e => setColor(e.target.value)}>
                    <option value="lightcoral">浅珊瑚色</option>
                    <option value="midnightblue">午夜蓝</option>
                    <option value="rebeccapurple">丽贝卡紫</option>
                </select>
            </p>

            {/*设置属性*/}
            <Clock color={color} time={time.toLocaleTimeString()} />
        </div>
    );
}

function useTime() {
    const [time, setTime] = useState(() => new Date());
    useEffect(() => {
        // 设置定时器
        const id = setInterval(() => {
            setTime(new Date());
        }, 1000);
        // 清除定时器
        return () => clearInterval(id);
    }, []);
    return time;
}

export default FCom;
//{% endraw %}
