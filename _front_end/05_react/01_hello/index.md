---
layout: note
title: 基础
nav_order: 10
parent: React
create_time: 2023/6/27
---

# 2023年的React生态系统

[原文](https://www.builder.io/blog/react-js-in-2023)

- 开始使用React
    - CodeSandbox
    - StackBlitz
    - Vite
    - Next.js
- 路由
    - React Router
    - TanStack Router
    - Next.js
- 客户端状态管理
    - Redux Toolkit
    - Zustand
    - Recoil
    - Jotai
- 服务器状态管理
    - TanStack Query
    - SWR
    - Redux Toolkit Query
    - Apollo Client
- 表单处理
    - Formik
    - React Hook Form
- 测试
    - Vitest
    - Playwright and Cypress
- 样式
    - Tailwind
    - Styled Components
    - Emotion
    - CSS Modules
    - Vanilla Extract
- UI组件库
    - Material UI
    - Ant Design
    - Chakra UI
    - Mantine UI
    - ShadCN
    - Daisy UI
    - Headless UI
- 动画
    - React Spring
    - Framer Motion
    - GreenSock
- 数据可视化
  - Victory
  - React Chartjs
  - Recharts
- 表格
  - React Data Grid
  - TanStack Table
- 国际化
  - i18next
  - React-Intl

# 项目创建

{: .note}
> `Create React App`是一种官方支持的创建单页React应用程序的方式。它提供了一个现代化的构建设置，无需配置。

```shell
# 安装全局脚手架
npm install -g create-react-app
```

```shell
# 创建项目
create-react-app hello

npm install

npm start
```

# 入门案例

最简单的React只需要`/public/index.html`入口文件和`/src/index.js`核心js文件。代码如下：

{% highlight html %}
{% include_relative hello/public/index.html %}
{% endhighlight %}

{% highlight js %}
{% include_relative hello/src/index.js %}
{% endhighlight %}

# JSX简介

`JSX（JavaScript XML）`是一种 JavaScript 的语法扩展，用于在 React 应用中描述用户界面的结构和外观。它允许您在 JavaScript 代码中编写类似 HTML 的标记语法，以声明性地创建组件的结构。

# 案例预览

[预览](hello-components/build/index.html)

# 组件开发

创建根组件：

{% highlight js %}
{% include_relative hello-components/src/App.js %}
{% endhighlight %}

在`index.js`中引入根组件：

{% highlight js %}
{% include_relative hello-components/src/index.js %}
{% endhighlight %}

# 函数式组件

{% highlight js %}
{% include_relative hello-components/src/FunctionalComponents.js %}
{% endhighlight %}

# 模板语法和样式绑定

{% highlight js %}
{% include_relative hello-components/src/TemplateSyntax.js %}
{% endhighlight %}

外部样式`TemplateSyntax.css`：

{% highlight css %}
{% include_relative hello-components/src/TemplateSyntax.css %}
{% endhighlight %}

# 事件绑定

{: .note-title}
> React事件绑定和原生事件绑定的区别
> 
> React并不会真正的将事件绑定到节点上，而是采用事件代理的模式

{% highlight js %}
{% include_relative hello-components/src/Events.js %}
{% endhighlight %}

# 引用

{% highlight js %}
{% include_relative hello-components/src/Ref.js %}
{% endhighlight %}

# 状态

通过`state`关键字定义状态，通过`setState`方法修改状态。

{% highlight js %}
{% include_relative hello-components/src/Status.js %}
{% endhighlight %}

# 列表渲染

{% highlight js %}
{% include_relative hello-components/src/ListRender.js %}
{% endhighlight %}

# 条件渲染

{% highlight js %}
{% include_relative hello-components/src/ConditionalRender.js %}
{% endhighlight %}

# 富文本展示

{% highlight js %}
{% include_relative hello-components/src/DangerouslySetInnerHTML.js %}
{% endhighlight %}

# 📖选项卡综合案例

`TabsExample.js`涉及以下知识点：

- 列表渲染
- 条件渲染
- 状态
- 数组遍历：map

{% highlight js %}
{% include_relative tabs-example/src/TabsExample.js %}
{% endhighlight %}

Tab样式：

{% highlight css %}
{% include_relative tabs-example/src/TabsExample.css %}
{% endhighlight %}

`Cinema.js`涉及知识点：

- axios发送请求
- filter过滤
- 状态渲染和更新
- 列表渲染

{% highlight js %}
{% include_relative tabs-example/src/Cinema.js %}
{% endhighlight %}

Cinema样式：

{% highlight js %}
{% include_relative tabs-example/src/Cinema.css %}
{% endhighlight %}

# 属性

用于组件之间的数据传递。

{: .warning}
> 属性是只读的，不能修改。

{% highlight js %}
{% include_relative hello-components/src/Properties.js %}
{% endhighlight %}

# 父子组件通信

- `父传子`：通过属性传递，使用子组件不是一成不变的，可以通过属性传递不同的值。提高复用性
- `子传父`：通过回调函数传递，父组件传递一个函数给子组件，子组件调用该函数，从而改变父组件的状态。当父组件中存在多个子组件时，可以方便子组件之间的通信。

下图展示通过父子组件通讯，间接实现子组件1和子组件2之间的通讯：

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202307071035851-%E7%88%B6%E5%AD%90%E7%BB%84%E4%BB%B6%E9%80%9A%E8%AE%AF.png)

{% highlight js %}
{% include_relative hello-components/src/ParentChildCommunication.js %}
{% endhighlight %}

# 受控组件和非受控组件

- 受控组件
  - 子组件的状态受父组件控制，父组件通过属性传递状态给子组件，子组件通过回调函数将状态传递给父组件。
  - 子组件中不存储状态，所有状态都存储在父组件中。
- 非受控组件
  - 子组件的状态不受父组件控制，子组件自己管理状态。
