---
layout: note
title: React
nav_order: 50
create_time: 2023/6/29
---

# create-react-app脚手架

```shell
# 使用脚手架创建项目
npx create-react-app my-app
```

# 项目构建过程

1. `npm start`启动项目,触发项目的构建和启动。
2. 查找并加载`src/index.js`,这是应用程序的入口点。
3. 在`src/index.js`中，使用`ReactDOM.render()`方法，将React`根组件`渲染到 `public/index.html` 中的根容器`<div id="root">`
   中。

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

## 基本用法

{% highlight jsx %}
{% include_relative src/views/basic/ComponentStyles/CCom.js %}
{% endhighlight %}

{: .note}
> 推荐使用行内样式，防止CSS污染。

## ⭐CSSModule

将css文件命名为`xxx.module.css`,然后在组件中将css当作对象使用。

这样可以防止css之间相互污染。

{% highlight jsx %}
{% include_relative src/views/basic/ComponentStyles/CssModuleCCom.js %}
{% endhighlight %}

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

## 函数式组件

{% highlight jsx %}
{% include_relative src/views/basic/Ref/FCom.js %}
{% endhighlight %}

## 类组件

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

使用`createContext`创建一个上下文，使用`Provider`提供上下文，使用`Consumer`消费上下文。

## 函数式组件（useContext）

{% highlight jsx %}
{% include_relative src/views/basic/ComponentCommunicationContext/FCom.js %}
{% endhighlight %}

## 类组件

{% highlight jsx %}
{% include_relative src/views/basic/ComponentCommunicationContext/CCom.js %}
{% endhighlight %}

# 状态管理（useReducer）

将状态管理与组件分离。

一般useReducer会和useContext一起配合使用。

{% highlight jsx %}
{% include_relative src/views/basic/UseReducer/FCom.js %}
{% endhighlight %}

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

使用setStatus更新状态时，如果想优化性能，我自可以自己手动在`shouldComponentUpdate`
中进行判断，但是这样会很麻烦，React提供了`PureComponent`，可以自动帮我们进行判断。

{% highlight jsx %}
{% include_relative src/views/basic/PureComponentDemo/CCom.js %}
{% endhighlight %}

# useEffect

- useEffect中如果第二个参数数组为空，则只会在组件挂载时执行，相当于`componentDidMount`。
- useEffect如果第二个参数不为空，则会监听对应的内容变化，相当于`componentDidUpdate`。
- useEffect如果返回一个函数，则会在组件卸载时执行，相当于`componentWillUnmount`。

{% highlight jsx %}
{% include_relative src/views/basic/LifeCycle/FCom.js %}
{% endhighlight %}

# useCallBack

当组件更新时，函数会重新创建。为了避免这种情况，可以使用`useCallBack`。

{% highlight jsx %}
{% include_relative src/views/basic/UseCallBack/FCom.js %}
{% endhighlight %}

# useMemo-计算属性

useMemo类似与Vue中的计算属性。

{% highlight jsx %}
{% include_relative src/views/basic/UseMemo/FCom.js %}
{% endhighlight %}

# 自定义hooks

自定义hooks可以将组件逻辑提取到可重用的函数中。

{% highlight jsx %}
{% include_relative src/views/basic/CustomHooks/FCom.js %}
{% endhighlight %}

# ⭐React Router V6

## 概述

- `react-router`:核心模块
- `react-router-dom`：开发网页，包含`react-router`
- `react-router-native`：开发Native应用

## 安装

```shell
npm install react-router-dom
```

## 路由模式

- `BrowserRouter`：浏览器路由模式，使用`history.pushState`和`history.replaceState`实现路由跳转，不会刷新页面。
- `HashRouter`：hash路由模式，使用`location.hash`实现路由跳转，不会刷新页面。

## 路由组件-Route

- `path`：路由路径
- `element`：路由组件
- `index`: 默认路由

## 重定向

```jsx
// 方式一：使用Navigate组件
<Route path="" element={<Navigate to={"CreatingAndNestingComponents"}/>}/>
```

```jsx
// 方式二：自定义Redirect组件
<Route path="" element={<Redirect to={"CreatingAndNestingComponents"} />} />

function Redirect(props) {
  const { to } = props

  useEffect(() => {
    // replace: true表示替换当前路由
    navigator.to(to, { replace: true })
  })
  return null
}
```

## 嵌套路由

```jsx
<Route path="Leve1" element={<Leve1/>}>
  <Route path="Leve2-1" element={<Leve21/>}/>
  <Route path="Leve2-2" element={<Leve22/>}/>
</Route>
```

```jsx
function Leve1() {
   return (
     <div>
        {/*Leve1组件中使用路由容器接收子路由*/}
        <Outlet></Outlet>
     </div>
   )
}
```

## 导航

声明式导航：

```jsx
// 方式一
<Link to="child1">Child1</Link>

// 方式二:可以对导航进行样式定制
<NavLink to="child2" className={({isActive}) => {
   return isActive ? "myRouterActive" : "myRouterUnActive"
}}>Child2:声明式导航NavLink，可以对选中进行样式定制</NavLink>
```


编程式导航：

```jsx
const navigate = useNavigate();

// 组件使用useSearchParams接收参数
<Button onClick={() => {
   navigate(`child2/subroute1?id=1000`)
}}>编程式导航，查询参数传参</Button>

// 组件使用useParams接收参数
<Button onClick={() => {
   navigate(`child2/subroute2/1001`)
}}>编程式导航，路由传参</Button>
```

## 路由拦截

```jsx
<Route path={"login"} element={<Login/>}/>
{/*进行路由拦截判断*/}
<Route path={"backstage"}
       element={<PrivateRoute element={<Backstage/>}/>}/>
```

```jsx
function PrivateRoute({element}) {
   if (!localStorage.getItem("token")) {
      return <Navigate to={"../login"}/>
   }
   return element;
}
```

{: .warning}
> 注意！这里不能直接在路由中进行判断。
> 
> 因为路由只会在页面加载时执行一次，如果在路由中进行判断，当token失效时，无法进行跳转。

## 路由组件懒加载

```jsx
// 路由组件懒加载
const lazyLoad = (path) => {
   const Comp = React.lazy(() => import(`${path}`));
   return (
           <React.Suspense fallback={<>加载中</>}>
              <Comp/>
           </React.Suspense>
   )
}
```

## useRoutes

```jsx
import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import { useRoutes } from 'react-router-dom';

function Home() {
   return <div>Home Page</div>;
}

function About() {
   return <div>About Page</div>;
}

function Contact() {
   return <div>Contact Page</div>;
}

const routes = [
   { path: '/', element: <Home /> },
   { path: '/about', element: <About /> },
   { path: '/contact', element: <Contact /> },
];

function App() {
   const routeResult = useRoutes(routes);

   return (
           <Router>
              <div>
                 <h1>My App</h1>
                 {routeResult}
              </div>
           </Router>
   );
}

export default App;
```

# 反向代理解决跨域

安装`http-proxy-middleware`。

```shell
npm install http-proxy-middleware --save
```

在`src`目录下创建`setupProxy.js`文件。

```js
const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function(app) {
  app.use(
    '/api',
    createProxyMiddleware({
      target: 'https://www.xxxx.com:5000',
      changeOrigin: true,
    })
  );
};
```

# Redux


