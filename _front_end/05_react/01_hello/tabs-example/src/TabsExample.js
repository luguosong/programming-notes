import React, {Component} from "react"
import "./TabsExample.css"
import Cinema from "./Cinema";

function Tab2() {
    return (
        <div>
            标签2
        </div>
    )
}

function Tab3() {
    return (
        <div>
            标签3
        </div>
    )
}

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
        current: 0
    }

    currentPage() {
        switch (this.state.current) {
            case 0:
                return <Cinema/>
            case 1:
                return <Tab2/>
            case 2:
                return <Tab3/>
            default:
                return null
        }
    }

    render() {
        return (
            <div>
                {/*根据current判断显示哪个组件*/}
                {/*方式一渲染页面*/}
                {/*{this.state.current===0&& <Tab1/>}*/}
                {/*{this.state.current===1&& <Tab2/>}*/}
                {/*{this.state.current===2&& <Tab3/>}*/}
                {/*方式二渲染页面*/}
                {
                    this.currentPage()
                }

                <ul>
                    {
                        this.state.list.map((item, index) => {
                            return <li key={item.id} className={this.state.current === index ? 'active' : ''}
                                       onClick={() => {
                                           this.setState({
                                               current: index
                                           })
                                       }}>{item.text}</li>
                        })
                    }
                </ul>
            </div>
        )
    }
}

export default TabsExample
