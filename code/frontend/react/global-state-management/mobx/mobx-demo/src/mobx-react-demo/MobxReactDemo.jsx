import {Button, Card, Space} from "antd";
import Child1 from "./Child1";
import Child2 from "./Child2";
import {CountContext} from "./CountContext";
import {myTimer} from "./Timer";

const MobxReactDemo = () => {

    return (
        <Card
            style={{width: 350}}
            title={"根组件"}
        >
            <Space>
                <Button onClick={() => {
                    myTimer.increase();
                }}>Add</Button>

                <Button onClick={() => {
                    myTimer.reset();
                }}>
                    reset
                </Button>
            </Space>

            <CountContext value={myTimer}>
                <Child1/>
                <Child2/>
            </CountContext>
        </Card>
    );
};

export default MobxReactDemo;
