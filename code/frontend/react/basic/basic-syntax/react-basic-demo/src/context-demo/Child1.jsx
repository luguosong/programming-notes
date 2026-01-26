import {Card} from "antd";
import GrandChild1 from "./GrandChild1";

const Child1 = () => {

    console.log("Child1重新渲染");

    return (
        <Card
            style={{width: 300}}
            title={"子组件1"}
        >
            即使没有使用count，当count变化，组件也会重新渲染
            <GrandChild1/>
        </Card>
    );
};

export default Child1;
