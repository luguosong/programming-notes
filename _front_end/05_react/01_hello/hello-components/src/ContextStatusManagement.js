import React, {Component} from 'react';

const GlobalContext = React.createContext();

class ContextStatusManagement extends Component {
    state={
        name:"张三",
        age:18
    }

    render() {
        return (
            <GlobalContext.Provider
                value={{
                    name: this.state.name,
                    age: this.state.age,
                    changeName: (value) => {
                        this.setState({
                            name: value
                        })
                    },
                    changeAge: (value) => {
                        this.setState({
                            age: value
                        })
                    }
                }}>

                <div>
                    <h1>Context状态管理</h1>
                    <Child1/>
                    <Child2/>
                </div>

            </GlobalContext.Provider>
        );
    }
}

class Child1 extends Component {
    render() {
        return (
            <GlobalContext.Consumer>
                {
                    context => {
                        return (
                            <div>
                                <h2>子组件1</h2>
                                <div>姓名：{context.name}</div>
                                <div>年龄：{context.age}</div>
                            </div>
                        )
                    }
                }
            </GlobalContext.Consumer>
        );
    }
}

class Child2 extends Component {
    render() {
        return (
            <GlobalContext.Consumer>
                {
                    context => {
                        return (
                            <div>
                                <h2>子组件2</h2>
                                <div>姓名：<input type="text" value={context.name} onChange={(e) => {
                                    context.changeName(e.target.value)
                                }}/></div>
                                <div>年龄：<input type="text" value={context.age} onChange={(e) => {
                                    context.changeAge(e.target.value)
                                }}/></div>
                            </div>
                        )
                    }
                }
            </GlobalContext.Consumer>
        );
    }
}

export default ContextStatusManagement;
