
import {Button, Card} from "antd";

//创建组件
function MyButton() {
    return (
        <div>
            <Button>子组件按钮</Button>
        </div>
    );
}

// 使用组件
export default function FCom() {
    return (
        <Card title="父组件">
            <MyButton/>
        </Card>
    );
}
