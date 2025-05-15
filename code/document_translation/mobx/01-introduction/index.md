# 介绍

## 关于MobX

简单且可扩展的状态管理。

### 介绍

任何可以从应用状态中推导出来的信息，都应该自动完成推导。

MobX 是一个基于信号、经过实战检验的库，通过透明地应用函数式响应式编程，让状态管理变得简单且易于扩展。MobX 背后的理念很简单：

- `简洁直观`:编写极简、无样板代码，精准表达你的意图。想要更新某个记录字段？只需像平常一样用 JavaScript
  赋值即可——响应式系统会自动检测你的所有更改，并将其传播到被使用的地方。即使在异步流程中更新数据，也无需任何特殊工具。
- `极致高效的渲染`:所有对数据的更改和使用都会在运行时被追踪，自动构建依赖树，完整捕捉状态与输出之间的所有关系。这确保了依赖于状态的计算（如
  React 组件）只会在真正需要时才运行。无需再用易出错、效果有限的记忆化或选择器等手动优化组件。
- `架构自由`:MobX 不强加任何架构约束，你可以在任何 UI 框架之外管理应用状态。这让你的代码高度解耦、易于移植，更重要的是，测试起来也非常方便。

### 一个简单的例子

那么，使用 MobX 的代码是什么样子的呢？

```javascript
import React from "react"
import ReactDOM from "react-dom"
import {makeAutoObservable} from "mobx"
import {observer} from "mobx-react-lite"

// 对应用状态建模
function createTimer() {
    return makeAutoObservable({
        secondsPassed: 0,
        increase() {
            this.secondsPassed += 1
        },
        reset() {
            this.secondsPassed = 0
        }
    })
}

const myTimer = createTimer()

// 构建一个利用可观察状态的“用户界面”。
const TimerView = observer(({timer}) => (
    <button onClick={() => timer.reset()}>Seconds passed: {timer.secondsPassed}</button>
))

ReactDOM.render(<TimerView timer={myTimer}/>, document.body)

// 每秒更新一次“已过去秒数：X”的文本。
setInterval(() => {
    myTimer.increase()
}, 1000)
```

包裹在 TimerView React 组件外的 `observer`，会自动检测到组件的渲染依赖于 `timer.secondsPassed`
这个可观察属性，即使这种依赖关系并没有被显式声明。当该字段在未来发生更新时，响应式系统会自动负责重新渲染该组件。

每当发生事件（如 `onClick` 或 `setInterval`），都会触发一个`动作（如 myTimer.increase 或 myTimer.reset）`，从而更新
`可观察的状态（myTimer.secondsPassed）`。可观察状态的变化会被精确地传递到所有依赖这些变化的计算和副作用（如 TimerView）中。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505132215086.png){ loading=lazy }
  <figcaption>概念图</figcaption>
</figure>

这个概念图不仅适用于上面的例子，也适用于任何使用 MobX 的应用程序。

### 入门指南

[10 分钟的 MobX 与 React 交互式入门课程](https://mobx.js.org/getting-started)。

关于 MobX
所提供的思维模型的理念与优势，也在博客文章[《UI as an afterthought》](https://michel.codes/blogs/ui-as-an-afterthought)
和[《How to decouple state and UI（也就是你不需要 componentWillMount）》](https://hackernoon.com/how-to-decouple-state-and-ui-a-k-a-you-dont-need-componentwillmount-cc90b787aa37)
中有非常详细的阐述。

## 关于这个文档

它遵循这样一个原则：`最常用的概念会在介绍更专业的信息之前先行讲解。`这一原则不仅适用于概念表中的标题，也适用于这些标题下的各个页面。

我们用 {🚀} 标记了那些更高级的章节和概念。除非你有特殊的使用场景，否则你大概率不需要理解这些内容，而且即使不了解它们，你也能非常高效地使用
MobX。你可以放心跳过这些部分，直接进入下一节！

本份文档已经针对 MobX 6 进行了重写。如果你需要查阅旧版本 MobX
的文档，可以在[这里](https://github.com/mobxjs/mobx/tree/mobx4and5/docs)找到。所有的基本原理都是一致的，API
也基本没有变化。主要的区别在于，`MobX 6 之前推荐使用装饰器语法来编写 MobX 增强类`。

### 导览讲解

要全面了解如何在 React 中使用 MobX，请先阅读当前的“介绍”部分，尤其是“MobX 的要点”这一节。这里会向你介绍最重要的原则、API
以及它们之间的关系。读完这些内容后，你就可以开始使用 MobX 了！

接下来可以参考以下建议，进一步深入学习：

- 试试 MobX 和 React 的 10 分钟交互式入门教程
- 了解 React 集成
- 学习 makeObservable / makeAutoObservable 的用法
- 了解 actions，包括异步 actions 的相关讨论
- 掌握 computed 的基础知识
- 阅读 autorun 的相关内容，哪怕只是因为它在示例中经常出现
- 想了解如何组织应用的数据存储，可以看看“定义数据存储”部分
- 如果 MobX 的行为让你感到困惑，不妨查阅“理解响应式”相关内容
- 快速浏览一下 API 概览，导航栏顶部也有链接

这些内容能帮助你全面掌握 MobX 在日常开发中的用法。当然，还有更多资料可以根据你的兴趣和需求自行阅读。

## 安装

MobX 可在任何 ES5 环境下运行，包括浏览器和 NodeJS。React 有三种类型的绑定方式：

• `mobx-react-lite`：用于手动应用观察的工具  
• `mobx-react-observer`：Babel/swc 插件，可自动为组件应用观察  
• `mobx-react`：支持类组件

根据你的使用场景，将相应的绑定包添加到下面的 Yarn 或 NPM 命令中即可：

Yarn: `yarn add mobx`

NPM: `npm install --save mobx`

CDN: [https://cdnjs.com/libraries/mobx](https://cdnjs.com/libraries/mobx) / [https://unpkg.com/mobx/dist/mobx.umd.production.min.js](https://unpkg.com/mobx/dist/mobx.umd.production.min.js)

### 转译设置

#### MobX与装饰器

根据你的偏好，MobX 可以选择使用或不使用装饰器。目前，MobX 同时支持传统实现方式和 TC-39 标准版的装饰器。需要注意的是，MobX 7
将移除对传统装饰器的支持，全面采用标准版装饰器。

#### 对类属性使用符合规范的转译方式

在使用 MobX 搭配 TypeScript 或 Babel，并且打算使用类时，请确保你的配置采用了符合 TC-39
规范的类字段转译方式，因为这并非总是默认设置。如果没有这样配置，类字段在初始化之前将无法被设为可观察（observable）。

- TypeScript：请将编译选项 "useDefineForClassFields" 设置为 true。
- Babel：请确保使用至少 7.12 版本，并进行如下配置：

``` json
{
  // Babel < 7.13.0
  "plugins": [
    [
      "@babel/plugin-proposal-class-properties",
      {
        "loose": false
      }
    ]
  ],
  // Babel >= 7.13.0 (https://babeljs.io/docs/en/assumptions)
  "plugins": [
    [
      "@babel/plugin-proposal-class-properties"
    ]
  ],
  "assumptions": {
    "setPublicClassFields": false
  }
}
```

为验证，请将以下代码片段插入到您的源文件开头（例如 index.js）。

``` js
if (!new class { x }().hasOwnProperty('x')) throw new Error('Transpiler is not configured correctly');
```

#### MobX在较旧的JavaScript环境中

默认情况下，MobX 使用 Proxy 以获得最佳性能和兼容性。然而，在一些较老的 JavaScript 引擎中并不支持 Proxy（可参考 Proxy
的支持情况）。例如，Internet Explorer（Edge 之前的版本）、Node.js 6 以下版本、iOS 10 以下、React Native 0.59 之前的 Android
设备，或者运行在 iOS 上的 Android 等环境都不支持 Proxy。

在这种情况下，MobX 可以退回到一个几乎完全相同的 ES5 兼容实现，不过在没有 Proxy 支持的情况下会有一些限制。你需要通过配置
useProxies，手动启用这种备用实现。

```js
import {configure} from "mobx"

configure({useProxies: "never"}) // Or "ifavailable".
```

此选项将在 MobX 7 中被移除。

#### MobX 在其他框架/平台上的应用

- MobX.dart：Flutter / Dart 的 MobX 实现
- lit-mobx：lit-element 的 MobX 实现
- mobx-angular：Angular 的 MobX 实现
- mobx-vue：Vue 的 MobX 实现

## MobX的精髓

### 概念

MobX 在你的应用程序中区分了以下三个概念：

1. 状态（State）
2. 动作（Actions）
3. 派生（Derivations）。

下面我们将详细介绍这些概念，或者你也可以参考 MobX 与 React 的 10 分钟入门教程，在那里你可以通过交互式的方式一步步深入了解这些概念，并构建一个简单的待办事项应用。

有些人可能会在下文描述的这些概念中发现“信号”（Signals）的影子。没错，MobX 本质上就是一个基于信号的状态管理库。

#### 1.定义状态(State)并使其可观察

`状态`是驱动应用程序的数据。通常会有与业务相关的状态，比如待办事项列表，也会有视图状态，比如当前选中的元素。

状态就像电子表格中的单元格，用来存储数值。你可以用任何你喜欢的数据结构来存储状态：普通对象、数组、类、循环数据结构或引用都可以。对于
MobX 的运行机制来说，这些都无关紧要。只需要确保所有你希望随时间变化的属性都被标记为可观察，这样 MobX 才能追踪它们。

下面是一个简单的例子：

```javascript
import {makeObservable, observable, action} from "mobx"

class Todo {
    id = Math.random()
    title = ""
    finished = false

    constructor(title) {
        makeObservable(this, {
            title: observable,
            finished: observable,
            toggle: action
        })
        this.title = title
    }

    toggle() {
        this.finished = !this.finished
    }
}
```

使用 `observable` 就像把对象的某个属性变成了电子表格中的一个单元格。但与电子表格不同的是，这些值不仅可以是基本类型，还可以是引用、对象或数组。

!!! note "无论你偏好使用类、普通对象还是装饰器，MobX 都能很好地支持多种编程风格。"

	这个例子其实可以用 makeAutoObservable 来简化，不过我们选择更为详细地写出来，是为了更好地展示不同的概念。需要注意的是，MobX 并不强制要求使用某种对象风格，你也可以直接用普通对象，或者用装饰器让类写得更简洁。更多细节可以参考相关页面。

那我们标记为 action 的 toggle 怎么办呢？

#### 2.使用操作(Actions)更新状态

动作是指任何会改变状态的代码片段，比如用户事件、后端数据推送、定时事件等。动作就像用户在电子表格单元格中输入新值一样。

在上面的 Todo 模型中，你可以看到我们有一个 toggle 方法，用于切换 finished 的值。finished 被标记为 observable。建议你将所有会修改
observable 的代码标记为 action。这样，MobX 就能自动应用事务处理，从而实现高效且无缝的性能优化。

使用 action 可以帮助你更好地组织代码，并防止在无意中修改状态。在 MobX 的术语中，那些会修改状态的方法被称为 `action`
。与之相对，view 则是基于当前状态计算出新的信息。每个方法最多只应承担这两种职责中的一种。

#### 3.创建能够自动响应状态变化的派生项(Derivations)

任何可以仅通过状态本身推导出来的信息，都属于派生内容。派生内容有多种形式：

- 用户界面
- 派生数据，比如剩余待办事项的数量
- 后端集成，例如将变更发送到服务器

MobX 区分了两种派生类型：

- 计算值：始终可以通过纯函数从当前的可观察状态中推导出来；
- 响应（反应）：当状态发生变化时，需要自动执行的副作用（在命令式编程和响应式编程之间起到桥梁作用）。

在刚开始使用 MobX 时，很多人往往会过度使用 `reactions`。黄金法则是：如果你想基于当前状态创建一个值，始终优先使用 `computed`。

##### 使用计算(computed)得出的模型派生值

要创建一个计算属性，可以使用 JavaScript 的 getter 函数 get 来定义该属性，并通过 makeObservable 将其标记为计算属性。

``` javascript
import {makeObservable, observable, computed} from "mobx"

class TodoList {
    todos = []

    get unfinishedTodoCount() {
        return this.todos.filter(todo => !todo.finished).length
    }

    constructor(todos) {
        makeObservable(this, {
            todos: observable,
            unfinishedTodoCount: computed
        })
        this.todos = todos
    }
}
```

MobX 会确保在添加新的待办事项或修改某个待办事项的 finished 属性时，unfinishedTodoCount 会自动更新。

这些计算类似于在 MS Excel 等电子表格程序中的公式。它们会自动更新，但只会在需要时才进行，也就是说，只有在有内容关心其结果时才会更新。

##### 使用响应机制(reactions)建模副作用

为了让你作为用户能够在屏幕上看到状态或计算值的变化，系统需要通过某种响应机制来重新渲染部分界面。

Reactions 类似于计算属性，但它们不是生成信息，而是产生副作用，比如在控制台打印信息、发起网络请求、逐步更新 React 组件树以修补
DOM 等。

简而言之，反应式编程中的反应机制架起了反应式和命令式编程之间的桥梁。

目前，最常用的反应形式是 UI 组件。需要注意的是，副作用既可以由动作触发，也可以由反应触发。对于那些有明确、可追溯来源的副作用，比如在提交表单时发起网络请求，应该在相关的事件处理函数中显式触发。

##### 响应式React组件

如果你在使用 React，可以通过将组件用你在安装时选择的绑定包中的 `observer 函数`包裹起来，让组件具备响应式特性。在这个例子中，我们将使用更轻量的
`mobx-react-lite 包`。

```javascript
import * as React from "react"
import {render} from "react-dom"
import {observer} from "mobx-react-lite"

const TodoListView = observer(({todoList}) => (
    <div>
        <ul>
            {todoList.todos.map(todo => (
                <TodoView todo={todo} key={todo.id}/>
            ))}
        </ul>
        Tasks left: {todoList.unfinishedTodoCount}
    </div>
))

const TodoView = observer(({todo}) => (
    <li>
        <input type="checkbox" checked={todo.finished} onClick={() => todo.toggle()}/>
        {todo.title}
    </li>
))

const store = new TodoList([new Todo("Get Coffee"), new Todo("Write simpler code")])
render(<TodoListView todoList={store}/>, document.getElementById("root"))
```

observer 会将 React 组件转化为它们所渲染数据的派生项。在使用 MobX 时，并不存在所谓的“智能组件”或“傻瓜组件”。所有组件都会智能地进行渲染，但它们的定义方式却非常简单。MobX
只会确保组件在需要时自动重新渲染，且绝不会多渲染一次。

因此，在上面的例子中，onClick 事件处理函数会因为使用了 toggle 操作而强制对应的 TodoView
组件重新渲染，但只有当未完成任务的数量发生变化时，TodoListView
组件才会重新渲染。如果你移除了“剩余任务数”这一行（或者将其单独放到一个组件中），那么在勾选任务时，TodoListView 组件就不会再重新渲染了。

想了解更多关于 React 与 MobX 如何协作的信息，请查阅 React 集成部分。

##### 自定义反应

你很少会用到它们，但可以通过 autorun、reaction 或 when 等函数，根据你的具体需求来创建。例如，下面的 autorun 会在
unfinishedTodoCount 的数量发生变化时，每次都打印一条日志信息：

```javascript
// A function that automatically observes the state.
autorun(() => {
    console.log("Tasks left: " + todos.unfinishedTodoCount)
})
```

为什么每次 unfinishedTodoCount 发生变化时，都会打印一条新消息？答案就在这条经验法则中：

MobX 会对在被追踪函数执行过程中读取的任何现有可观察属性做出响应。

想了解更多关于 MobX 如何确定需要响应哪些可观察对象的信息，请参阅“理解响应式原理”部分。

### 原则

MobX 采用单向数据流，动作会改变状态，进而自动更新所有受影响的视图。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505141114740.png){ loading=lazy }
  <figcaption></figcaption>
</figure>

1. 所有派生值在状态发生变化时都会自动且原子地更新，因此你永远不会看到中间状态的值。
2. 默认情况下，所有派生值都是同步更新的。这意味着，比如说，在修改状态后，action 可以直接安全地读取计算属性的最新值。
3. 计算属性采用惰性更新机制。任何未被实际使用的计算属性都不会被更新，直到它被用于某个副作用（如
   I/O）时才会重新计算。如果某个视图不再被使用，它会被自动垃圾回收。
4. 所有计算属性都应该是纯函数，不应在内部修改状态。

想了解更多背景信息，可以查阅 MobX 的基本原理。

### 代码规范检查

如果你觉得难以适应 MobX 的思维模式，可以将其配置为非常严格，这样每当你偏离这些模式时，系统会在运行时发出警告。你可以查看 MobX 的代码规范检查部分。
