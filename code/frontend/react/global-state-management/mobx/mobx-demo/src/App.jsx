import {Route, Routes} from "react-router";
import MobxReactDemo from "./mobx-react-demo/MobxReactDemo";

const App = () => {
    return (
        <Routes>
            {/*Mobx React示例*/}
            <Route path={"mobx-react-demo"} element={<MobxReactDemo/>}/>
        </Routes>
    );
};

export default App;
