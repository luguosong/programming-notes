import {Card} from "antd";
import {useContext} from "react";
import {CountContext} from "./CountContext";

const GrandChild1 = () => {

    const count = useContext(CountContext)

    console.log("GrandChild1重新渲染");

    return (
        <Card
            title={"孙组件1"}
        >
            Count:{count}
        </Card>
    );
};

export default GrandChild1;
