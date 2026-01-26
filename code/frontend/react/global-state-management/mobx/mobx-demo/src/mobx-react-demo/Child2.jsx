import {Card} from "antd";

const Child2 = () => {

    console.log("Child2重新渲染");

    return (
        <Card
            style={{width: 300}}
            title={"子组件2"}
        >
            组件没有使用count，即使count变化，组件不会重新渲染
        </Card>
    );
};

export default Child2;
