import React, { Component } from "react"
import "./TabsExample.css"

/*
* 选项卡综合示例
* */
class TabsExample extends Component {
    
    state = {
        list: [
            {
                id: 1,
                text: "选项卡1",
            },
            {
                id: 2,
                text: "选项卡2",
            },
            {
                id: 3,
                text: "选项卡3",
            },
        ],
    }

    render() {
        return (
            <div>
                <h1>选项卡示例</h1>
                <ul>
                    {
                        this.state.list.map(item => {
                            return <li key={item.id}>{item.text}</li>
                        })
                    }
                </ul>
            </div>
        )
    }
}

export default TabsExample
