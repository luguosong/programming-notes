import React from 'react';
import {BrowserRouter, Route, Routes} from "react-router-dom";
import DefaultLayout from "../layout/DefaultLayout/DefaultLayout";
import CreatingAndNestingComponents from "../views/basic/CreatingAndNestingComponents";
import {Result} from "antd";
import ComponentStyles from "../views/basic/ComponentStyles";
import EventHandling from "../views/basic/EventHandling";
import Ref from "../views/basic/Ref";
import State from "../views/basic/State";
import List from "../views/basic/List";
import ConditionalRendering from "../views/basic/ConditionalRendering";
import Props from "../views/basic/Props";
import ComponentCommunication from "../views/basic/ComponentCommunication";
import ComponentCommunicationPubSub from "../views/basic/ComponentCommunicationPubSub";
import ComponentCommunicationContext from "../views/basic/ComponentCommunicationContext";
import Slot from "../views/basic/Slot";
import LifeCycle from "../views/basic/LifeCycle/CCom";
import UseEffect from "../views/basic/LifeCycle/FCom";
import PureComponentDemo from "../views/basic/PureComponentDemo";
import UseCallBack from "../views/basic/UseCallBack";
import UseMemo from "../views/basic/UseMemo";
import UseReducer from "../views/basic/UseReducer";
import CustomHooks from "../views/basic/CustomHooks";
import RouterDemo from "../views/router/RouterDemo";


function NotFound() {
    return (<Result
        status="404"
        title="404"
        subTitle="Sorry, 页面不存在"
    />)
}

function Main() {
    return (<div>
        {/*主页*/}
        主页
    </div>)
}


export default function MainRouter(props) {
    return (
        <BrowserRouter>
            <Routes>

                <Route path={"/"} element={<DefaultLayout/>}>
                    {/*主页*/}
                    <Route index element={<Main/>}/>
                    {/*基础知识*/}
                    <Route path={"basic"}>
                        <Route index path="CreatingAndNestingComponents" element={<CreatingAndNestingComponents/>}/>
                        <Route path="ComponentStyles" element={<ComponentStyles/>}/>
                        <Route path="EventHandling" element={<EventHandling/>}/>
                        <Route path="Ref" element={<Ref/>}/>
                        <Route path="State" element={<State/>}/>
                        <Route path="List" element={<List/>}/>
                        <Route path="ConditionalRendering" element={<ConditionalRendering/>}/>
                        <Route path="Props" element={<Props/>}/>
                        <Route path="ComponentCommunication" element={<ComponentCommunication/>}/>
                        <Route path="ComponentCommunicationPubSub" element={<ComponentCommunicationPubSub/>}/>
                        <Route path="ComponentCommunicationContext" element={<ComponentCommunicationContext/>}/>
                        <Route path="UseReducer" element={<UseReducer/>}/>
                        <Route path="Slot" element={<Slot/>}/>
                        <Route path="LifeCycle" element={<LifeCycle/>}/>
                        <Route path="PureComponentDemo" element={<PureComponentDemo/>}/>
                        <Route path="UseEffect" element={<UseEffect/>}/>
                        <Route path="UseCallBack" element={<UseCallBack/>}/>
                        <Route path="UseMemo" element={<UseMemo/>}/>
                        <Route path="CustomHooks" element={<CustomHooks/>}/>
                    </Route>

                    <Route path={"router"}>
                        <Route path="RouterDemo/*" element={<RouterDemo/>}/>
                    </Route>

                    <Route path='*' element={<NotFound/>}/>
                </Route>
            </Routes>
        </BrowserRouter>
    );
}
