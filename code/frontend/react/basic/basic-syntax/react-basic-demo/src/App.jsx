import {Button} from "antd";
import {useState} from "react";

const App = () => {

    const [count, setCount] = useState(0)

    return (
        <div>
            <text>{count}</text>
            <Button
                onClick={() => {
                    setCount(count + 1)
                }}
            >测试</Button>
        </div>
    );
};

export default App;
