import {Card} from "antd";
import {useContext} from "react";
import {CountContext} from "./CountContext";
import {observer} from "mobx-react"

const GrandChild1 = observer(() => {

    const myTimer = useContext(CountContext)

    console.log("GrandChild1重新渲染");

    return (
        <Card
            title={"孙组件1"}
        >
            Count:{myTimer.count}
        </Card>
    );
});

export default GrandChild1;
