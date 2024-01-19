---
layout: note
title: React
nav_order: 50
has_children: true
create_time: 2023/6/29
---

# 🏷️描述UI

React 是一个用于构建用户界面（UI）的 JavaScript 库，用户界面由按钮、文本和图像等小单元内容构建而成。React
帮助你把它们组合成`可重用`、`可嵌套`的组件。从 web 端网站到移动端应用，屏幕上的所有内容都可以被分解成组件。

# 定义组件

{% highlight react %}
{% include_relative src/views/describing-the-ui/defining-a-component/FCom.js %}
{% endhighlight %}

# 使用组件

{% highlight react %}
{% include_relative src/views/describing-the-ui/using-a-component/FCom.js %}
{% endhighlight %}

# 组件的导入与导出

导出组件：

```javascript
export default xxx
```

导入组件：

```javascript
import xxx from 'xxx'
```

# JSX

React组件使用一种被称为`JSX`的语法扩展来描述这些标签。JSX看起来和HTML很像，但它的语法更加严格并且可以动态展示信息。

JSX规则：

- 只能返回一个根元素
- 标签必须闭合
- 使用驼峰式命名法给~~所有~~大部分属性命名！
- 在 JSX 中通过`大括号`使用 JavaScript

# Props属性

每个父组件都可以提供 props 给它的子组件，从而将一些信息传递给它。

{% highlight react %}
{% include_relative src/views/describing-the-ui/passing-props-to-a-component/FCom.js %}
{% endhighlight %}

# 插槽

{% highlight react %}
{% include_relative src/views/describing-the-ui/slot/CCom.js %}
{% endhighlight %}

# 条件渲染

{% highlight react %}
{% include_relative src/views/describing-the-ui/conditional-rendering/FCom.js %}
{% endhighlight %}

# 列表渲染

使用`map`函数渲染列表。使用`filter`函数过滤列表。使用`key`属性来标识列表项。

{% highlight react %}
{% include_relative src/views/describing-the-ui/rendering-lists/FCom.js %}
{% endhighlight %}

# 🏷️添加交互

界面上的控件会根据用户的输入而更新。例如，点击按钮切换轮播图的展示。在 React 中，随时间变化的数据被称为状态（state）。你可以向任何组件添加状态，并按需进行更新。

# 响应事件

{% highlight react %}
{% include_relative src/views/adding-interactivity/responding-to-events/FCom.js %}
{% endhighlight %}

# state

{: .note-title}
> 普通变量和state的区别
>
> - `局部变量`无法在多次渲染中持久保存，当React再次渲染这个组件时，它会重新初始化这个变量。而`state`可以在多次渲染中持久保存。
> - 更改`局部变量`不会触发渲染。而更改`state`会触发渲染。

{% highlight react %}
{% include_relative src/views/adding-interactivity/state-a-components-memory/FCom.js %}
{% endhighlight %}

# 原理：渲染和提交

组件请求和提供UI的过程如下：

1. 触发一次渲染
    1. 组件的`初次渲染`。
    2. 组件（或者其祖先之一）的`状态`发生了改变。
2. 渲染组件
    1. 在进行初次渲染时, React 会调用根组件。
    2. 对于后续的渲染, React 会调用内部状态更新触发了渲染的函数组件。
    3. 如果当前组件包含子组件，会一直渲染到最底层的子组件。
3. 提交到DOM
    1. 对于初次渲染， React 会使用 `appendChild()` DOM API 将其创建的所有 DOM 节点放在屏幕上。
    2. 对于重渲染， React 将应用最少的必要操作（在渲染时计算！），以使得 DOM 与最新的渲染输出相互匹配。

{: .warning}
> React仅在渲染之间存在差异时才会`更改DOM`节点。如下面组件，当time属性变化时，并`不会更新input标签`：
>
> ```react
> export default function Clock({ time }) {
>   return (
>     <>
>       <h1>{time}</h1>
>       <input />
>     </>
>   );
> }
> ```

# state如同快照

当对状态进行修改时，状态并不会立刻改变，而是生成一个快照。等到下一次`渲染`时，才会将快照中的状态更新到组件中：

{% highlight react %}
{% include_relative src/views/adding-interactivity/state-as-a-snapshot/FCom.js %}
{% endhighlight %}

# state更新加入队列

在下次渲染之前多次更新同一个 state。

将`更新函数`传递给一个 state 设置函数时：

1. React 会将此函数加入队列，以便在对应处理函数中的所有其他代码运行后进行处理。
2. 在下一次渲染期间，React 会遍历队列并给你更新之后的最终 state。

当你在下次`渲染期间`调用 useState 时，React 会遍历队列。先获取`之前state的值`作为`参数 n `传递给第一个更新函数的值。然后
React 会获取你上一个更新函数的`返回值`，并将其作为 `n` 传递给下一个更新函数，以此类推。

{% highlight react %}
{% include_relative src/views/adding-interactivity/queueing-a-series-of-state-updates/FCom.js %}
{% endhighlight %}

# 更新对象类型state

当你想要更新一个对象时，你需要创建一个新的对象（或者将其拷贝一份），然后将 state 更新为此对象。

{% highlight react %}
{% include_relative src/views/adding-interactivity/updating-objects-in-state/FCom.js %}
{% endhighlight %}

# 更新数组类型state

{% highlight react %}
{% include_relative src/views/adding-interactivity/updating-arrays-in-state/FCom.js %}
{% endhighlight %}

# 🏷️状态管理

随着你的应用不断变大，更有意识的去关注应用状态如何组织，以及数据如何在组件之间流动会对你很有帮助。冗余或重复的状态往往是缺陷的根源。

# 声明式编程

命令式编程和声明式编程：

- `命令式编程`：必须去根据要发生的事情写一些明确的命令去操作UI
- `声明式编程`：只需要告诉React你想要的结果，React会自动帮你处理

{% highlight react %}
{% include_relative src/views/managing-state/reacting-to-input-with-state/FCom.js %}
{% endhighlight %}

# state构建原则

构建state的原则:

- 合并关联的 state
- 避免互相矛盾的 state
- 避免冗余的state:如果你能在渲染期间从组件的 props 或其现有的 state 变量中计算出一些信息，则不应将这些信息放入该组件的
  state 中。
- 避免重复的state
- 避免深度嵌套的 state

# 共享状态：状态提升

希望两个组件的状态始终同步更改,可以将相关 state 从这两个组件上移除，并把 state 放到它们的公共父级，再通过 props 将 state
传递给这两个组件。

{% highlight react %}
{% include_relative src/views/managing-state/sharing-state-between-components/FCom.js %}
{% endhighlight %}

# 受控组件

当组件中的重要信息是由`props`而不是其自身`状态`驱动时，就可以认为该组件是`受控组件`。这就允许父组件完全指定其行为。

`非受控组件`的特点是简单，不需要太多配置。

而`受控组件`的特点是灵活，需要父组件使用 props 对其进行配置。

{: .warning-title}
> 可信单一数据源
>
> 对于每个独特的状态，都应该存在且只存在于一个指定的组件中作为 state。

# 对state进行保留和重置

每个组件都有完全独立的state,互不影响。

- 组件停止渲染，state状态会被清除
- 相同位置的相同组件（比如只是属性不一样），状态会被保留
- 相同位置的不同组件，state状态会被清除(包含它下面的子组件的state也会被清理)

相同位置相同组件重置组件(特殊情况):

- 将组件渲染在不同的位置
- 使用 key 来重置 state

{: .note}
> `如果你想在重新渲染时保留 state，几次渲染中的树形结构就应该相互“匹配”`。结构不同就会导致 state 的销毁，因为 React
> 会在将一个组件从树中移除时销毁它的 state。

{% highlight react %}
{% include_relative src/views/managing-state/preserving-and-resetting-state/FCom.js %}
{% endhighlight %}

# Reducer

`Reducer`将组件的所有`状态更新`逻辑整合到一个外部函数中。

{% highlight react %}
{% include_relative src/views/managing-state/extracting-state-logic-into-a-reducer/FCom.js %}
{% endhighlight %}

# Context深层传递参数

通常来说，你会通过 props 将信息从父组件传递到子组件。但是，如果你必须通过许多中间组件向下传递 props，或是在你应用中的许多组件需要相同的信息，传递
props 会变的十分冗长和不便。`Context`允许父组件向其下层无论多深的任何组件提供信息，而无需通过 props 显式传递。

{% highlight react %}
{% include_relative src/views/managing-state/passing-data-deeply-with-context/FCom.js %}
{% endhighlight %}

# Reducer+Context

可以将Reducer和Context整合到一个文件中，方便管理：

{% highlight react %}
{% include_relative src/views/managing-state/scaling-up-with-reducer-and-context/CountProvider.js %}
{% endhighlight %}

然后将组件包裹在Provider中：

{% highlight react %}
{% include_relative src/views/managing-state/scaling-up-with-reducer-and-context/FCom.js %}
{% endhighlight %}

# 🏷️应急方案

有些组件可能需要控制和同步 React 之外的系统。例如，你可能需要使用浏览器 API 聚焦输入框，或者在没有 React
的情况下实现视频播放器，或者连接并监听远程服务器的消息。在本章中，你将学习到一些应急方案，让你可以“走出” React
并连接到外部系统。大多数应用逻辑和数据流不应该依赖这些功能。

# ref

当你希望组件记住某些信息，但又不想让这些信息`触发新的渲染`时，你可以使用`ref`。

与`state`类似，ref能在渲染之间保留，但是不会触发渲染。

ref和state的区别：

| ref                                                            | state                                                                                          |
|----------------------------------------------------------------|------------------------------------------------------------------------------------------------|
| `useRef(initialValue)`<br/>返回 <br/>`{ current: initialValue }` | `useState(initialValue)` <br/>返回 <br/>state 变量的`当前值`和一个 state`设置函数` <br/>`([value, setValue])` |
| 更改时不会触发重新渲染                                                    | 更改时触发重新渲染。                                                                                     |
| 可变 —— 你可以在渲染过程之外修改和更新 current 的值。                              | “不可变” —— 你必须使用 state 设置函数来修改 state 变量，从而排队重新渲染。                                                |
| 你不应在渲染期间读取（或写入） current 值。                                     | 你可以随时读取 state。但是，每次渲染都有自己不变的 state 快照。                                                         |

ref一般应用于不会影响组件外观的外部API,比如：

- 存储和操作 DOM 元素，将ref传递给JSX中的`ref属性`时，比如`<div ref={myRef}>`,React会将相应的`DOM元素`放入myRef.current
  中。当元素从 DOM中删除时，React会将myRef.current更新为`null`。
- 存储 timeout ID
- 存储不需要被用来计算 JSX 的其他对象。

{: .note}
> ⭐如果你的组件需要存储一些值，但不影响渲染逻辑，请选择ref。



{% highlight react %}
{% include_relative src/views/escape-hatches/referencing-values-with-refs/FCom.js %}
{% endhighlight %}

# ref处理列表

大部分情况使用state是可以完成相应需求的，

当面对一个数量不确定的列表时，按下面这种方式处理显然是不行的：

```react
<ul>
  {items.map((item) => {
    // 行不通！
    const ref = useRef(null);
    return <li ref={ref} />;
  })}
</ul>
```

❌这是因为Hook只能在组件的顶层被调用。不能在循环语句、条件语句或`map()`函数中调用`useRef`。

❌还有一种解决方案是用一个ref引用其父元素，然后用DOM操作方法如`querySelectorAll`
来寻找它的子节点。然而，这种方法很脆弱，如果DOM结构发生变化(列表数据是变化的，随时可能会变动)，可能会失效或报错。

正确的解决方案是将函数传递给ref属性。这称为`ref回调`。当需要设置 ref 时，React 将传入 DOM 节点来调用你的 ref
回调，并在需要清除它时传入 null 。这使你可以维护自己的数组或 Map，并通过其索引或某种类型的 ID 访问任何 ref：

{% highlight react %}
{% include_relative src/views/escape-hatches/manage-a-list-of-refs/FCom.js %}
{% endhighlight %}

{: .warning}
> 再次强调，大部分情况使用state都能解决问题。ref只是应急方案。比如需要直接操作DOM元素。

# ref访问另一个组件的DOM节点

React 不允许组件访问其他组件的 DOM 节点。甚至自己的子组件也不行！

需要通过`forwardRef`将一个组件指定的 ref`转发`给一个子组件。

{% highlight react %}
{% include_relative src/views/escape-hatches/ref-accessing-another-component/FCom.js %}
{% endhighlight %}

# ref添加时间

- `渲染阶段`:React调用你的组件来确定屏幕上应该显示什么
- `提交阶段`:React把变更应用于DOM。React在提交阶段设置`ref.current`。在更新DOM之前，React将受影响的 ref.current 值设置为null。更新DOM后，React立即将它们设置到相应的DOM节点。

在 React 中，state 更新是排队进行的。你可以强制 React 同步更新`（“刷新”）`DOM。为此，从react-dom导入`flushSync`并将state更新包裹 到`flushSync`调用中。

# Effect简介

Effect会在每次`渲染后`都会执行。

Effect通常用于暂时“跳出” React 代码并与一些`外部系统`进行同步。这包括`浏览器API、第三方小部件，以及网络`等等。如果你想用 Effect仅根据其他状态调整某些状态，那么 `你可能不需要Effect`。

```js
useEffect(() => {
  // 这里的代码会在每次渲染后执行(每次渲染都会执行)
});

useEffect(() => {
  // 这里的代码只会在组件挂载后执行(只执行一次)
}, []);

useEffect(() => {
  //这里的代码只会在每次渲染后，并且 a 或 b 的值与上次渲染不一致时执行
}, [a, b]);
```

{% highlight react %}
{% include_relative src/views/escape-hatches/synchronizing-with-effects/FCom.js %}
{% endhighlight %}

# Effect应用模式

- 控制非React组件

```javascript
/*
* 假设你要向页面添加地图组件，并且它有一个 setZoomLevel() 方法，你希望调整缩放级别（zoom level）并与 React 代码中的 zoomLevel state 变量保持同步。
* */
useEffect(() => {
  const map = mapRef.current
  map.setZoomLevel(zoomLevel)
}, [zoomLevel])
```

- 事件退订，如果Effect订阅了某些事件，清理函数应该退订这些事件：

```javascript
useEffect(() => {
  function handleScroll(e) {
    console.log(window.scrollX, window.scrollY);
  }
  window.addEventListener('scroll', handleScroll);
  return () => window.removeEventListener('scroll', handleScroll);
}, []);
```

- 触发动画：如果 Effect 对某些内容加入了动画，清理函数应将动画重置:

```javascript
useEffect(() => {
  const node = ref.current;
  node.style.opacity = 1; // 触发动画
  return () => {
    node.style.opacity = 0; // 重置为初始值
  };
}, []);
```

- 获取数据：如果 Effect 将会获取数据，清理函数应该要么 中止该数据获取操作，要么忽略其结果：

```javascript
useEffect(() => {
  let ignore = false;

  async function startFetching() {
    // 如果 userId 从 'Alice' 变为 'Bob'，那么请确保 'Alice' 响应数据被忽略，即使它在 'Bob' 之后到达。
    const json = await fetchTodos(userId);
    if (!ignore) {
      setTodos(json);
    }
  }

  startFetching();

  return () => {
    // 如果 userId 从 'Alice' 变为 'Bob'，那么请确保 'Alice' 响应数据被忽略，即使它在 'Bob' 之后到达。
    ignore = true;
  };
}, [userId]);
```

- 初始化应用时不需要使用Effect

验证登陆状态和加载本地程序数据。你可以将其放在组件之外。

这保证了这种逻辑在浏览器加载页面后只运行一次。

```javascript
if (typeof window !== 'undefined') { // 检查是否在浏览器中运行
  checkAuthToken();
  loadDataFromLocalStorage();
}

function App() {
  // ……
}
```

- 不要在Effect中执行购买商品一类的操作
  - 开发环境下，Effect 会执行两次，这意味着购买操作执行了两次。
  - 如果用户转到另一个页面，然后按“后退”按钮回到了这个界面。Effect 会随着组件再次挂载而再次执行。

# 移除不必要的Effect

- `你不必使用Effect来转换渲染所需的数据。`如果一个值可以基于现有的 props 或 state 计算得出，不要把它作为一个 state，而是在渲染期间直接计算这个值。
- `你不必使用Effect来处理用户事件。`

## 根据props或state来更新state

{: .note}
> 如果一个值可以基于现有的 props 或 state 计算得出，`不要把它作为一个 state`，而是在渲染期间直接计算这个值。

根据props或state来更新state：

```javascript
/*
* ❌错误做法，你不必使用Effect来转换渲染所需的数据。
* */
function Form() {
  const [firstName, setFirstName] = useState("Taylor")
  const [lastName, setLastName] = useState("Swift")

  // 🔴 避免：多余的 state 和不必要的 Effect
  const [fullName, setFullName] = useState("")
  useEffect(() => {
    setFullName(firstName + " " + lastName)
  }, [firstName, lastName])
  // ...
}

/*
* ✅正确做法
* */
function Form() {
  const [firstName, setFirstName] = useState("Taylor")
  const [lastName, setLastName] = useState("Swift")
  // ✅ 非常好：在渲染期间进行计算
  const fullName = firstName + " " + lastName
  // ...
}
```

## 缓存昂贵的计算

{: .warning-note}
> useMemo和useEffect的区别
> 
> - `useMemo`：在渲染期间执行，返回一个值，因此它只适用于`纯函数`场景。
> - `useEffect`：在渲染之后执行，它可以执行任何操作，因此它适用于`副作用`场景。

你可以使用 useMemo Hook 缓存（或者说 记忆（memoize））一个昂贵的计算：

```javascript
/*
* ❌错误做法，你不必使用Effect来转换渲染所需的数据。
* */
function TodoList({ todos, filter }) {
  const [newTodo, setNewTodo] = useState('');

  // 🔴 避免：多余的 state 和不必要的 Effect
  const [visibleTodos, setVisibleTodos] = useState([]);
  useEffect(() => {
    setVisibleTodos(getFilteredTodos(todos, filter));
  }, [todos, filter]);

  // ...
}

/*
* ❌这个做法也不推荐，如果getFilteredTodos耗时较长，会很低效
* */
function TodoList({ todos, filter }) {
  const [newTodo, setNewTodo] = useState('');
  // ✅ 如果 getFilteredTodos() 的耗时不长，这样写就可以了。
  const visibleTodos = getFilteredTodos(todos, filter);
  // ...
}

/*
* ✅正确做法
* */
function TodoList({ todos, filter }) {
  const [newTodo, setNewTodo] = useState('');
  const visibleTodos = useMemo(() => {
    // ✅ 除非 todos 或 filter 发生变化，否则不会重新执行
    return getFilteredTodos(todos, filter);
  }, [todos, filter]);
  // ...
}
```

## 当props变化时重置所有state

{: .note}
> 使用`key属性`对状态进行重置

```javascript
/*
* ❌这样做很低效，
* */
export default function ProfilePage({ userId }) {
  const [comment, setComment] = useState("")

  // 🔴 避免：当 prop 变化时，在 Effect 中重置 state
  useEffect(() => {
    setComment("")
  }, [userId])
  // ...
}

/*
* ✅正确做法
* */
export default function ProfilePage({ userId }) {
  return (
    <Profile
      userId={userId}
      key={userId}
    />
  )
}

function Profile({ userId }) {
  // ✅ 当 key 变化时，该组件内的 comment 或其他 state 会自动被重置
  const [comment, setComment] = useState("")
  // ...
}
```

## 当prop变化时调整部分state

key值的变化会将整个组件状态进行重置。

```javascript
/*
* ❌错误做法，使用useEffect重置部分state会让组件多次渲染
* */
function List({ items }) {
  const [isReverse, setIsReverse] = useState(false);
  const [selection, setSelection] = useState(null);

  // 🔴 避免：当 prop 变化时，在 Effect 中调整 state
  useEffect(() => {
    setSelection(null);
  }, [items]);
  // ...
}

/*
* ✅正确做法，直接在渲染期间（准确的说是组件被渲染前）调整state
* */
function List({ items }) {
  const [isReverse, setIsReverse] = useState(false);
  const [selection, setSelection] = useState(null);

  // 好一些：在渲染期间调整 state
  const [prevItems, setPrevItems] = useState(items);
  if (items !== prevItems) {
    setPrevItems(items);
    setSelection(null);
  }
  // ...
}
```

## 在事件处理函数中共享逻辑

```javascript
/*
* ❌错误做法
* */
function ProductPage({ product, addToCart }) {
  // 🔴 避免：在 Effect 中处理属于事件特定的逻辑
  useEffect(() => {
    if (product.isInCart) {
      showNotification(`已添加 ${product.name} 进购物车！`);
    }
  }, [product]);

  function handleBuyClick() {
    addToCart(product);
  }

  function handleCheckoutClick() {
    addToCart(product);
    navigateTo('/checkout');
  }
  // ...
}

/*
* ✅正确做法
* */
function ProductPage({ product, addToCart }) {
  // ✅ 非常好：事件特定的逻辑在事件处理函数中处理
  function buyProduct() {
    addToCart(product);
    showNotification(`已添加 ${product.name} 进购物车！`);
  }

  function handleBuyClick() {
    buyProduct();
  }

  function handleCheckoutClick() {
    buyProduct();
    navigateTo('/checkout');
  }
  // ...
}
```

## 发送POST请求

```javascript
/*
* ❌错误做法
* */
function Form() {
  const [firstName, setFirstName] = useState("")
  const [lastName, setLastName] = useState("")

  // ✅ 非常好：这个逻辑应该在组件显示时执行
  useEffect(() => {
    post("/analytics/event", { eventName: "visit_form" })
  }, [])

  // 🔴 避免：在 Effect 中处理属于事件特定的逻辑
  const [jsonToSubmit, setJsonToSubmit] = useState(null)

  // 发送到 /api/register并不是由表单 “显示” 时引起的。
  useEffect(() => {
    if (jsonToSubmit !== null) {
      post("/api/register", jsonToSubmit)
    }
  }, [jsonToSubmit])

  function handleSubmit(e) {
    e.preventDefault()
    setJsonToSubmit({ firstName, lastName })
  }

  // ...
}

/*
* ✅正确做法
* */
function Form() {
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');

  // ✅ 非常好：这个逻辑应该在组件显示时执行
  useEffect(() => {
    post('/analytics/event', { eventName: 'visit_form' });
  }, []);

  function handleSubmit(e) {
    e.preventDefault();
    // ✅ 非常好：事件特定的逻辑在事件处理函数中处理
    post('/api/register', { firstName, lastName });
  }
  // ...
}
```

## 链式计算

```javascript
/*
* ❌错误做法
* 1. 在链式的每个set调用之间，组件（及其子组件）都不得不重新渲染。非常低效
* 2. 无法回溯到之前的值
* */
function Game() {
  const [card, setCard] = useState(null)
  const [goldCardCount, setGoldCardCount] = useState(0)
  const [round, setRound] = useState(1)
  const [isGameOver, setIsGameOver] = useState(false)

  // 🔴 避免：链接多个 Effect 仅仅为了相互触发调整 state
  useEffect(() => {
    if (card !== null && card.gold) {
      setGoldCardCount(c => c + 1)
    }
  }, [card])

  useEffect(() => {
    if (goldCardCount > 3) {
      setRound(r => r + 1)
      setGoldCardCount(0)
    }
  }, [goldCardCount])

  useEffect(() => {
    if (round > 5) {
      setIsGameOver(true)
    }
  }, [round])

  useEffect(() => {
    alert("游戏结束！")
  }, [isGameOver])

  function handlePlaceCard(nextCard) {
    if (isGameOver) {
      throw Error("游戏已经结束了。")
    } else {
      setCard(nextCard)
    }
  }
}

/*
* ✅正确做法
*
* 尽可能在渲染期间进行计算，以及在事件处理函数中调整 state
* */
function Game() {
  const [card, setCard] = useState(null)
  const [goldCardCount, setGoldCardCount] = useState(0)
  const [round, setRound] = useState(1)

  // ✅ 尽可能在渲染期间进行计算
  const isGameOver = round > 5

  function handlePlaceCard(nextCard) {
    if (isGameOver) {
      throw Error("游戏已经结束了。")
    }

    // ✅ 在事件处理函数中计算剩下的所有 state
    setCard(nextCard)
    if (nextCard.gold) {
      if (goldCardCount <= 3) {
        setGoldCardCount(goldCardCount + 1)
      } else {
        setGoldCardCount(0)
        setRound(round + 1)
        if (round === 5) {
          alert("游戏结束！")
        }
      }
    }
  }
}
```

## 初始化应用

```javascript
/*
* ❌错误做法
*  开发环境会执行两次
*  使用Effect会在每次组件挂载时执行一次而不是应用加载时执行一次
* */
function App() {
  // 🔴 避免：把只需要执行一次的逻辑放在 Effect 中
  useEffect(() => {
    loadDataFromLocalStorage();
    checkAuthToken();
  }, []);
  // ...
}

/*
* ✅正确做法
* */
if (typeof window !== 'undefined') { // 检测我们是否在浏览器环境
  // ✅ 只在每次应用加载时执行一次
  checkAuthToken();
  loadDataFromLocalStorage();
}

function App() {
  // ...
}
```

## 通知父组件有关state变化的信息

```javascript
/*
* ❌错误做法
* 
* 过程分析：
* 1. Toggle 首先更新它的 state
* 2. Toggle 重新渲染
* 3. Effect 执行
* 4. onChange 函数被调用
* 5. 父组件更新它的 state
* 6. 父组件重新渲染
* 7. Toggle 重新渲染
* */
function Toggle({ onChange }) {
  const [isOn, setIsOn] = useState(false)

  // 🔴 避免：onChange 处理函数执行的时间太晚了
  useEffect(() => {
    onChange(isOn)
  }, [isOn, onChange])

  function handleClick() {
    setIsOn(!isOn)
  }

  function handleDragEnd(e) {
    if (isCloserToRightEdge(e)) {
      setIsOn(true)
    } else {
      setIsOn(false)
    }
  }

  // ...
}

/*
* ✅正确做法
* Toggle 组件及其父组件都在事件处理期间更新了各自的 state。React 会 批量 处理来自不同组件的更新，所以只会有一个渲染流程。
* */
function Toggle({ onChange }) {
  const [isOn, setIsOn] = useState(false)

  function updateToggle(nextIsOn) {
    // ✅ 非常好：在触发它们的事件中执行所有更新
    setIsOn(nextIsOn)
    onChange(nextIsOn)
  }

  function handleClick() {
    updateToggle(!isOn)
  }

  function handleDragEnd(e) {
    if (isCloserToRightEdge(e)) {
      updateToggle(true)
    } else {
      updateToggle(false)
    }
  }

  // ...
}

// ✅ 也很好：该组件完全由它的父组件控制
function Toggle({ isOn, onChange }) {
  function handleClick() {
    onChange(!isOn)
  }

  function handleDragEnd(e) {
    if (isCloserToRightEdge(e)) {
      onChange(true)
    } else {
      onChange(false)
    }
  }

  // ...
}
```

## 将数据传递给父组件

避免：在 Effect 中传递数据给父组件

在React中，数据从父组件流向子组件。当你在屏幕上看到了一些错误时，你可以通过一路追踪组件树来寻找错误信息是从哪个组件传递下来的，从而找到传递了错误的prop或具有错误的state的组件。

当子组件在Effect中更新其父组件的 state 时，数据流变得非常难以追踪。既然子组件和父组件都需要相同的数据，那么可以让父组件获取那些数据，并将其 `向下传递` 给子组件。

```javascript
/*
* ❌错误做法
* */
function Parent() {
  const [data, setData] = useState(null);
  // ...
  return <Child onFetched={setData} />;
}

function Child({ onFetched }) {
  const data = useSomeAPI();
  // 🔴 避免：在 Effect 中传递数据给父组件
  useEffect(() => {
    if (data) {
      onFetched(data);
    }
  }, [onFetched, data]);
  // ...
}
```

```javascript
/*
* ✅正确做法
* */
function Parent() {
  const data = useSomeAPI();
  // ...
  // ✅ 非常好：向子组件传递数据
  return <Child data={data} />;
}

function Child({ data }) {
  // ...
}
```

## 订阅外部store 

```javascript
function useOnlineStatus() {
  // 不理想：在 Effect 中手动订阅 store
  const [isOnline, setIsOnline] = useState(true)
  useEffect(() => {
    function updateState() {
      setIsOnline(navigator.onLine)
    }

    updateState()

    window.addEventListener("online", updateState)
    window.addEventListener("offline", updateState)
    return () => {
      window.removeEventListener("online", updateState)
      window.removeEventListener("offline", updateState)
    }
  }, [])
  return isOnline
}

function ChatIndicator() {
  const isOnline = useOnlineStatus()
  // ...
}

/*
* 推荐使用useSyncExternalStore
* */
function subscribe(callback) {
  window.addEventListener("online", callback)
  window.addEventListener("offline", callback)
  return () => {
    window.removeEventListener("online", callback)
    window.removeEventListener("offline", callback)
  }
}

function useOnlineStatus() {
  // ✅ 非常好：用内置的 Hook 订阅外部 store
  return useSyncExternalStore(
    subscribe, // 只要传递的是同一个函数，React 不会重新订阅
    () => navigator.onLine, // 如何在客户端获取值
    () => true // 如何在服务端获取值
  )
}

function ChatIndicator() {
  const isOnline = useOnlineStatus()
  // ...
}
```

## 获取数据

```javascript
/*
* ❌假设你快速地输入 “hello”。那么 query 会从 “h” 变成 “he”，“hel”，“hell” 最后是 “hello”。
* 这会触发一连串不同的数据获取请求，但无法保证对应的返回顺序。
* 例如，“hell” 的响应可能在 “hello” 的响应 之后 返回。
* 由于它的 setResults() 是在最后被调用的，你将会显示错误的搜索结果。
* 这种情况被称为 “竞态条件”：两个不同的请求 “相互竞争”，并以与你预期不符的顺序返回。
* */
function SearchResults({ query }) {
  const [results, setResults] = useState([])
  const [page, setPage] = useState(1)

  useEffect(() => {
    // 🔴 避免：没有清除逻辑的获取数据
    fetchResults(query, page).then(json => {
      setResults(json)
    })
  }, [query, page])

  function handleNextPageClick() {
    setPage(page + 1)
  }
  // ...
}

/*
* 解决两个不同的请求 “相互竞争”，并以与你预期不符的顺序返回。
* */
function SearchResults({ query }) {
  const [results, setResults] = useState([])
  const [page, setPage] = useState(1)
  useEffect(() => {
    let ignore = false
    fetchResults(query, page).then(json => {
      if (!ignore) {
        setResults(json)
      }
    })
    return () => {
      ignore = true
    }
  }, [query, page])

  function handleNextPageClick() {
    setPage(page + 1)
  }
  // ...
}

/*
* 将获取逻辑提取到一个自定义Hook中
* */
function SearchResults({ query }) {
  const [page, setPage] = useState(1)
  const params = new URLSearchParams({ query, page })
  const results = useData(`/api/search?${params}`)

  function handleNextPageClick() {
    setPage(page + 1)
  }
  // ...
}

function useData(url) {
  const [data, setData] = useState(null)
  useEffect(() => {
    let ignore = false
    fetch(url)
      .then(response => response.json())
      .then(json => {
        if (!ignore) {
          setData(json)
        }
      })
    return () => {
      ignore = true
    }
  }, [url])
  return data
}
```

# 响应式Effect的生命周期

