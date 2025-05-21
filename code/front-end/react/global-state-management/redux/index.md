# Redux

## 概述

### 什么是Redux

Redux 是一个使用叫作`actions的事件`去管理和更新应用状态的模式和工具库。 它以`集中式 Store`（centralized
store）的方式对整个应用中使用的状态进行集中管理，其规则确保状态只能以可预测的方式更新。

### 什么时候应该用Redux

- 在应用的大量地方，都存在大量的状态
- 应用状态会随着时间的推移而频繁更新
- 更新该状态的逻辑可能很复杂
- 中型和大型代码量的应用，很多人协同开发

### Redux库和工具

- `React-Redux`:Redux 可以结合任何 UI 框架一起使用，最常与 React。React-Redux是我们的官方库。它让 React 组件与 Redux
  有了交互，可以从 store 读取一些 state，可以通过 dispatch actions 来更新 store。
- `Redux Toolkit`:是我们推荐的编写 Redux 逻辑的方法。 它包含我们认为对于构建 Redux 应用程序必不可少的包和函数。 Redux
  Toolkit 构建在我们建议的最佳实践中，简化了大多数 Redux 任务，防止了常见错误，并使编写 Redux 应用程序变得更加容易。
- `Redux DevTools 拓展`:可以显示 Redux 存储中状态随时间变化的历史记录。这允许你有效地调试应用程序，包括使用强大的技术，如“时间旅行调试”。

## 入门案例

### 创建reducer函数

```js
// 定义应用程序的初始状态值
const initialState = {
    value: 0
};

// 创建一个“reducer”函数，用于决定当应用程序发生某些事情时，新的状态应该是什么
function counterReducer(state = initialState, action) {
    // reducer 通常会查看发生的 action 类型
    // 来决定如何更新状态
    switch (action.type) {
        case "counter/incremented":
            return {...state, value: state.value + 1};
        case "counter/decremented":
            return {...state, value: state.value - 1};
        default:
            // 如果 reducer 不关心这个 action 类型，
            // 则返回现有的状态，不做修改
            return state;
    }
}
```

### 创建store实例

```js
import {createStore} from "@reduxjs/toolkit";

// 使用 `createStore` 函数创建一个新的 Redux store，
// 并使用 `counterReducer` 作为更新逻辑
const store = createStore(counterReducer);
```

### 获取状态

```js
const state = store.getState();
console.log(state.value)
```

### 更新状态

```js
store.dispatch({type: "counter/incremented"});

store.dispatch({type: "counter/decremented"});
```

### 监听状态变化

```js
store.subscribe(() => {
    // 监听状态更新，更新后再次获取状态
    const state = store.getState();
});
```

## 数据流

- actions 会在用户交互如点击时被 dispatch
- store 通过执行 reducer 方法计算出一个新的 state
- UI 读取最新的 state 来展示最新的值

<figure markdown="span">
  ![](https://cn.redux.js.org/assets/images/ReduxDataFlowDiagram-49fa8c3968371d9ef6f2a1486bd40a26.gif){ loading=lazy }
  <figcaption>数据流示意图</figcaption>
</figure>

## 设计原则

### 单一数据源

应用程序的`全局状态`作为对象存储在单个 `store` 中。任何给定的数据片段都应仅存在于一个位置，而不是在许多位置重复。

这样，随着事物的变化，可以更轻松地调试和检查应用的状态，并集中需要与整个应用程序交互的逻辑。

### State是只读的

更改状态的唯一方法是 dispatch 一个 action，这是一个描述所发生事件的对象。

这样，UI 就不会意外覆盖数据，并且更容易跟踪发生状态更新的原因。由于 actions 是普通的 JS 对象，因此可以记录、序列化、存储这些操作，并在以后重放这些操作以进行调试或测试。

### 使用Reducer纯函数进行更改

若要指定如何基于 action 更新状态树，请编写 `reducer` 函数。Reducers 是纯函数，它接收旧 state 和 action，并返回新 state。与任何其他函数一样，你可以将 Reducer 拆分为较小的函数以帮助完成工作，或者为常见任务编写可重用的 Reducer。

## Reducers详解

### Reducers方法概述

reducer方法接收俩参数，`当前的 state` 和`一个描述发生了什么的action对象`。当 Redux 应用启动时，我们还没有任何状态，所以我们提供一个
`initialState` 作为该 reducer 的默认值。

你可以将 reducer 视为一个`事件监听器`，它根据接收到的 action（事件）类型处理事件。

!!! note

    "Reducer" 函数的名字来源是因为它和 Array.reduce() 函数使用的回调函数很类似。

根据 Action 的 type，我们要么需要返回一个`全新的对象`作为新的 state 的结果，要么返回现有的 state 对象（如果没有任何变化）。请注意，我们通过复制现有
state 并更新副本的方式来 `不可变地（immutably）`更新状态，而不是直接修改原始对象。

??? "代码示例"

    ``` js title="reducer.js"
    --8<-- "code/front-end/react/global-state-management/redux/example/reducer/reducer.js"
    ```

### Reducer遵循规则

- 仅使用 `state` 和 `action` 参数计算新的状态值
- 禁止直接修改 `state`。必须通过复制现有的 state 并对复制的值进行更改的方式来做 `不可变更新（immutable updates）`。
- 禁止任何异步逻辑、依赖随机值或导致其他`副作用`的代码

## Store详解

### Store概述

所有 Redux 应用的中心都是 store 。"store" 是保存应用程序的全局 state 的容器。

可以通过调用 Redux 库 `createStore` API 来创建一个 store 实例。

```js
// 通过 createStore 方法创建一个新的 Redux store，
// 使用 counterReducer 进行更新逻辑
const store = Redux.createStore(counterReducer)
```

## Dispatch和Actions

### Actions概述

```js
const addTodoAction = {
    type: 'todos/todoAdded',
    payload: 'Buy milk'
}
```

action 是一个具有 type 字段的普通 JavaScript 对象。`你可以将 action 视为描述应用程序中发生了什么的事件`.

Action 始终具有 `type 字段`，该字段的值是你提供的字符串，充当 action 的唯一名称。

比如`todos/todoAdded`。我们通常把那个类型的字符串写成`域/事件名称`，其中第一部分是这个 action 所属的特征或类别，第二部分是发生的具体事情。

### Dispatch概述

`更新 state 的唯一方法是调用 store.dispatch() 并传入一个 action 对象。`

我们需要通过创建描述所发生情况的 action 对象，并将其 dispatching 到 store 来响应用户输入。当我们调用
`store.dispatch(action)` 时，store 运行 `reducer` ，计算更新的状态，并执行订阅者来更新 UI。

## 获取状态详解

### 获取状态-getState()

通过`store.getState()` 方法从 Redux store 中获取最新状态。

### 订阅状态-subscribe()

Redux store 允许我们调用 `store.subscribe()` 方法，并传递一个订阅者回调函数，该函数将在每次更新 store 时调用。因此，我们可以将
render 函数作为订阅者传递，并且知道每次 store 更新时，我们都可以使用最新值更新 UI。

### 提取状态片段

#### 基于store.getState()

Selector 函数可以从 store 状态树中提取指定的片段。随着应用变得越来越大，会遇到应用程序的不同部分需要读取相同的数据，selector
可以避免重复这样的读取逻辑：

```js
/*
* 全局状态
* const initialState = {
    value: 2
};
* */

// selector函数
const selectCounterValue = state => state.value

const currentValue = selectCounterValue(store.getState())
console.log(currentValue)
// 2
```

#### 使用useSelector

`useSelector`使得 React 组件可以从 Redux store 中读取数据。

useSelector接收一个 `selector函数`。`selector函数`接收Redux store的`state`作为其参数，然后从 state 中取值并返回。

`useSelector 会自动订阅 Redux store！`任何时候 dispatch action，它都会立即再次调用对应的 selector 函数。
`如果 selector 返回的值与上次运行时相比发生了变化，useSelector 将强制组件使用新值重新渲染。`我们仅需要在组件中调用一次
`useSelector()` 即可。

```js
import {useSelector} from 'react-redux'

const selectTodos = state => state.todos

const todos = useSelector(selectTodos)

// 也可以对状态先进行过滤
const selectTotalCompletedTodos = state => {
    const completedTodos = state.todos.filter(todo => todo.completed)
    return completedTodos.length
}

const totalCompletedTodos = useSelector(selectTotalCompletedTodos)
```

### 数组渲染问题

组件读取Redux state其中数组，并将实际数组元素作为prop传递给子元素。虽然可行，但存在潜在的性能问题。

更改一个`数组元素`，整个数组会创建新的副本，从而导致所有数组元素都创建新的副本。

```js title="禁止直接修改 state。必须通过复制现有的 state 并对复制的值进行更改的方式来做 不可变更新（immutable updates）。"
export default function todosReducer(state = initialState, action) {
    switch (action.type) {
        case 'todos/todoAdded': {
            return [
                ...state,
                {
                    id: nextTodoId(state),
                    text: action.payload,
                    completed: false
                }
            ]
        }
        default:
            return state
    }
}
```

因此，无论何时任何数组元素被更新，整个Redux state数组中的元素都会被更新。这会导致所有子元素都重新渲染。

解决方案：父组件中只记录数组的id，然后将id传递给子元素。这样即使其中某个元素更新，但由id不变，其它子元素不会更新。

!!! note

    这里，接收数组必须使用shallowEqual，因为map每次都会创建新的副本

```js
import React from 'react'
import {useSelector, shallowEqual} from 'react-redux'
import TodoListItem from './TodoListItem'

// 只保留数组的id，而不是整个数组
const selectTodoIds = state => state.todos.map(todo => todo.id)

const TodoList = () => {
    const todoIds = useSelector(selectTodoIds, shallowEqual)

    const renderedListItems = todoIds.map(todoId => {
        //将数组元素的id传递给子组件，而不是整个数组元素
        return <TodoListItem key={todoId} id={todoId}/>
    })

    return <ul className="todo-list">{renderedListItems}</ul>
}
```

### useSelector渲染问题

`useSelector 使用严格的 === 来比较结果，因此只要 selector 函数返回的结果是新地址引用，组件就会重新渲染！`这意味着如果在
selector 中创建并返回新地址引用，那么每次 dispatch action 后组件都会被重新渲染，即使数据值确实没有改变。

类似于数组，使用map、filter等方法，都会产生新的引用，导致数组重新渲染。

#### 使用shallowEqual解决

React-Redux 有一个 shallowEqual 比较函数，我们可以使用它来检查数组 内部每一项 是否仍然相同。

```js
import React from 'react'
import {useSelector, shallowEqual} from 'react-redux'
import TodoListItem from './TodoListItem'

const selectTodoIds = state => state.todos.map(todo => todo.id)

const TodoList = () => {
    const todoIds = useSelector(selectTodoIds, shallowEqual)

    const renderedListItems = todoIds.map(todoId => {
        return <TodoListItem key={todoId} id={todoId}/>
    })

    return <ul className="todo-list">{renderedListItems}</ul>
}
```

#### 记忆化Selectors

安装 Reselect:

```shell
npm install reselect
```

```js title="todosSlice.js"
import {createSelector} from 'reselect'

// 省略 reducer

// 省略 action creators

export const selectTodoIds = createSelector(
    // 首先传入一个或更多的 input selector 函数：
    state => state.todos,
    // 然后，output selector 接收所有输入结果作为参数
    // 并返回最终结果值
    todos => todos.map(todo => todo.id)
)
```

```js
import React from 'react'
import {useSelector, shallowEqual} from 'react-redux'

import {selectTodoIds} from './todosSlice'
import TodoListItem from './TodoListItem'

const TodoList = () => {
    const todoIds = useSelector(selectTodoIds)

    const renderedListItems = todoIds.map(todoId => {
        return <TodoListItem key={todoId} id={todoId}/>
    })

    return <ul className="todo-list">{renderedListItems}</ul>
}
```

!!! note

    createSelector方法可以具有多个参数

    ```js
      import { createSelector } from 'reselect'
      import { StatusFilters } from '../filters/filtersSlice'
      
      // 省略其他代码
      
      export const selectFilteredTodos = createSelector(
        // 第一个 input selector：所有的 todo 列表
        state => state.todos,
        // 第二个 input selector：当前状态过滤器
        state => state.filters.status,
        // Output selector：接收两个值
        (todos, status) => {
          if (status === StatusFilters.All) {
            return todos
          }
      
          const completedStatus = status === StatusFilters.Completed
          // 根据过滤器返回未完成或已完成的 todo 列表
          return todos.filter(todo => todo.completed === completedStatus)
        }
      )
    ```
