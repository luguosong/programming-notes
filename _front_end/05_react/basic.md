---
layout: note
title: React基础
nav_order: 10
parent: React
create_time: 2023/8/29
---

# create-react-app脚手架

```shell
# 使用脚手架创建项目
npx create-react-app my-app
```

# 项目构建过程

1. `npm start`启动项目,触发项目的构建和启动。
2. 查找并加载`src/index.js`,这是应用程序的入口点。
3. 在`src/index.js`中，使用`ReactDOM.render()`方法，将React`根组件`渲染到 `public/index.html` 中的根容器`<div id="root">`中。

# index.js

{% highlight js %}
{% include_relative src/index.js %}
{% endhighlight %}

# 函数式组件和类组件

- `类组件`
- `函数式组件`：16.8版本之前没有状态管理，16.8之后采用react hooks进行状态管理

# 创建和使用组件

函数式组件实现：

{% highlight jsx %}
{% include_relative src/views/basic/CreatingAndNestingComponents/FCom.js %}
{% endhighlight %}

类组件实现：

{% highlight jsx %}
{% include_relative src/views/basic/CreatingAndNestingComponents/CCom.js %}
{% endhighlight %}

# Html转JSX

[地址](https://transform.tools/html-to-jsx)

# 添加样式

{% highlight jsx %}
{% include_relative src/views/basic/ComponentStyles/CCom.js %}
{% endhighlight %}

{: .note}
> 推荐使用行内样式，防止CSS污染。

# 事件处理

{% highlight jsx %}
{% include_relative src/views/basic/EventHandling/CCom.js %}
{% endhighlight %}

{: .note}
> React组件中的非箭头函数中的this无法访问组件实例中的属性和方法，需要使用箭头函数或使用`bind`绑定this。

{: .note-title}
> 事件代理
> 
> React中的事件处理是基于事件代理的，事件代理是将事件绑定到`root根元素上`，通过事件`冒泡机制`，触发父元素上的事件，从而实现事件处理。
> 
> 事件对象（通常称为event）在事件冒泡过程中会携带相关信息，其中包括触发事件的目标元素（即触发事件的子元素）以及其他与事件相关的信息。通过这些信息获取到触发事件的子元素，从而实现事件处理。

# 引用

{% highlight jsx %}
{% include_relative src/views/basic/Ref/CCom.js %}
{% endhighlight %}

# 状态

状态改变组件会重新渲染，组件修改状态不会自动改变。

## 函数式组件实现

{% highlight jsx %}
{% include_relative src/views/basic/State/FCom.js %}
{% endhighlight %}

## 类组件实现

`setState`是异步的，如果需要获取最新的状态，需要使用回调函数。

{% highlight jsx %}
{% include_relative src/views/basic/State/CCom.js %}
{% endhighlight %}

# 列表循环

{% highlight jsx %}
{% include_relative src/views/basic/List/CCom.js %}
{% endhighlight %}

# 条件渲染

- 使用三元运算符
- 使用逻辑与运算符`&&`

{% highlight jsx %}
{% include_relative src/views/basic/ConditionalRendering/CCom.js %}
{% endhighlight %}

# 属性（父传子）

可以通过属性传递数据，提高组件复用性。

属性不能在子组件中修改，只能通过父组件修改。

函数式组件：

{% highlight jsx %}
{% include_relative src/views/basic/Props/FCom.js %}
{% endhighlight %}

类组件：

{% highlight jsx %}
{% include_relative src/views/basic/Props/CCom.js %}
{% endhighlight %}

# 组件通信（子传父，父传子）

{% highlight jsx %}
{% include_relative src/views/basic/ComponentCommunication/CCom.js %}
{% endhighlight %}

# 组件通信（发布订阅）

使用发布订阅模式。

{% highlight jsx %}
{% include_relative src/views/basic/ComponentCommunicationPubSub/CCom.js %}
{% endhighlight %}

# 组件通信（Context）

使用生产者消费者模式。

```jsx
{% raw %}
import React, {Component, createContext} from 'react';
import {Card, Input} from "antd";

const context = createContext();

const {Provider, Consumer} = context;

class Child1 extends Component {
    render() {
        return (
            <Consumer>
                {
                    (value) =>
                        <div>
                            <Card title={"子组件1"}>
                                <Input addonBefore={"子组件1修改内容"} onChange={(e) => {
                                    value.setMsg(e.target.value)
                                }}></Input>
                            </Card>
                        </div>
                }
            </Consumer>

        );
    }
}

class Child2 extends Component {
    render() {
        return (
            <Consumer>
                {
                    (value) =>
                        <div>
                            <Card title={"子组件2"}>
                                接收来自子组件1的内容：{value.msg}
                            </Card>
                        </div>
                }
            </Consumer>


        );
    }
}

class CCom extends Component {

    //只有通过状态的改变，才会刷新组件
    state = {
        msg: ""
    }

    render() {
        return (
            <Provider value={{
                msg: this.state.msg,
                setMsg: (value) => {
                    this.setState({
                        msg: value
                    })
                }
            }}>
                <div>
                    <Child1 onChange={(value) => {

                    }}></Child1>
                    <Child2></Child2>
                </div>
            </Provider>
        );
    }
}

export default CCom;
{% endraw %}
```

# 插槽

可以通过插槽实现组件的复用。并且减少父子组件间的通信。

{% highlight jsx %}
{% include_relative src/views/basic/Slot/CCom.js %}
{% endhighlight %}

# 生命周期

- `constructor`：构造函数，初始化state和props
- ~~`componentWillMount`~~：组件将要挂载
- componentDidMount: 组件挂载完成
- ~~`componentWillReceiveProps(nextProps)`~~：组件将要接收新的props
- `getDerivedStateFromProps(nextProps, prevState)`：组件将要接收新的props或state
- `shouldComponentUpdate(nextProps, nextState)`: 组件是否需要更新,返回true更新，返回false不更新
- ~~`componentWillUpdate`~~：组件将要更新
- `getSnapshotBeforeUpdate(prevProps, prevState)`：组件更新前获取快照
- `componentDidUpdate(prevProps, prevState)`：组件更新完成
- `componentWillUnmount`：组件将要卸载
- `render`：渲染函数

{% highlight jsx %}
{% include_relative src/views/basic/LifeCycle/CCom.js %}
{% endhighlight %}

# PureComponent

使用setStatus更新状态时，如果想优化性能，我自可以自己手动在`shouldComponentUpdate`中进行判断，但是这样会很麻烦，React提供了`PureComponent`，可以自动帮我们进行判断。

{% highlight jsx %}
{% include_relative src/views/basic/PureComponentDemo/CCom.js %}
{% endhighlight %}

