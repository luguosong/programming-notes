import React from 'react';
import {Layout, Watermark} from 'antd';
import {Link, Outlet, useNavigate} from "react-router-dom";
import SiderMenu from "./SiderMenu";
import {GithubOutlined} from "@ant-design/icons";

const {Header, Footer, Sider, Content} = Layout;

const layoutStyle = {
    height: "100%",
    display: "flex"
}

const headerStyle = {
    backgroundColor: "#001628",
    color: "white",
    display: "flex",
    justifyContent: "space-between"
}

const mainStyle = {
    flex: 1,
    display: "flex"
}

const siderStyle = {
    backgroundColor: "#ffffff",
    borderRight: "rgba(100,100,100,0.1) 1px solid",
    display: "auto"
}

const contentStyle = {
    overflow: "auto",
    backgroundColor: "#ffffff",
    height: "100%",
    flex: 1
}

function DefaultLayout(props) {
    const navigate = useNavigate();

    return (
        <Layout style={layoutStyle}>
            <Header style={headerStyle}>
                <div style={{
                    cursor: "pointer",
                }} onClick={() => {
                    navigate("/")
                }}>React学习项目
                </div>
                <Link to={"https://github.com/luguosong/react-demo"} style={{
                    fontSize: "1.2rem",
                    color: "white"
                }}>
                    <GithubOutlined/>
                </Link>

            </Header>
            <Layout style={mainStyle} hasSider>
                <Sider style={siderStyle}>
                    <SiderMenu/>
                </Sider>
                <Watermark style={{width: "100%"}}
                           font={{fontSize: 30, color: "rgba(115,160,197,0.1)"}}
                           zIndex={0}
                           gap={[600, 1000]}
                           offset={[100, 400]}
                           content="陆国松的编程笔记">
                    <Content style={contentStyle}>
                        <Outlet/>
                    </Content>
                </Watermark>
            </Layout>
            <Footer style={{
                backgroundColor: "#001628",
            }}>

            </Footer>
        </Layout>
    );
}

export default DefaultLayout;
