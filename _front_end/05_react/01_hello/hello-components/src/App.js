import React from "react";
import FunctionalComponents from "./FunctionalComponents"; //引入函数式组件
import TemplateSyntax from "./TemplateSyntax";
import Events from "./Events";
import Ref from "./Ref";
import Status from "./Status";
import ListRender from "./ListRender";
import ConditionalRender from "./ConditionalRender";
import DangerouslySetInnerHTML from "./DangerouslySetInnerHTML";
import Properties from "./Properties";

/*
* 继承React.Component类，表示创建一个组件类
* */
class App extends React.Component {
    /**
     * render()方法用于渲染组件
     * @returns {JSX.Element}
     */
    render() {
        return (
            /*
            * 要保证最外层只有一个标签，所以可以用一个div标签包裹起来
            * */
            <div>
                <h1>hello,我是React根组件</h1>
                根组件中的内容
                {/*函数式组件*/}
                <FunctionalComponents/>
                {/*模板语法*/}
                <TemplateSyntax/>
                {/*事件*/}
                <Events/>
                {/*ref*/}
                <Ref/>
                {/*状态*/}
                <Status/>
                {/*列表渲染*/}
                <ListRender/>
                {/*条件渲染*/}
                <ConditionalRender/>
                {/*富文本展示*/}
                <DangerouslySetInnerHTML/>
                {/*属性传递*/}
                <Properties/>
            </div>
        );
    }
}

/*
* 导出组件类
* */
export default App;
