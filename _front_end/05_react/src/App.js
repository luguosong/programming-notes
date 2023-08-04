import './App.css';
import {Layout} from "antd";
import SideMenu from "./sider";
const { Header, Footer, Sider, Content } = Layout;

function App() {
    return (
        <div>
            <Layout>
                <Sider>
                    <SideMenu/>
                </Sider>
                <Layout>
                    <Header>Header</Header>
                    <Content>Content</Content>
                    <Footer>Footer</Footer>
                </Layout>
            </Layout>
        </div>
    );
}

export default App;
