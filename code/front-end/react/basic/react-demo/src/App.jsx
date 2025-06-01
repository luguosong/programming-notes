import {BrowserRouter, HashRouter, Route, Routes} from "react-router";
import {Result} from "antd";

const App = () => {

    const urlParams = new URLSearchParams(window.location.search);
    const page = urlParams.get("page");


    return (
        <>{page}</>
    );
};

export default App;
