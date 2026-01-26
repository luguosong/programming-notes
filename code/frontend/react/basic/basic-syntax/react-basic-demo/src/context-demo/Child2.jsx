import {Card} from "antd";

const Child2 = () => {

    console.log("Child2重新渲染");

    return (
        <Card
            style={{width: 300}}
            title={"子组件2"}
        >
            即使没有使用count，当count变化，组件也会重新渲染
        </Card>
    );
};

export default Child2;
