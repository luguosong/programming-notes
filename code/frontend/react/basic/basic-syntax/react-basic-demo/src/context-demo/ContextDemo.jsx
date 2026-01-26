import {useState} from "react";
import {Button, Card} from "antd";
import Child1 from "./Child1";
import Child2 from "./Child2";
import {CountContext} from "./CountContext";

const ContextDemo = () => {
    const [count, setCount] = useState(1)

    return (
        <Card
            style={{width: 350}}
            title={"根组件"}
        >
            <Button onClick={() => {
                setCount(count + 1)
            }}>Count:{count}</Button>

            <CountContext value={count}>
                <Child1/>
                <Child2/>
            </CountContext>
        </Card>
    );
};

export default ContextDemo;
