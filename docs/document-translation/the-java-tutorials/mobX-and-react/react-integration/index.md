# React 集成

本文档说明了如何手动为 React 组件应用 `observation`（观察）。不过，借助 `mobx-react-observer` Babel/SWC
插件，你可以自动处理观察机制，无需手动介入。即便使用了自动化方案，理解 MobX 的观察机制如何与 React 组件集成，依然很有价值。

```javascript
import {observer} from "mobx-react-lite" // Or "mobx-react".

const MyComponent = observer(props => ReactElement)
```

虽然 **MobX** 可以脱离 **React** 独立工作，但它们最常见的用法还是搭配在一起。你在《MobX 精要》里已经看到这种集成里最关键的一环：
`observer` 这个高阶组件（HOC），可以用来包裹一个 React 组件。

`observer` 由一个单独的 React 绑定（bindings）包提供，你会在安装时自行选择。这个例子里，我们将使用更轻量的 `mobx-react-lite`
包。

``` javascript
import React from "react"
import ReactDOM from "react-dom"
import { makeAutoObservable } from "mobx"
import { observer } from "mobx-react-lite"

class Timer {
    secondsPassed = 0

    constructor() {
        makeAutoObservable(this)
    }

    increaseTimer() {
        this.secondsPassed += 1
    }
}

const myTimer = new Timer()

// 由 `observer` 包裹的函数组件，
// 会对其此前使用过的可观察对象 (observable) 在后续发生的任何变化作出响应。
const TimerView = observer(({ timer }) => <span>Seconds passed: {timer.secondsPassed}</span>)

ReactDOM.render(<TimerView timer={myTimer} />, document.body)

setInterval(() => {
    myTimer.increaseTimer()
}, 1000)
```

!!! note

	你可以在 CodeSandbox 上亲自体验并修改上面的示例。

`observer` HoC 会在渲染过程中，自动让 React 组件订阅所有被使用到的 `observable`。因此，当相关 `observable`
发生变化时，组件会自动重新渲染。同时，它也会确保在没有相关变化时组件不会重新渲染。也就是说，组件虽然能访问到某些 `observable`
，但如果实际并未读取，它们就永远不会触发重新渲染。

在实际项目中，这让 MobX 应用默认就具备很好的性能优化，通常不需要额外编写代码来防止过度渲染。

要让 `observer` 生效，`observable` 是通过什么方式进入组件的并不重要，关键在于它们被读取了即可。深度读取 `observable`
也完全没问题，像 `todos[0].author.displayName` 这样的复杂表达式同样开箱即用。与其他框架相比（那些框架往往需要显式声明数据依赖，或提前预计算，例如
`selector`），这使得订阅机制更精确、更高效。

## 本地与外部状态

状态的组织方式具有很大的灵活性，因为（从技术角度来说）我们读取哪些 `observable`，以及这些 `observable`
源自何处，并不重要。下面的示例展示了多种模式，说明在使用 `observer` 包裹的组件中，如何同时使用外部与本地的 `observable` 状态。

### 在观察者组件中使用外部状态

#### 使用props

`Observable` 可以像上面的示例那样，通过 `props` 传递给组件：

``` javascript title="使用props"

import { observer } from "mobx-react-lite"

const myTimer = new Timer() // 请参阅上面的 `Timer` 定义。

const TimerView = observer(({ timer }) => <span>Seconds passed: {timer.secondsPassed}</span>)

// 将 `myTimer` 作为一个 prop 传入。
ReactDOM.render(<TimerView timer={myTimer} />, document.body)
```

#### 使用全局变量

由于我们是如何获得 `observable` 引用的并不重要，因此可以直接消费外层作用域中的 `observable`（包括从 `import` 等引入的）：

``` javascript title="使用全局变量"

const myTimer = new Timer() // 参见上面的 `Timer` 定义。

// 不需要传入 props，`myTimer` 直接从闭包 (closure) 中获取并使用。
const TimerView = observer(() => <span>Seconds passed: {myTimer.secondsPassed}</span>)

ReactDOM.render(<TimerView />, document.body)
```

!!! warning

	直接使用 `observables` 的效果很好，但由于这通常会引入模块状态，这种模式可能会让单元测试更复杂。因此，我们建议改用 React `Context`。

#### 使用 React context

`React Context` 是一种非常出色的机制，可以在整个子树范围内共享可观察对象 (observables)：

``` javascript title="使用 React context"
import {observer} from 'mobx-react-lite'
import {createContext, useContext} from "react"

const TimerContext = createContext < Timer > ()

const TimerView = observer(() => {
    // Grab the timer from the context.
    const timer = useContext(TimerContext) // See the Timer definition above.
    return (
        <span>Seconds passed: {timer.secondsPassed}</span>
    )
})

ReactDOM.render(
    <TimerContext.Provider value={new Timer()}>
        <TimerView/>
    </TimerContext.Provider>,
    document.body
)
```

!!! note

	请注意，我们不建议在任何情况下把 `Provider` 的值替换成另一个。使用 MobX 时通常也没有这个必要，因为共享的 `observable` 本身就可以直接更新。

### 在observer组件中使用本地可观察状态 (observable state)

由于 `observer` 使用的可观察对象 (observables) 可以来自任何地方，因此它们也可以是本地状态。同样，我们也有多种不同的选择。

#### useState与可观察类

使用本地可观察状态最简单的方式，是通过 `useState` 保存一个可观察类 (observable class) 的引用。注意，由于我们通常不希望替换这个引用，因此会完全忽略
`useState` 返回的更新函数：

``` javascript
import { observer } from "mobx-react-lite"
import { useState } from "react"

const TimerView = observer(() => {
    const [timer] = useState(() => new Timer()) // 参见上面的 `Timer` 定义。
    return <span>Seconds passed: {timer.secondsPassed}</span>
})

ReactDOM.render(<TimerView />, document.body)
```

如果你想像原始示例中那样自动更新计时器，可以按照 React 的常见用法使用 `useEffect`：

```javascript
useEffect(() => {
    const handle = setInterval(() => {
        timer.increaseTimer()
    }, 1000)
    return () => {
        clearInterval(handle)
    }
}, [timer])
```

#### useState与本地可观察对象

如前所述，我们不一定要用类，也可以直接创建可观察对象。为此，我们可以利用 `observable`：

``` javascript
import { observer } from "mobx-react-lite"
import { observable } from "mobx"
import { useState } from "react"

const TimerView = observer(() => {
    const [timer] = useState(() =>
        observable({
            secondsPassed: 0,
            increaseTimer() {
                this.secondsPassed++
            }
        })
    )
    return <span>Seconds passed: {timer.secondsPassed}</span>
})

ReactDOM.render(<TimerView />, document.body)
```

#### useLocalObservable hook

`const [store] = useState(() => observable({ /* something */}))` 这种组合用法非常常见。为了简化这种模式，mobx-react-lite
包提供了 `useLocalObservable` Hook，使得可以把前面的示例简化为：

``` javascript
import { observer, useLocalObservable } from "mobx-react-lite"

const TimerView = observer(() => {
    const timer = useLocalObservable(() => ({
        secondsPassed: 0,
        increaseTimer() {
            this.secondsPassed++
        }
    }))
    return <span>Seconds passed: {timer.secondsPassed}</span>
})

ReactDOM.render(<TimerView />, document.body)
```

### 你可能并不需要本地observable状态

一般来说，我们建议不要太快就把本地组件状态交给 `MobX` 的 `observable`，因为从理论上讲，这可能会让你无法使用 `React` 的
`Suspense` 机制中的某些特性。经验法则是：当状态承载的是在组件之间（包括子组件）共享的领域数据时，再使用 `MobX` 的
`observable`，比如待办事项、用户、预订等。

只用于描述 `UI` 状态的状态，比如加载状态、选择项等，可能更适合使用 `useState` hook，因为这样未来就能更好地利用 `React` 的
`Suspense` 特性。

在 `React` 组件内部使用 `observable`，只要满足以下任一条件就很有价值：

1. 结构很深
2. 包含计算值 (computed values)
3. 会与其他 `observer` 组件共享

## 始终在observer组件中读取observable

你可能会想：什么时候该用 `observer`？经验法则是：凡是读取 `observable` 数据的组件，都应该应用 `observer`。

`observer` 只会增强你正在装饰的那个组件，而不会增强它所调用的组件。因此通常你的组件都应该用 `observer` 包起来。别担心，这并不低效。恰恰相反，
`observer` 组件越多，渲染反而越高效，因为更新会变得更细粒度。

### 小贴士：尽可能晚地从对象中取值

`observer` 的最佳实践是：尽可能长时间地传递对象引用，并且只在最终要把它们渲染到 `DOM` / 底层组件 (low-level components)
的、基于 `observer` 的组件中才读取对象属性。换句话说，`observer` 响应的是你从对象中“解引用 (dereference)”出某个值这一行为。

在上面的例子中，如果 `TimerView` 组件像下面这样定义，那么它将不会对后续变化作出响应，因为 `.secondsPassed` 并不是在
`observer` 组件内部读取的，而是在外部读取的，因此不会被追踪：

``` javascript
const TimerView = observer(({ secondsPassed }) => <span>Seconds passed: {secondsPassed}</span>)

React.render(<TimerView secondsPassed={myTimer.secondsPassed} />, document.body)
```

请注意，这里的思维方式与 react-redux 等其他库不同：在那些库中，尽早解引用并向下传递基础类型（primitives）是一种良好实践，因为这样可以更好地利用 `memoization`（记忆化）。如果你对此还不够清楚，务必查看“理解响应性（Understanding reactivity）”一节。

### 不要把observables传给不是observer的组件

被 `observer` 包裹的组件，只会订阅该组件**自身渲染过程中**用到的 `observables`。因此，如果将可观察对象 / 数组 / `maps` 作为 props 传给子组件，那么这些子组件也必须同样用 `observer` 包裹。任何基于回调（callback）的组件也同样如此。

如果你想把 `observables` 传给一个不是 `observer` 的组件——无论是因为它是第三方组件，还是因为你希望该组件与 MobX 解耦——那么在传递之前，就必须先把 `observables` 转换为普通的 JavaScript 值或结构。

为进一步说明这一点，请看下面的示例：一个可观察的 todo 对象、一个 `TodoView` 组件（`observer`），以及一个假想的 `GridRow` 组件。`GridRow` 接收“列 / 值”的映射，但它本身不是 `observer`：

```javascript
class Todo {
    title = "test"
    done = true

    constructor() {
        makeAutoObservable(this)
    }
}

const TodoView = observer(({ todo }: { todo: Todo }) =>
    // 错误：`GridRow` 不会捕获 `todo.title` / `todo.done` 的变化
    //      因为它不是观察者 (observer)。
   return <GridRow data={todo} />

   // 正确：让 `TodoView` 检测 `todo` 中的相关变化，
   //          并将纯数据向下传递。
   return <GridRow data={{
       title: todo.title,
       done: todo.done
   }} />

   // 正确：使用 `toJS` 也可以，但通常还是明确表达更好。
   return <GridRow data={toJS(todo)} />
)
```

### 回调组件可能需要<Observer>

设想同样的例子，只是这次 `GridRow` 接收的是一个 `onRender` 回调。由于 `onRender` 属于 `GridRow` 的渲染周期，而不是 `TodoView` 的 `render`（尽管从语法上看它写在那里面），我们必须确保这个回调组件使用了 `observer` 组件。或者，我们也可以通过 `<Observer />` 创建一个内联的匿名 `observer`：

``` javascript
const TodoView = observer(({ todo }: { todo: Todo }) => {
    // 错误：`GridRow.onRender` 不会捕捉到 `todo.title` / `todo.done` 的变化，
    //        因为它不是观察者 (observer)。
    return <GridRow onRender={() => <td>{todo.title}</td>} />

    // `正确`：将回调渲染包裹在 `Observer` 中，以便能够检测到变化。
    return <GridRow onRender={() => <Observer>{() => <td>{todo.title}</td>}</Observer>} />
})
```

## 提示

### 服务器端渲染

如果在服务端渲染（Server-Side Rendering，SSR）场景中使用 `observer`，务必调用 `enableStaticRendering(true)`，这样 `observer` 就不会订阅任何用到的可观察对象（Observable），也就不会引入垃圾回收（Garbage Collection，GC）方面的问题。

### mobx-react vs. mobx-react-lite

在这份文档中，我们默认使用 `mobx-react-lite`。`mobx-react` 可以理解为它的“大哥”，底层同样基于 `mobx-react-lite`。它额外提供了一些功能，但在新项目（greenfield projects）里通常已经不太需要了。`mobx-react` 额外提供的内容包括：

1. 支持 React `class` 组件。
2. `Provider` 和 `inject`。这是 MobX 早期用于替代 `React.createContext` 的方案，如今已经不再需要。
3. 面向 `observable` 的特定 `propTypes`。

需要注意的是，`mobx-react` 会完整打包并重新导出 `mobx-react-lite`，也包括对函数组件的支持。因此，如果你使用 `mobx-react`，就不必再额外把 `mobx-react-lite` 加为依赖，也无需在任何地方从它导入。

### observer 还是 React.memo

`observer` 会自动应用 `memo`，因此 `observer` 组件永远不需要再额外用 `memo` 包一层。对 `observer` 组件使用 `memo` 也是安全的，因为只要与渲染相关，即使是在 `props` 内部（深层）的变更，`observer` 也会照样捕获并响应。

### 适用于基于类的React组件的observer

如上所述，`mobx-react-lite` 并不支持基于类的组件，只有通过 `mobx-react` 才能支持。简而言之，你可以像包装函数组件那样，用 `observer` 包装基于类的组件：

``` javascript
import React from "React"

const TimerView = observer(
    class TimerView extends React.Component {
        render() {
            const { timer } = this.props
            return <span>Seconds passed: {timer.secondsPassed} </span>
        }
    }
)
```

查看 `mobx-react` [文档](https://github.com/mobxjs/mobx/tree/main/packages/mobx-react#class-components)了解更多信息。

### React DevTools中好看的组件名称

`React DevTools` 会使用组件的 `显示名称 (display name)` 信息，以便正确显示 `组件层级结构 (component hierarchy)`。

如果你使用：

```javascript
export const MyComponent = observer(props => <div>hi</div>)
```

那么在 DevTools 中就不会显示任何 `display name`。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260129100229770.png){ loading=lazy }
  <figcaption></figcaption>
</figure>

可以用以下方法来修复这个问题：

- 使用带名称的函数，替代箭头函数。`mobx-react` 会根据函数名推断组件名称：

``` javascript
export const MyComponent = observer(function MyComponent(props) {
    return <div>hi</div>
})
```

- 转译器（比如 Babel 或 TypeScript）会从变量名中推断出组件名称：

``` javascript
const _MyComponent = props => <div>hi</div>
export const MyComponent = observer(_MyComponent)
```

- 再次从变量名推断，使用 `default export`：

``` javascript
const MyComponent = props => <div>hi</div>
export default observer(MyComponent)
```

- [破坏性方法] 显式的声明 displayName：

``` javascript
export const MyComponent = observer(props => <div>hi</div>)
MyComponent.displayName = "MyComponent"
```

截至撰写时，这在 React 16 中是坏的；mobx-react 的 `observer` 使用了 `React.memo`，因此触发了这个 bug：[https://github.com/facebook/react/issues/18026](https://github.com/facebook/react/issues/18026)，不过它会在 React 17 中修复。  

现在你可以看到组件名称：

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260129100511200.png){ loading=lazy }
  <figcaption></figcaption>
</figure>

### 将observer与其他高阶组件组合时，先应用observer

当需要把 `observer` 与其他装饰器或高阶组件一起使用时，请确保 `observer` 是最内层（最先应用）的装饰器；否则它可能完全不起作用。

### 从props派生computed

在某些情况下，你的本地可观察对象 (observable) 的计算值 (computed) 可能依赖于组件接收到的某些 `props`。但 React 组件接收到的 `props` 集合本身并不是可观察的，因此 `props` 的变化不会体现在任何计算值中。你需要手动更新本地可观察状态，才能基于最新数据正确地派生出计算值。

``` javascript
import { observer, useLocalObservable } from "mobx-react-lite"
import { useEffect } from "react"

const TimerView = observer(({ offset = 0 }) => {
    const timer = useLocalObservable(() => ({
        offset, // The initial offset value
        secondsPassed: 0,
        increaseTimer() {
            this.secondsPassed++
        },
        get offsetTime() {
            return this.secondsPassed - this.offset // Not 'offset' from 'props'!
        }
    }))

    useEffect(() => {
        // Sync the offset from 'props' into the observable 'timer'
        timer.offset = offset
    }, [offset])

    // Effect to set up a timer, only for demo purposes.
    useEffect(() => {
        const handle = setInterval(timer.increaseTimer, 1000)
        return () => {
            clearInterval(handle)
        }
    }, [])

    return <span>Seconds passed: {timer.offsetTime}</span>
})

ReactDOM.render(<TimerView />, document.body)
```

在实际开发中，你很少会需要这种模式 (pattern)，因为 `return <span>Seconds passed: {timer.secondsPassed - offset}</span>` 是更简单的方案，尽管效率会略低一些。

### useEffect和可观察对象

`useEffect` 可用于设置需要发生的副作用 (side effects)，并将其与 `React` 组件的 `生命周期 (life-cycle)` 绑定。使用 `useEffect` 时需要指定 `依赖项 (dependencies)`。但在 `MobX` 中其实没这个必要，因为 `MobX` 已经提供了一种自动确定副作用 (effect) 依赖的方式：`autorun`。幸运的是，把 `autorun` 结合 `useEffect` 并将其与组件生命周期绑定起来非常直接：

``` javascript
import { observer, useLocalObservable, useAsObservableSource } from "mobx-react-lite"
import { useState } from "react"

const TimerView = observer(() => {
    const timer = useLocalObservable(() => ({
        secondsPassed: 0,
        increaseTimer() {
            this.secondsPassed++
        }
    }))

    // Effect that triggers upon observable changes.
    useEffect(
        () =>
            autorun(() => {
                if (timer.secondsPassed > 60) alert("Still there. It's a minute already?!!")
            }),
        []
    )

    // Effect to set up a timer, only for demo purposes.
    useEffect(() => {
        const handle = setInterval(timer.increaseTimer, 1000)
        return () => {
            clearInterval(handle)
        }
    }, [])

    return <span>Seconds passed: {timer.secondsPassed}</span>
})

ReactDOM.render(<TimerView />, document.body)
```

注意，我们会在 `effect 函数 (effect function)` 中返回由 `autorun` 创建的 `清理函数 (disposer)`。这一点非常关键，因为它能确保组件 `卸载 (unmount)` 后 `autorun` 会被正确清理！

`依赖数组 (dependency array)` 通常可以留空，除非你希望某个 `非可观察值 (non-observable value)` 触发 `autorun` 重新执行，这时就需要把它加进去。为了让 `代码检查器 (linter)` 顺利通过检查，你也可以把（上面示例里的）`timer` 写进依赖里。这么做是安全的，也不会带来额外影响，因为这个 `引用 (reference)` 实际上根本不会变化。

如果你更想显式指定哪些 `可观察值 (observable)` 会触发该 `effect`，可以用 `reaction` 替代 `autorun`，除此之外，这个模式完全一致。

## 故障排查 (Troubleshooting)

救命！我的组件怎么没有重新渲染……

1. 确认你没有忘记加 `observer`（没错，这是最常见的错误）。
2. 核实你希望触发响应的对象确实是可观察的 (observable)。如有需要，可在运行时使用 `isObservable`、`isObservableProp` 等工具来验证。
3. 查看浏览器控制台日志，看看是否有任何警告或错误。
4. 确保你真正理解追踪 (tracking) 机制的工作方式。可以阅读 `Understanding reactivity` 这一节。
5. 阅读上面提到的常见坑点。
6. 配置 MobX，让它在你以不可靠的方式使用相关机制时发出警告，并查看控制台日志。
7. 使用 `trace` 验证你是否订阅到了正确的内容，或通过 `spy` / `mobx-log` 包整体查看 MobX 的运行情况。
