import {Button, Card, Space} from "antd";
import Child1 from "./Child1";
import Child2 from "./Child2";
import {CountContext} from "./CountContext";
import {myTimer} from "./Timer";

const MobxReactDemo = () => {

    return (
        <Card

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

            <div>
                <Button
                    onClick={() => {
                        myTimer.count += 1
                    }}
                >❗错误做法：不通过action，直接修改可观测属性（控制台会报错）</Button>

            </div>

            <CountContext value={myTimer}>
                <Child1/>
                <Child2/>
            </CountContext>
        </Card>
    );
};

export default MobxReactDemo;
