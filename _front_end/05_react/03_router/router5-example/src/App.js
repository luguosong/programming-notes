import {HashRouter, NavLink, Route, Switch} from "react-router-dom";
import Basic from "./Basic";

function App() {
    return (
        <div>

            <HashRouter>
                <div style={{display:"flex"}}>
                    <ul style={{padding:"20px"}}>
                        <li>
                            <NavLink to="/basic">基础</NavLink>
                        </li>
                    </ul>
                    <Switch>
                        <Route path="/basic" component={Basic}/>
                    </Switch>
                </div>
            </HashRouter>
        </div>
    );
}

export default App;
