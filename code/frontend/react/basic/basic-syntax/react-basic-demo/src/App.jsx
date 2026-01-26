import {Route, Routes} from "react-router";
import ContextDemo from "./context-demo/ContextDemo";

const App = () => {
    return (
        <Routes>
            {/*Context示例*/}
            <Route path={"/context-demo"} element={<ContextDemo/>}/>
        </Routes>
    );
};

export default App;
