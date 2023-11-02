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
import ConditionalRendering2 from "../views/describing-the-ui/conditional-rendering";
import Props from "../views/basic/Props";
import ComponentCommunication from "../views/basic/ComponentCommunication";
import ComponentCommunicationPubSub from "../views/basic/ComponentCommunicationPubSub";
import ComponentCommunicationContext from "../views/basic/ComponentCommunicationContext";
import Slot from "../views/basic/Slot";
import Slot2 from "../views/describing-the-ui/slot";
import LifeCycle from "../views/basic/LifeCycle/CCom";
import UseEffect from "../views/basic/LifeCycle/FCom";
import PureComponentDemo from "../views/basic/PureComponentDemo";
import UseCallBack from "../views/basic/UseCallBack";
import UseMemo from "../views/basic/UseMemo";
import UseReducer from "../views/basic/UseReducer";
import CustomHooks from "../views/basic/CustomHooks";
import RouterDemo from "../views/router/RouterDemo";
import DefiningAComponent from "../views/describing-the-ui/defining-a-component";
import UsingAComponent from "../views/describing-the-ui/using-a-component";
import PassingPropsToAComponent from "../views/describing-the-ui/passing-props-to-a-component";
import RenderingLists from "../views/describing-the-ui/rendering-lists";
import RespondingToEvents from "../views/adding-interactivity/responding-to-events";
import StateAComponentsMemory from "../views/adding-interactivity/state-a-components-memory";
import StateAsASnapshot from "../views/adding-interactivity/state-as-a-snapshot";
import QueueingASeriesOfStateUpdates from "../views/adding-interactivity/queueing-a-series-of-state-updates";
import UpdatingObjectsInState from "../views/adding-interactivity/updating-objects-in-state";
import UpdatingArraysInState from "../views/adding-interactivity/updating-arrays-in-state";
import ReactingToInputWithState from "../views/managing-state/reacting-to-input-with-state";
import SharingStateBetweenComponents from "../views/managing-state/sharing-state-between-components";
import PreservingAndResettingState from "../views/managing-state/preserving-and-resetting-state";


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
                    {/*描述UI*/}
                    <Route path={"describing-the-ui"}>
                        <Route path="defining-a-component" element={<DefiningAComponent/>}/>
                        <Route path="using-a-component" element={<UsingAComponent/>}/>
                        <Route path="passing-props-to-a-component" element={<PassingPropsToAComponent/>}/>
                        <Route path="slot" element={<Slot2/>}/>
                        <Route path="conditional-rendering" element={<ConditionalRendering2/>}/>
                        <Route path="rendering-lists" element={<RenderingLists/>}/>

                    </Route>
                    {/*添加交互*/}
                    <Route path={"adding-interactivity"}>
                        <Route path="responding-to-events" element={<RespondingToEvents/>}/>
                        <Route path="state-a-components-memory" element={<StateAComponentsMemory/>}/>
                        <Route path="state-as-a-snapshot" element={<StateAsASnapshot/>}/>
                        <Route path="queueing-a-series-of-state-updates" element={<QueueingASeriesOfStateUpdates/>}/>
                        <Route path="updating-objects-in-state" element={<UpdatingObjectsInState/>}/>
                        <Route path="updating-arrays-in-state" element={<UpdatingArraysInState/>}/>
                    </Route>

                    <Route path={"managing-state"}>
                        <Route path={"reacting-to-input-with-state"} element={<ReactingToInputWithState/>}/>
                        <Route path={"sharing-state-between-components"} element={<SharingStateBetweenComponents/>}/>
                        <Route path={"preserving-and-resetting-state"} element={<PreservingAndResettingState/>}/>
                    </Route>


                    <Route path='*' element={<NotFound/>}/>
                </Route>
            </Routes>
        </BrowserRouter>
    );
}
