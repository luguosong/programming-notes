import React from 'react';
import {
    Link,
    Navigate,
    NavLink,
    Outlet,
    Route,
    Routes,
    useNavigate,
    useParams,
    useSearchParams
} from "react-router-dom";
import {Button, Card, Space} from "antd";
import "./index.css"


function RouterDemo() {
    return (
        <div>
            <Routes>
                <Route element={<Root/>}>
                    {/*Navigate进行路由重定向*/}
                    <Route index element={<Navigate to={"child1"}/>}/>
                    <Route path={"child1"} element={<Child1/>}/>
                    <Route path={"child2"} element={<Child2/>}>
                        {/*嵌套路由*/}
                        <Route index element={<Navigate to={"subroute1"}/>}/>
                        <Route path={"subroute1"} element={<Subroute1/>}/>
                        <Route path={"subroute2/:id"} element={<Subroute2/>}/>
                    </Route>
                    <Route path={"login"} element={<Login/>}/>
                    {/*进行路由拦截判断*/}
                    <Route path={"backstage"}
                           element={<PrivateRoute element={<Backstage/>}/>}/>

                    <Route path={"lazyLoad"} element={lazyLoad("./LazyLoadComponent")}/>
                </Route>
            </Routes>
        </div>
    )
}

function Root() {

    const navigate = useNavigate();

    return (
        <div>
            <Space>
                <Button>
                    <Link to="child1">Child1:声明式导航Link</Link>
                </Button>
                <Button>
                    <NavLink to="child2" className={({isActive}) => {
                        return isActive ? "myRouterActive" : "myRouterUnActive"
                    }}>Child2:声明式导航NavLink，可以对选中进行样式定制</NavLink>
                </Button>

                <Button onClick={() => {
                    navigate(`child2/subroute1?id=1000`)
                }}>编程式导航，查询参数传参</Button>

                <Button onClick={() => {
                    navigate(`child2/subroute2/1001`)
                }}>编程式导航，路由传参</Button>


                <Button onClick={() => {
                    navigate(`backstage`)
                }}>后台页面</Button>


                <Button>
                    <Link to="lazyLoad">组件懒加载</Link>
                </Button>
            </Space>

            {/*Outlet作为嵌套路由的路由容器*/}
            <Outlet/>
        </div>)
}

function Child1() {
    return (
        <Card title={"一级路由"}>
            child1
        </Card>
    )
}

function Child2() {
    return (
        <Card title={"一级路由"}>
            child2
            {/*Outlet作为嵌套路由的路由容器*/}
            <Outlet/>
        </Card>
    )
}


function Subroute1() {

    const [searchParams] = useSearchParams();

    return (
        <Card title={"二级路由"}>
            subroute1
            <p>{searchParams.has("id") ? "接收到参数：" + searchParams.get("id") : "id属性不存在"}</p>
        </Card>
    )
}

function Subroute2() {

    const params = useParams();

    return (
        <Card title={"二级路由"}>
            subroute2
            <p>接收到参数：{params.id}</p>
        </Card>
    )
}

// 登录页面
function Login() {

    const navigate = useNavigate();

    return (
        <Card title={"登录页面"}>
            <Button onClick={() => {
                localStorage.setItem("token", true)
                navigate("../backstage")
            }}>登录</Button>
        </Card>
    )
}

// 登录成功后的后台页面
function Backstage() {

    const navigate = useNavigate();

    return (
        <Card title={"后台页面，已登录"}>
            <Button onClick={() => {
                localStorage.removeItem("token")
                navigate("../login")
            }}>退出登录</Button>
        </Card>
    )
}

// 路由拦截
function PrivateRoute({element}) {
    if (!localStorage.getItem("token")) {
        return <Navigate to={"../login"}/>
    }
    return element;
}

// 路由组件懒加载
const lazyLoad = (path) => {
    const Comp = React.lazy(() => import(`${path}`));
    return (
        <React.Suspense fallback={<>加载中</>}>
            <Comp/>
        </React.Suspense>
    )
}

export default RouterDemo;
