import React from 'react';
import {HashRouter, NavLink, Redirect, Route, Switch, useHistory, withRouter} from "react-router-dom";

function Basic(props) {

    /*
    * 使用props.history.push进行编程式导航
    * */
    const handlePage2 = (id) => {
        props.history.push(`/basic/page2/${id}`);
    }

    /*
    * 使用useHistory进行编程式导航
    * */
    const history = useHistory();

    const handlePage3 = (id) => {
        history.push("/basic/page3/333");
    }

    /**
     * 路由拦截判断函数
     */
    function isAuth() {
        return localStorage.getItem("token")
    }


    return (
        <div>
            <h1>01_导航基础</h1>
            {/*导航可以分为BrowserRouter和HashRouter*/}
            <HashRouter>

                {/*导航*/}
                {/*💀NavLink标签必须在HashRouter标签内部*/}
                {/*可以自定义activeClassName*/}
                <NavLink to={{pathname: "/basic/page1/111"}} activeClassName="myActive">声明式导航 </NavLink>
                <NavLink to={{pathname: "/basic/page11", state: {name: "张三"}}}
                         activeClassName="myActive">声明式导航-state传参 </NavLink>
                <button onClick={() => handlePage2(222)}> 编程式导航-props.history.push</button>
                <button onClick={() => handlePage3(333)}>编程式导航-useHistory</button>
                <NavLink to="/basic/auth"> 路由拦截认证</NavLink>
                <NavLink to="/basic/withRouter"> withRouter测试</NavLink>


                {/*Switch只渲染匹配到的第一个组件，匹配到后就跳出*/}
                <Switch>
                    {/*配置路由*/}
                    {/*path是模糊匹配的，/basic/page1/111也能匹配到/basic/page1*/}
                    <Route path="/basic/page1/:myid" component={Page1}/>
                    <Route path="/basic/page11" component={Page11}/>
                    <Route path="/basic/page2/:myid" component={Page2}/>
                    <Route path="/basic/page3/:myid" component={Page3}/>


                    {/*路由拦截*/}
                    <Route path="/basic/auth" render={() => {
                        return isAuth() ? <LoginSuccess/> : <Redirect to="/basic/login"/>
                    }}/>
                    <Route path="/basic/login" component={Login}/>

                    {/*展示使用withRouter*/}
                    <Route path="/basic/withRouter" render={() => {
                        return <WithRouter1/>
                    }}/>

                    {/*路由重定向*/}
                    {/*👻注意：from默认是模糊匹配，加上exact可以转为精准匹配*/}
                    <Redirect from="/basic" to="/basic/page1/100" exact/>

                    {/*配置404路由*/}
                    <Route component={NotFound}/>
                </Switch>
            </HashRouter>
        </div>
    );
}

/**
 * 子组件1
 * @param props
 * @returns {JSX.Element}
 * @constructor
 */
function Page1(props) {
    console.log(props)
    return (
        <div>
            <h2>Page1</h2>
            <p>通过动态路由获取参数：{props.match.params.myid}</p>
        </div>
    );
}

/**
 * 子组件11
 * @param props
 * @returns {JSX.Element}
 * @constructor
 */
function Page11(props) {
    console.log(props)
    return (
        <div>
            <h2>Page11</h2>
            <p>通过state传参：{props.location.state.name}</p>
        </div>
    );
}

/**
 * 子组件2
 * @param props
 * @returns {JSX.Element}
 * @constructor
 */
function Page2(props) {
    return (
        <div>
            <h2>Page2</h2>
            <p>通过动态路由获取参数：{props.match.params.myid}</p>
        </div>
    );
}

/**
 * 子组件3
 * @param props
 * @returns {JSX.Element}
 * @constructor
 */
function Page3(props) {
    console.log(props)
    return (
        <div>
            <h2>Page3</h2>
            <p>通过动态路由获取参数：{props.match.params.myid}</p>
        </div>
    );
}

function NotFound() {
    return (
        <div>
            404
        </div>
    );
}

function Login(props) {
    return (
        <div>
            <h2>登录页面</h2>
            <button onClick={() => {
                localStorage.setItem("token", "xxxxxx")
                props.history.push("/basic/auth")
            }}>登录
            </button>
        </div>
    );
}

function LoginSuccess(props) {
    const history = useHistory();

    return (
        <div>
            <h2>登录成功页面</h2>
            <button onClick={() => {
                localStorage.removeItem("token")
                history.push("/basic/auth")
            }}>退出登录
            </button>
        </div>
    );
}

/**
 * 测试withRouter
 * @param props
 * @returns {JSX.Element}
 * @constructor
 */
function Page4(props) {
    console.log(props)
    return (
        <div>
            <h2>Page4</h2>
        </div>
    );
}

/*
* 这样WithRouter1的props就会有history、location、match属性
* */
const WithRouter1 = withRouter(Page4);

export default Basic;
