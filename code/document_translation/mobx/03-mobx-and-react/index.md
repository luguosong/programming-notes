# MobX和React

## React集成

本文档介绍了如何手动为 React
组件添加观察功能。不过，如果你使用 [mobx-react-observer](https://github.com/christianalfoni/mobx-react-observer) 的
Babel/SWC 插件，就可以自动完成观察的处理，无需手动操作。即便如此，了解 MobX 观察机制是如何与 React
组件集成的，依然非常有价值，即使你已经在使用自动化方案。

```javascript
import {observer} from "mobx-react-lite" // Or "mobx-react".

const MyComponent = observer(props => ReactElement)
```

虽然 MobX 可以独立于 React
使用，但它们最常见的用法还是结合在一起。在[《MobX 要点》](/document_translation/mobx/01-introduction/#mobx_3)
中，你已经见识到了这种集成中最关键的部分：可以用来包裹 React 组件的 observer 高阶组件（HoC）。

observer 是由你在安装时选择的独立 React
绑定包提供的。在本例中，我们将使用更轻量级的 [mobx-react-lite](https://github.com/mobxjs/mobx/tree/main/packages/mobx-react-lite)
包。

```javascript
import React from "react"
import ReactDOM from "react-dom"
import {makeAutoObservable} from "mobx"
import {observer} from "mobx-react-lite"

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

// 用 `observer` 包裹的函数组件，
// 会在它之前使用过的任意可观察对象发生变化时自动响应更新。
const TimerView = observer(({timer}) => <span>Seconds passed: {timer.secondsPassed}</span>)

ReactDOM.render(<TimerView timer={myTimer}/>, document.body)

setInterval(() => {
    myTimer.increaseTimer()
}, 1000)
```

observer 高阶组件（HoC）会自动将 React 组件订阅到在渲染过程中使用到的任何可观察对象（observable）。因此，当相关的 observable
发生变化时，组件会自动重新渲染。同时，它也确保在没有相关变化时，组件不会重新渲染。也就是说，组件可以访问但未实际读取的
observable，不会触发组件的重新渲染。

实际上，这使得 MobX 应用在默认情况下就能实现非常出色的优化，通常无需额外编写代码来防止过度渲染。

对于 observer 来说，observable 如何传递到组件并不重要，关键在于它们是否被读取。即使是深层次地读取 observable 也是没问题的，像
`todos[0].author.displayName` 这样的复杂表达式也能直接使用。这让订阅机制比其他需要显式声明或预先计算数据依赖（比如使用
selector）的框架更加精准和高效。

### 本地状态与外部状态

状态的组织方式非常灵活，因为从技术角度来说，我们读取哪些 observable 或这些 observable 来源于哪里其实并不重要。下面的示例展示了在使用
observer 包裹组件时，如何采用不同的模式来使用外部和本地的 observable 状态。

#### 在observer组件中使用外部状态

=== "使用props"

	可观察对象可以作为属性传递给组件（如上例所示）：
	
	``` javascript
	import { observer } from "mobx-react-lite"
	
	const myTimer = new Timer() // 请参见上面的 Timer 定义。
	
	const TimerView = observer(({ timer }) => <span>Seconds passed: {timer.secondsPassed}</span>)
	
	// 将 myTimer 作为一个 prop 传递。
	ReactDOM.render(<TimerView timer={myTimer} />, document.body)
	```

=== "使用全局变量"

	由于我们获取可观察对象引用的方式无关紧要，因此可以直接使用外部作用域中的可观察对象（包括通过导入等方式获得的对象）：

	```javascript
	const myTimer = new Timer() // 请参见上面的 Timer 定义。
	
	// 没有使用 props，`myTimer` 直接从闭包中获取。
	const TimerView = observer(() => <span>Seconds passed: {myTimer.secondsPassed}</span>)
	
	ReactDOM.render(<TimerView />, document.body)
	```

	直接使用可观察对象（observable）效果很好，但由于这通常会引入模块状态，这种模式可能会让单元测试变得更加复杂。因此，`我们建议优先使用 React Context`。

=== "使用React context"

	React Context 是一种非常实用的机制，可以在整个子树中共享可观察对象。
	
	``` javascript
	import {observer} from 'mobx-react-lite'
	import {createContext, useContext} from "react"
	
	const TimerContext = createContext<Timer>()
	
	const TimerView = observer(() => {
	    // 从上下文中获取定时器。
	    const timer = useContext(TimerContext) // See the Timer definition above.
	    return (
	        <span>Seconds passed: {timer.secondsPassed}</span>
	    )
	})
	
	ReactDOM.render(
	    <TimerContext.Provider value={new Timer()}>
	        <TimerView />
	    </TimerContext.Provider>,
	    document.body
	)
	```
	
	请注意，我们并不建议将 Provider 的值替换为另一个不同的值。在使用 MobX 时，通常没有这种需求，因为共享的 observable 本身就可以被更新。

#### 在observer组件中使用本地可观察状态

由于 observer 所使用的 observable 可以来自任何地方，也可以是本地状态。同样，我们有多种选择可以实现这一点。

=== "`useState` with observable class"

	使用本地可观察状态最简单的方法，就是通过 useState 存储一个可观察类的引用。需要注意的是，由于我们通常并不打算替换这个引用，因此可以完全忽略 useState 返回的更新函数。

	``` javascript
	import { observer } from "mobx-react-lite"
	import { useState } from "react"
	
	const TimerView = observer(() => {
	    const [timer] = useState(() => new Timer()) // 请参见上面的 Timer 定义。
	    return <span>Seconds passed: {timer.secondsPassed}</span>
	})
	
	ReactDOM.render(<TimerView />, document.body)
	```

	如果你想像我们在最初的示例中那样自动更新计时器，可以按照常规的 React 方式使用 useEffect。

	``` javascript
	useEffect(() => {
	    const handle = setInterval(() => {
	        timer.increaseTimer()
	    }, 1000)
	    return () => {
	        clearInterval(handle)
	    }
	}, [timer])
	```

=== "`useState` with local observable object"

	如前所述，我们可以不使用类，直接创建可观察对象。为此，我们可以利用 observable。

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

=== "`useLocalObservable` hook"

	`const [store] = useState(() => observable({ /* something */})) `这种写法非常常见。为了让这种模式更简单，mobx-react-lite 包中提供了 `useLocalObservable` 这个 hook，因此可以将之前的示例简化为：

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

#### 你可能并不需要本地可观察状态

一般来说，我们建议不要过早地将 MobX 的 observable 用于本地组件状态，因为这在理论上可能会限制你使用 React Suspense
机制的一些功能。通常的经验法则是：当状态涉及在多个组件（包括子组件）之间共享的领域数据时，比如待办事项、用户、预订等，可以使用
MobX 的 observable。

仅用于捕捉 UI 状态（如加载状态、选中项等）的状态，建议使用 useState 钩子来管理，这样将来可以更好地利用 React 的 suspense 特性。

在 React 组件中使用可观察对象的优势主要体现在以下几种情况：1）数据结构较为复杂，2）包含计算属性，或 3）需要与其他观察者组件共享数据时。

### 始终在observer组件内部读取可观察对象

你可能会想，什么时候应该使用 observer？一般来说，所有会读取可观察数据的组件都应该加上 observer。

observer 只会增强你所装饰的那个组件，而不会影响它内部调用的其他组件。因此，通常你应该把所有组件都用 observer
包裹起来。别担心，这样做并不会影响性能。相反，使用更多的 observer 组件可以让渲染更加高效，因为更新会变得更加细粒度。

#### 提示：尽量在最后一刻再从对象中获取值

observer 最佳的使用方式是尽可能传递对象的引用，只有在需要将其渲染到 DOM 或底层组件的 observer 组件内部，才读取对象的属性。换句话说，observer
会在你`解引用`对象的属性时做出响应。

在上面的例子中，如果 TimerView 组件像下面这样定义，它将无法响应后续的变化，因为 .secondsPassed
并不是在观察者组件内部读取的，而是在外部读取的，因此不会被追踪。

```javascript
const TimerView = observer(({secondsPassed}) => <span>Seconds passed: {secondsPassed}</span>)

React.render(<TimerView secondsPassed={myTimer.secondsPassed}/>, document.body)
```

请注意，这种思维方式与其他库（如
react-redux）有所不同。在那些库中，建议尽早解引用并向下传递原始值，以更好地利用记忆化优化。如果你对这个问题还不是很清楚，建议查阅[理解响应式](https://mobx.js.org/understanding-reactivity.html)
这一章节。

#### 不要把可观察对象传递给不是observer的组件

用 observer 包裹的组件只会订阅在其自身渲染过程中用到的 observable。如果你把 observable 的对象、数组或 Map 传递给`子组件`
，那么这些`子组件也需要用 observer 包裹`。同样，这一点也适用于基于回调的组件。

如果你想将 observables 传递给一个不是 observer 的组件，无论是因为它是第三方组件，还是你希望让该组件与 MobX
解耦，那么在传递之前，你需要先把 observables 转换为普通的 JavaScript 值或结构。

为了进一步说明上述内容，请看以下示例：一个可观察的 todo 对象、一个 TodoView 组件（观察者），以及一个假想的 GridRow
组件，它接收一个列/值映射，但本身不是观察者。

``` javascript
class Todo {
    title = "test"
    done = true

    constructor() {
        makeAutoObservable(this)
    }
}

const TodoView = observer(({ todo }: { todo: Todo }) =>
    // 错误：由于 GridRow 不是 observer，
	// 无法感知 todo.title 或 todo.done 的变化。
   return <GridRow data={todo} />

   // 正确做法：让 `TodoView` 监听 `todo` 的相关变化，并向下传递原始数据。
   return <GridRow data={{
       title: todo.title,
       done: todo.done
   }} />

   // 正确：使用 `toJS` 也可以，但通常更推荐显式地写出来。
   return <GridRow data={toJS(todo)} />
)
```

#### 回调组件可能需要使用`<Observer>`

想象一下同样的例子，这次 GridRow 接收的是一个 onRender 回调。由于 onRender 是在 GridRow 的渲染周期内执行的，而不是在
TodoView 的 render 里（尽管它在语法上看起来是在那里），我们必须确保回调组件本身是一个 observer
组件。或者，我们也可以直接用 <Observer /> 创建一个内联的匿名 observer。

``` javascript
const TodoView = observer(({todo}: { todo: Todo }) => {
    // 错误：GridRow.onRender 不会捕捉到 todo.title 或 todo.done 的变化 
    // 因为它不是一个 observer。
    return <GridRow onRender={() => <td>{todo.title}</td>}/>

    // 正确做法：将回调渲染包裹在 Observer 中，以便能够检测到变化。
    return <GridRow onRender={() => <Observer>{() => <td>{todo.title}</td>}</Observer>}/>
})
```

### 提示

#### 服务器端渲染 (SSR)

如果在服务端渲染环境中使用 observer，请确保调用 enableStaticRendering(true)，这样 observer 就不会订阅任何被使用的
observable，也不会引入垃圾回收相关的问题。

#### mobx-react与mobx-react-lite对比

在本篇文档中，我们默认使用了 mobx-react-lite。mobx-react 是它的`进阶版`，其底层同样依赖于 mobx-react-lite。mobx-react
额外提供了一些功能，但这些功能在全新项目中通常已经不再需要。mobx-react 主要多了以下几点：

1. 支持 React 类组件。
2. Provider 和 inject，这是 MobX 早期自带的 React.createContext 替代方案，现在已经不再需要。
3. 针对 observable 的特定 propTypes。需要注意的是，mobx-react 完全打包并重新导出了 mobx-react-lite，包括对函数组件的支持。

如果你使用 mobx-react，就无需再单独安装 mobx-react-lite 或从中导入任何内容。

#### observer or React.memo?

observer 会自动应用 memo，因此 observer 组件无需再用 memo 包裹。即使对 observer 组件使用 memo 也是安全的，因为如果 props
内部（无论多深）发生了相关变更，observer 依然能够检测到。

#### 用于基于类的React组件的observer

如上所述，类组件只在 mobx-react 中受支持，而不适用于 mobx-react-lite。简单来说，你可以像包装函数组件一样，用 observer 包装类组件。

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

请查阅 [mobx-react 文档](https://github.com/mobxjs/mobx/tree/main/packages/mobx-react#class-components)以获取更多信息。

#### 在React DevTools中显示友好的组件名称

React DevTools 利用组件的显示名称信息来正确展示组件层级结构。

如果你使用：

```javascript
export const MyComponent = observer(props => <div>hi</div>)
```

那么在开发者工具中将不会显示任何名称。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505192249513.png){ loading=lazy }
  <figcaption>DevTools</figcaption>
</figure>

可以通过以下方法来解决这个问题：

- 请使用具名函数而不是箭头函数。mobx-react 会根据函数名推断组件名称。

```javascript
export const MyComponent = observer(function MyComponent(props) {
    return <div>hi</div>
})
```

- 转译器（如 Babel 或 TypeScript）会根据变量名推断组件名称：

```javascript
const _MyComponent = props => <div>hi</div>
export const MyComponent = observer(_MyComponent)
```

- 再次根据变量名进行推断，使用默认导出：

```javascript
const MyComponent = props => <div>hi</div>
export default observer(MyComponent)
```

- 【已损坏】请显式设置 displayName：

```javascript
export const MyComponent = observer(props => <div>hi</div>)
MyComponent.displayName = "MyComponent"
```

在撰写本文时，React 16 存在此问题；mobx-react 的 observer 使用了 React.memo，因此会遇到这个 bug：https://github.com/facebook/react/issues/18026。不过，这个问题将在 React 17 中得到修复。


现在你可以看到组件名称了：

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505200940191.png){ loading=lazy }
  <figcaption>显示组件名称</figcaption>
</figure>

#### 在将observer与其他高阶组件结合使用时，应优先应用observer。

当 observer 需要与其他装饰器或高阶组件一起使用时，请确保 observer 是最内层（最先应用）的装饰器；否则它可能完全不起作用。

#### 从props派生计算属性

在某些情况下，你的本地可观察对象的计算值可能依赖于组件接收到的部分 props。然而，React 组件接收的 props 本身并不是可观察的，因此 props 的变化不会自动反映到任何计算值上。你需要手动更新本地的可观察状态，才能确保计算值能够基于最新的数据正确推导出来。

```javascript
import { observer, useLocalObservable } from "mobx-react-lite"
import { useEffect } from "react"

const TimerView = observer(({ offset = 0 }) => {
    const timer = useLocalObservable(() => ({
        offset, // 初始偏移值
        secondsPassed: 0,
        increaseTimer() {
            this.secondsPassed++
        },
        get offsetTime() {
            return this.secondsPassed - this.offset // 不是从 props 中获取的 'offset'！
        }
    }))

    useEffect(() => {
        // 将 'props' 中的 offset 同步到可观察对象 'timer' 中
        timer.offset = offset
    }, [offset])

    // 用于设置定时器，仅供演示使用。
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

在实际应用中，你很少会用到这种模式，因为直接返回 `<span>Seconds passed: {timer.secondsPassed offset}</span>` 这种方式更简单，尽管效率上略逊一筹。

#### useEffect和observables

useEffect 可以用来设置那些需要执行的副作用，并且这些副作用会随着 React 组件的生命周期而变化。使用 useEffect 时需要手动指定依赖项。而在 MobX 中，这一步其实并不必要，因为 MobX 已经有办法自动追踪副作用的依赖关系，那就是 autorun。将 autorun 与组件的生命周期结合起来，只需通过 useEffect 就能轻松实现：

```javascript
import { observer, useLocalObservable, useAsObservableSource } from "mobx-react-lite"
import { useState } from "react"

const TimerView = observer(() => {
    const timer = useLocalObservable(() => ({
        secondsPassed: 0,
        increaseTimer() {
            this.secondsPassed++
        }
    }))

    // 在可观察变化发生时触发的效果。
    useEffect(
        () =>
            autorun(() => {
                if (timer.secondsPassed > 60) alert("Still there. It's a minute already?!!")
            }),
        []
    )

    // 此功能用于设置定时器，仅供演示使用。
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

请注意，我们在 effect 函数中返回了由 autorun 创建的 disposer。这一点非常重要，因为这样可以确保在组件卸载时，autorun 能被正确清理！

依赖数组通常可以留空，除非你希望某个非可观察的值触发 autorun 重新运行，这种情况下你需要把它加进去。为了让你的代码检查工具不报错，你可以像上面例子那样把 timer 作为依赖项。这么做是安全的，也不会产生其他影响，因为这个引用实际上不会发生变化。

如果你希望明确指定哪些可观察对象会触发该副作用，可以使用 reaction 替代 autorun，除此之外，其用法模式保持一致。

### 故障排查

救命！我的组件没有重新渲染……

1. 确认你没有忘记加 observer（没错，这就是最常见的错误）。
2. 检查你想要响应的数据是否真的被设置为可观察。必要时可以用 isObservable、isObservableProp 这类工具在运行时验证一下。
3. 查看浏览器控制台日志，看看有没有警告或报错信息。
4. 确保你理解响应式追踪的基本原理。可以参考“理解响应式”这一节。
5. 仔细阅读上面提到的常见陷阱。
6. 配置 MobX，让它在你用法不当时给出警告，并检查控制台日志。
7. 使用 trace 来确认你是否订阅了正确的数据，或者用 spy / mobx-log 包来观察 MobX 的具体行为。


## 优化React组件渲染🚀

MobX 的性能非常出色，很多时候甚至比 Redux 还要快。不过，以下这些建议可以帮助你更好地发挥 React 和 MobX 的优势。其实，大多数建议对 React 本身也适用，并不仅限于 MobX。需要注意的是，虽然了解这些模式很有帮助，但通常情况下，即使你完全不考虑这些问题，你的应用性能也已经足够优秀了。

只有在性能真的成为问题时，才需要优先考虑优化！

### 多用小型组件

observer 组件会追踪它们所使用的所有值，并在其中任意一个值发生变化时重新渲染。因此，组件划分得越小，每次需要重新渲染的部分就越少。这意味着，界面中的更多部分可以彼此独立地进行渲染。

### 在专用组件中渲染列表

上述情况在渲染大型集合时尤为明显。React 在渲染大规模集合时表现不佳，因为每当集合发生变化时，调和器都需要重新评估集合中生成的组件。因此，建议将组件仅用于遍历并渲染集合内容，而不做其他渲染操作。

不好的做法：

```javascript
const MyComponent = observer(({ todos, user }) => (
    <div>
        {user.name}
        <ul>
            {todos.map(todo => (
                <TodoView todo={todo} key={todo.id} />
            ))}
        </ul>
    </div>
))
```

在上面的代码中，当 user.name 发生变化时，React 会不必要地对所有的 TodoView 组件进行协调（reconcile）。虽然这些组件不会重新渲染，但协调过程本身也是非常耗费性能的。

好的做法：

```javascript
const MyComponent = observer(({ todos, user }) => (
    <div>
        {user.name}
        <TodosView todos={todos} />
    </div>
))

const TodosView = observer(({ todos }) => (
    <ul>
        {todos.map(todo => (
            <TodoView todo={todo} key={todo.id} />
        ))}
    </ul>
))
```

### 不要使用数组索引作为键

不要使用数组索引或任何将来可能会变化的值作为 key。如果需要，请为你的对象生成唯一的 id。可以参考这篇[博客文章](https://robinpokorny.medium.com/index-as-a-key-is-an-anti-pattern-e0349aece318)。

### 延迟解引用值

在使用 mobx-react 时，建议尽可能晚地解引用值。这样做的原因是，MobX 会自动重新渲染那些解引用了可观察值的组件。如果在组件树的更深层级进行解引用，就会有更少的组件需要重新渲染。

```javascript
// 慢
<DisplayName name={person.name} />

// 快
<DisplayName person={person} />
```

在更快的示例中，name 属性的变化只会导致 DisplayName 组件重新渲染，而在较慢的示例中，组件的父级也需要重新渲染。这其实没有什么问题，如果父组件的渲染速度足够快（通常都是这样！），这种做法也是可行的。

### 函数属性

你可能会注意到，为了在后期解引用这些值，你需要创建许多小型的观察者组件，每个组件都针对数据的不同部分进行定制渲染，例如：

```javascript
const PersonNameDisplayer = observer(({ person }) => <DisplayName name={person.name} />)

const CarNameDisplayer = observer(({ car }) => <DisplayName name={car.model} />)

const ManufacturerNameDisplayer = observer(({ car }) => 
    <DisplayName name={car.manufacturer.name} />
)
```

如果你有大量不同结构的数据，这种做法很快就会变得繁琐。另一种方法是使用一个函数，让它返回你希望 *Displayer 渲染的数据：

```javascript
const GenericNameDisplayer = observer(({ getName }) => <DisplayName name={getName()} />)
```

然后，你可以这样使用这个组件：

```javascript
const MyComponent = ({ person, car }) => (
    <>
        <GenericNameDisplayer getName={() => person.name} />
        <GenericNameDisplayer getName={() => car.model} />
        <GenericNameDisplayer getName={() => car.manufacturer.name} />
    </>
)
```

这种方法可以让 GenericNameDisplayer 在你的应用中被灵活复用来渲染任意名称，同时还能最大程度地减少组件的重新渲染。
