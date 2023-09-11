import './App.css';
import React from "react";
import MainRouter from "./router";

const mainStyle = {
    height: "100%"
}

function App() {
    return (
        <div style={mainStyle}>
            <MainRouter/>
        </div>

    );
}

export default App;
