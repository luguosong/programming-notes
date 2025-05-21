# 添加交互

## 响应事件

### 创建事件处理函数

``` jsx
export default function Button() {
  function handleClick() {
    alert('你点击了我！');
  }

  return (
    <button onClick={handleClick}>
      点我
    </button>
  );
}
```

handleClick是一个`事件处理函数`，`事件处理函数`有如下特点:

- 通常在你的组件 `内部` 定义。
- 名称以 `handle` 开头，后跟事件名称。

或者，你也可以在 JSX 中定义一个内联的事件处理函数：

``` jsx
<button onClick={function handleClick() {
  alert('你点击了我！');
}}>
```

或者，直接使用更为简洁箭头函数：

``` jsx
<button onClick={() => {
alert('你点击了我！');
}}>
```

传递给事件处理函数的函数应直接传递，而非调用。

| 传递一个函数（正确）                                                                 | 调用一个函数（错误❌）                                                                                 |
|----------------------------------------------------------------------------|---------------------------------------------------------------------------------------------|
| `<button onClick={handleClick}>`                                           | `<button onClick={handleClick()}>`                                                          |
| 在第一个示例中，handleClick 函数作为 onClick 事件处理函数传递。这会让 React 记住它，并且只在用户点击按钮时调用你的函数。 | 第二个示例中，handleClick() 中最后的 () 会在 渲染 过程中 立即 触发函数，即使没有任何点击。这是因为位于 JSX {} 之间的 JavaScript 会立即执行。 |

### 事件处理函数读取props

由于事件处理函数声明于组件内部，因此它们可以直接访问组件的 props。

``` jsx
function AlertButton({ message, children }) {
  return (
    <button onClick={() => alert(message)}>
      {children}
    </button>
  );
}

export default function Toolbar() {
  return (
    <div>
      <AlertButton message="正在播放！">
        播放电影
      </AlertButton>
      <AlertButton message="正在上传！">
        上传图片
      </AlertButton>
    </div>
  );
}
```

### 父组件定义事件处理函数

在父组件中定义子组件的事件处理函数。不同的子组件，最总执行的事件处理函数可能是不同的。为此，将组件从父组件接收的 prop
作为事件处理函数传递。

``` jsx
function Button({onClick, children}) {
    return <button onClick={onClick}>{children}</button>;
}

export default function Toolbar() {
    function handlePlayClick() {
        alert(`正在播放！`);
    }

    return (
        <div>
            <Button onClick={handlePlayClick}>播放电影</Button>
            <Button onClick={() => alert("正在上传！")}>上传图片</Button>
        </div>
    );
}
```

!!! note "命名事件处理函数 prop"

    按照惯例，事件处理函数 props 应该以 on 开头，后跟一个大写字母。

### 事件传播问题

事件处理函数还将捕获任何来自子组件的事件。通常，我们会说事件会沿着树向上`冒泡`或`传播`：它从事件发生的地方开始，然后沿着树向上传播。

在 React 中所有事件都会传播，除了 onScroll，它仅适用于你附加到的 JSX 标签。

```jsx
/*
* 如果你点击任一按钮，它自身的 onClick 将首先执行，
* 然后父级 <div> 的 onClick 会接着执行。
* 因此会出现两条消息。
* 如果你点击 toolbar 本身，
* 将只有父级 <div> 的 onClick 会执行。
* */
export default function Toolbar() {
    return (
        <div className="Toolbar" onClick={() => {
            alert('你点击了 toolbar ！');
        }}>
            <button onClick={() => alert('正在播放！')}>
                播放电影
            </button>
            <button onClick={() => alert('正在上传！')}>
                上传图片
            </button>
        </div>
    );
}
```

调用 `e.stopPropagation()`，阻止事件进一步向上冒泡。

```jsx
export default function Toolbar() {
    return (
        <div className="Toolbar" onClick={() => {
            alert('你点击了 toolbar ！');
        }}>
            <button onClick={e => {
                // 阻止事件向上传播
                e.stopPropagation();
                alert('正在播放！');
            }}>
                播放电影
            </button>

            <button onClick={() => alert('正在上传！')}>
                上传图片
            </button>
        </div>
    );
}
```

也可以让`子组件处理事件`，同时也让`父组件指定一些额外的行为`。与事件传播不同，它并非自动。但使用这种模式的好处是你可以清楚地
`追踪因某个事件的触发而执行的整条代码链`。如果你依赖于事件传播，而且很难追踪哪些处理程序在执行，及其执行的原因，可以尝试这种方法。

``` jsx
function Button({ onClick, children }) {
  return (
    <button onClick={e => {
      e.stopPropagation();
      onClick();
    }}>
      {children}
    </button>
  );
}
```

### 阻止默认行为

某些浏览器事件具有与事件相关联的默认行为。例如，点击 <form> 表单内部的按钮会触发表单提交事件，默认情况下将重新加载整个页面。可以调用事件对象中的
`e.preventDefault()` 来阻止这种情况发生。

- `e.preventDefault()` 阻止少数事件的默认浏览器行为。

!!! warning

    不要混淆`e.stopPropagation()`和`e.preventDefault()`

``` jsx
export default function Signup() {
  return (
    <form onSubmit={e => {
      e.preventDefault();
      alert('提交表单！');
    }}>
      <input />
      <button>发送</button>
    </form>
  );
}
```

### 事件处理函数可以包含副作用

事件处理函数是执行副作用的最佳位置！！！

与渲染函数不同，事件处理函数不需要是 `纯函数`，因此它是用来 更改
某些值的绝佳位置。例如，更改输入框的值以响应键入，或者更改列表以响应按钮的触发。但是，为了更改某些信息，你首先需要某种方式存储它。在
React 中，这是通过 `state`（组件的记忆） 来完成的。

<figure markdown="span">
  ![](https://edrawcloudpubliccn.oss-cn-shenzhen.aliyuncs.com/viewer/self/1059758/share/2025-1-7/1736231321/main.svg){ loading=lazy }
  <figcaption>在事件处理函数中产生副作用</figcaption>
</figure>

## Hook

在 React 中，`useState` 以及任何其他以`use`开头的函数都被称为 `Hook`。

`Hook` 是特殊的函数，只在 React 渲染时有效。它们能让你 `hook` 到不同的 React 特性中去。

!!! warning

    `Hooks` ——`以 use 开头的函数`——`只能在组件或自定义 Hook 的最顶层调用`。 你不能在条件语句、循环语句或其他嵌套函数内调用 Hook。Hook 是函数，但将它们视为关于组件需求的无条件声明会很有帮助。在组件顶部 `use` React 特性，类似于在文件顶部`导入`模块。

## State

`useState` Hook 提供了这两个功能：

- `State 变量` 用于保存渲染间的数据，会保存上次渲染的值。
- `State setter 函数` 更新变量并触发 React 再次渲染组件。

### 案例

``` jsx title="use-state-demo.html"
--8<-- "code/front-end/react/basic/example/use-state-demo.html"
```

<iframe loading="lazy" src="../example/use-state-demo.html"></iframe>

点击按钮，会改变对应状态。状态改变组件会重新刷新，同时另一个状态的值不会被重新初始化，而是会被记住。

### 执行过程

``` jsx
const [index, setIndex] = useState(0);
```

1. `组件进行第一次渲染。` 因为你将 `0` 作为 `index` 的初始值传递给 `useState`，它将返回 `[0, setIndex]`。 React 记住 `0`
   是最新的 `state` 值。
2. `你更新了 state。`当用户点击按钮时，它会调用 `setIndex(index + 1)`。 index 是 `0`，所以它是 `setIndex(1)`。这告诉 React
   现在记住 `index` 是 `1` 并触发下一次渲染。
3. `组件进行第二次渲染。`React 仍然看到 `useState(0)`，但是因为 React 记住 了你将 `index` 设置为了 `1`，它将返回
   `[1, setIndex]`。
4. 以此类推！

### React如何知道返回哪个state

组件中调用多次useState方法，组件如何知道哪个方法返回哪个状态的？

```jsx
function MyApp() {
    const [state1, setState1] = useState(0)
    const [state2, setState2] = useState(0)
    const [state3, setState3] = useState(0)
    const [state4, setState4] = useState(0)
    const [state5, setState5] = useState(0)
}
```

`在同一组件的每次渲染中，Hooks 都依托于一个稳定的调用顺序。`因为只在顶层调用 Hooks，Hooks 将始终以相同的顺序被调用。此外，linter
插件也可以捕获大多数错误。

在 React 内部，为每个组件保存了一个数组，其中每一项都是一个 state 对。它维护当前 state 对的索引值，在渲染之前将其设置为
“0”。每次调用 useState 时，React 都会为你提供一个 state 对并增加索引值。

### State是隔离且私有的

State 是屏幕上组件实例内部的状态。换句话说，`如果你渲染同一个组件两次，每个副本都会有完全隔离的 state！`改变其中一个不会影响另一个。

与 props 不同，`state 完全私有于声明它的组件`。父组件无法更改它。这使你可以向任何组件添加或删除 state，而不会影响其他组件。

## 渲染和提交

<figure markdown="span">
  ![](https://edrawcloudpubliccn.oss-cn-shenzhen.aliyuncs.com/viewer/self/1059758/share/2025-1-7/1736231321/main.svg){ loading=lazy }
  <figcaption>渲染和提交</figcaption>
</figure>

### 触发渲染

- 初次渲染
	- 应用启动
	- 调用 createRoot 方法并传入目标 DOM 节点
	- 调用 render 函数触发第一次渲染
- 状态更新时重新渲染
	- 通过使用 set 函数 更新其状态来触发之后的渲染。

更新组件的状态会自动将一次渲染送入队列。

### 渲染过程

渲染过程是`递归的`:如果更新后的组件会返回某个另外的组件，那么 React 接下来就会渲染 那个 组件，而如果那个组件又返回了某个组件，那么
React 接下来就会渲染 那个 组件，以此类推。这个过程会持续下去，直到没有更多的嵌套组件并且 React 确切知道哪些东西应该显示到屏幕上为止。

!!! note "性能问题"

	如果更新的组件在树中的位置非常高，渲染更新后的组件内部所有嵌套组件的默认行为将不会获得最佳性能。如果你遇到了性能问题，性能 章节描述了几种可选的解决方案 。不要过早进行优化！

- 初次渲染
	- React 会调用根组件
	- 为所有标签`创建 DOM 节点`(此时仅仅是创建，还没有add到DOM中)
- 状态更新时重新渲染
	- React 会调用内部状态更新触发了渲染的函数组件。
	- React 将计算它们的哪些属性（如果有的话）自上次渲染以来`已更改`。在下一步`提交阶段`之前，它不会对这些信息执行任何操作(
	  此时仅仅计算变化，并不会更新DOM)。

### 提交到DOM

React 仅在渲染之间存在差异时才会更改 DOM 节点(局部更新DOM节点)。

- 初次渲染
	- React 会使用 `appendChild()` DOM API 将其创建的所有 DOM 节点放在屏幕上。
- 状态更新时重新渲染
	- React 将应用最少的必要操作（在渲染时计算！），以使得 DOM 与最新的渲染输出相互匹配。

### 浏览器绘制

在渲染完成并且 React 更新 DOM 之后，浏览器就会重新绘制屏幕。尽管这个过程被称为`浏览器渲染（browser rendering）`，但我们还是将它称为
`绘制（painting）`，以避免在这些文档的其余部分中出现混淆。

## state如同一张快照

### 渲染会及时生成一张快照

- 开始渲染
- 调用组件函数
- 根据当前渲染时的`state`计算出当前时间点上 UI 的`快照`
- React 会更新界面(DOM)以匹配返回的`快照`

`state`作为一个组件的记忆，不同于在你的函数返回之后就会消失的普通变量。state 实际上`活`在 React 本身中——就像被摆在一个架子上！——
`位于你的组件函数之外`。当 React 调用你的组件时，它会为特定的那一次渲染提供一张 `state快照`。你的组件会在其 JSX
中返回一张包含一整套新的 props 和事件处理函数的 UI 快照 ，其中所有的值都是 根据那一次渲染中 `state` 的值 `被计算出来的`！

请看以下示例：

``` jsx
import { useState } from 'react';

export default function Counter() {
  const [number, setNumber] = useState(0);

  return (
    <>
      <h1>{number}</h1>
      <button onClick={() => {
        setNumber(number + 1);
        setNumber(number + 1);
        setNumber(number + 1);
      }}>+3</button>
    </>
  )
}
```

分析以上代码：

1. 第一个`setNumber(number + 1)`被调用：number 是 0 所以 `setNumber(0 + 1)`。
	- React 准备在下一次渲染时将 number 更改为 1。
2. 第二个`setNumber(number + 1)`被调用：number 是0 所以 `setNumber(0 + 1)`。
	- React 准备在下一次渲染时将 number 更改为 1。
3. 第三个`setNumber(number + 1)`被调用：number 是0 所以 `setNumber(0 + 1)`。
	- React 准备在下一次渲染时将 number 更改为 1。

尽管你调用了三次 `setNumber(number + 1)`，但在 这次渲染的 事件处理函数中 `number 会一直是 0`，所以你会三次将 state 设置成
1。这就是为什么在你的事件处理函数执行完以后，React 重新渲染的组件中的 number 等于 1 而不是 3。

#### 示例一

``` jsx
import { useState } from 'react';

export default function Counter() {
  const [number, setNumber] = useState(0);

  return (
    <>
      <h1>{number}</h1>
      <button onClick={() => {
        setNumber(number + 5);
        alert(number);
      }}>+5</button>
    </>
  )
}
```

实际执行的是：

``` jsx
setNumber(0 + 5);
alert(0);
```

所有最终输出的是0

#### 示例二

`一个 state 变量的值永远不会在一次渲染的内部发生变化`， 即使其事件处理函数的代码是异步的。

``` jsx
import { useState } from 'react';

export default function Counter() {
  const [number, setNumber] = useState(0);

  return (
    <>
      <h1>{number}</h1>
      <button onClick={() => {
        setNumber(number + 5);
        setTimeout(() => {
          alert(number);
        }, 3000);
      }}>+5</button>
    </>
  )
}
```

实际执行的是：

``` jsx
setNumber(0 + 5);
setTimeout(() => {
  alert(0);
}, 3000);
```

因此执行结果依旧是0

## 把一系列state更新加入队列

### React会对state更新进行批处理

``` jsx
import { useState } from 'react';

export default function Counter() {
  const [number, setNumber] = useState(0);

  return (
    <>
      <h1>{number}</h1>
      <button onClick={() => {
        setNumber(number + 1);
        setNumber(number + 1);
        setNumber(number + 1);
      }}>+3</button>
    </>
  )
}
```

`React 会等到事件处理函数中的 所有 代码都运行完毕再处理你的 state 更新。` 这就是重新渲染只会发生在所有这些 `setNumber()`
调用 之后 的原因。

这让你可以更新多个 state 变量——甚至来自多个组件的 state 变量——而不会触发太多的 `重新渲染`。

但这也意味着只有在你的事件处理函数及其中任何代码执行完成 之后，UI 才会更新。这种特性也就是 `批处理`，它会使你的 React
应用运行得更快。它还会帮你避免处理只更新了一部分 state 变量的令人困惑的`半成品`渲染。

`React 不会跨多个需要刻意触发的事件（如点击）进行批处理`——每次点击都是单独处理的。请放心，React
只会在一般来说安全的情况下才进行批处理。这可以确保，例如，如果第一次点击按钮会禁用表单，那么第二次点击就不会再次提交它。

### ⭐(难点)在下次渲染前多次更新同一个state

在下次渲染之前多次更新同一个state。

你可以像 `setNumber(n => n + 1)` 这样传入一个根据`队列中的前一个state`计算下一个 state 的 函数，而不是像
`setNumber(number + 1) `这样传入下一个state值。

这是一种告诉 React`用 state 值做某事`而不是仅仅替换它的方法。

``` jsx
import { useState } from 'react';

export default function Counter() {
  const [number, setNumber] = useState(0);

  return (
    <>
      <h1>{number}</h1>
      <button onClick={() => {
        setNumber(n => n + 1);
        setNumber(n => n + 1);
        setNumber(n => n + 1);
      }}>+3</button>
    </>
  )
}
```

这样每次点击按钮会增加3

在这里，`n => n + 1` 被称为 `更新函数`。当你将它传递给一个 state 设置函数时：

1. React 会将此函数加入队列，以便在事件处理函数中的所有其他代码运行后进行处理。
2. 在下一次渲染期间，React 会遍历队列并给你更新之后的最终 state。

具体执行过程：

1. `setNumber(n => n + 1)`：`n => n + 1` 是一个函数定义。React 将它加入队列。
2. `setNumber(n => n + 1)`：`n => n + 1` 是一个函数定义。React 将它加入队列。
3. `setNumber(n => n + 1)`：`n => n + 1` 是一个函数定义。React 将它加入队列。

当你在下次渲染期间调用 useState 时，React 会遍历队列。之前的 number state 的值是 0，所以这就是 React 作为参数 n
传递给第一个更新函数的值。然后 React 会获取你上一个更新函数的返回值，并将其作为 n 传递给下一个更新函数，以此类推：

| 更新队列       | n | 返回值       |
|------------|---|-----------|
| n => n + 1 | 0 | 0 + 1 = 1 |
| n => n + 1 | 1 | 1 + 1 = 2 |
| n => n + 1 | 2 | 2 + 1 = 3 |

React 会保存 3 为最终结果并从 useState 中返回。

这就是为什么在上面的示例中点击“+3”正确地将值增加“+3”。

---

再举一个例子：

``` jsx
import { useState } from 'react';

export default function Counter() {
  const [number, setNumber] = useState(0);

  return (
    <>
      <h1>{number}</h1>
      <button onClick={() => {
        setNumber(number + 5);
        setNumber(n => n + 1);
      }}>增加数字</button>
    </>
  )
}
```

点击按钮，每次会增加6

这是事件处理函数告诉 React 要做的事情：

1. `setNumber(number + 5)`：number 为 0，所以 setNumber(0 + 5)。React 将 `替换为 5` 添加到其队列中。
2. `setNumber(n => n + 1)`：`n => n + 1` 是一个更新函数。 React 将 该函数 添加到其队列中。

| 更新队列       | n      | 返回值       |
|------------|--------|-----------|
| “替换为 5”    | 0（未使用） | 5         |
| n => n + 1 | 5      | 5 + 1 = 6 |

--- 

再举一个例子

``` jsx
import { useState } from 'react';

export default function Counter() {
  const [number, setNumber] = useState(0);

  return (
    <>
      <h1>{number}</h1>
      <button onClick={() => {
        setNumber(number + 5);
        setNumber(n => n + 1);
        setNumber(42);
      }}>增加数字</button>
    </>
  )
}
```

执行结果为42

以下是 React 在执行事件处理函数时处理这几行代码的过程：

1. `setNumber(number + 5)`：number 为 0，所以 setNumber(0 + 5)。React 将 “替换为 5” 添加到其队列中。
2. `setNumber(n => n + 1)`：n => n + 1 是一个更新函数。React 将该函数添加到其队列中。
3. `setNumber(42)`：React 将 “替换为 42” 添加到其队列中。

在下一次渲染期间，React 会遍历 state 队列：

| 更新队列       |        |           |
|------------|--------|-----------|
| “替换为 5”    | 0（未使用） | 5         |
| n => n + 1 | 5      | 5 + 1 = 6 |
| “替换为 42”   | 6（未使用） | 42        |

总而言之，以下是你可以考虑传递给 setNumber state 设置函数的内容：

- 一个`更新函数`（例如：n => n + 1）会被添加到队列中。
- 任何其他的`值`（例如：数字 5）会导致“替换为 5”被添加到队列中，已经在队列中的内容会被忽略。

事件处理函数执行完成后，React 将触发重新渲染。在重新渲染期间，React 将处理队列。更新函数会在渲染期间执行，因此 `更新函数必须是 纯函数`
并且只 `返回` 结果。不要尝试从它们内部设置 state 或者执行其他副作用。在严格模式下，React 会执行每个更新函数两次（`但是丢弃第二个结果`
）以便帮助你发现错误。

### 更新函数命名惯例

通常可以通过相应 state 变量的第一个字母来命名更新函数的参数：

``` jsx
setEnabled(e => !e);
setLastName(ln => ln.reverse());
setFriendCount(fc => fc * 2);
```

如果你喜欢更冗长的代码，另一个常见的惯例是重复使用完整的 state 变量名称，如 setEnabled(enabled => !enabled)，或使用前缀，如
setEnabled(prevEnabled => !prevEnabled)。

## 更新state中的对象

### 对象视为只读

state 中可以保存任意类型的 JavaScript 值，包括对象。但是，你不应该直接修改存放在 React state 中的对象。相反，当你想要更新一个对象时，你需要
`创建一个新的对象（或者将其拷贝一份），然后将 state 更新为此对象`。

你应该 `把所有存放在 state 中的JavaScript对象都视为只读的`。

``` jsx title="❌错误做法"
import {useState} from 'react';

export default function MovingDot() {
    const [position, setPosition] = useState({
        x: 0,
        y: 0
    });
    return (
        <div
            onPointerMove={e => {
                {/*❌没有使用 state 的设置函数，React 并不知道对象已更改。*/
                }
                position.x = e.clientX;
                position.y = e.clientY;
            }}
            style={{
                position: 'relative',
                width: '100vw',
                height: '100vh',
            }}>
            <div style={{
                position: 'absolute',
                backgroundColor: 'red',
                borderRadius: '50%',
                transform: `translate(${position.x}px, ${position.y}px)`,
                left: -10,
                top: -10,
                width: 20,
                height: 20,
            }}/>
        </div>
    );
}
```

``` jsx title="✅正确做法"
import {useState} from 'react';

export default function MovingDot() {
    const [position, setPosition] = useState({
        x: 0,
        y: 0
    });
    return (
        <div
            onPointerMove={e => {
                {/*✅使用 state 的设置函数*/}
                setPosition({
                    x: e.clientX,
                    y: e.clientY
                });
            }}
            style={{
                position: 'relative',
                width: '100vw',
                height: '100vh',
            }}>
            <div style={{
                position: 'absolute',
                backgroundColor: 'red',
                borderRadius: '50%',
                transform: `translate(${position.x}px, ${position.y}px)`,
                left: -10,
                top: -10,
                width: 20,
                height: 20,
            }}/>
        </div>
    );
}

```

### 使用展开语法复制对象

把`现有`数据作为你所创建的新对象的一部分。

``` jsx
import { useState } from 'react';

export default function Form() {
  const [person, setPerson] = useState({
    firstName: 'Barbara',
    lastName: 'Hepworth',
    email: 'bhepworth@sculpture.com'
  });

  function handleFirstNameChange(e) {
    setPerson({
      ...person,
      firstName: e.target.value
    });
  }

  function handleLastNameChange(e) {
    setPerson({
      ...person,
      lastName: e.target.value
    });
  }

  function handleEmailChange(e) {
    setPerson({
      ...person,
      email: e.target.value
    });
  }

  return (
    <>
      <label>
        First name:
        <input
          value={person.firstName}
          onChange={handleFirstNameChange}
        />
      </label>
      <label>
        Last name:
        <input
          value={person.lastName}
          onChange={handleLastNameChange}
        />
      </label>
      <label>
        Email:
        <input
          value={person.email}
          onChange={handleEmailChange}
        />
      </label>
      <p>
        {person.firstName}{' '}
        {person.lastName}{' '}
        ({person.email})
      </p>
    </>
  );
}
```

!!! warning

	请注意 `...` 展开语法本质是是`浅拷贝`——它只会复制一层。这使得它的执行速度很快，但是也意味着当你想要更新一个嵌套属性时，你必须得多次使用展开语法。

### 更新一个嵌套对象

考虑下面这种结构的嵌套对象：

```js
const [person, setPerson] = useState({
    name: 'Niki de Saint Phalle',
    artwork: {
        title: 'Blue Nana',
        city: 'Hamburg',
        image: 'https://i.imgur.com/Sd1AgUOm.jpg',
    }
});
```

但是在 React 中，你需要将 state 视为不可变的！为了修改 city 的值，你首先需要创建一个新的 artwork 对象（其中预先填充了上一个
artwork 对象中的数据），然后创建一个新的 person 对象，并使得其中的 artwork 属性指向新创建的 artwork 对象：

```js
const nextArtwork = {...person.artwork, city: 'New Delhi'};
const nextPerson = {...person, artwork: nextArtwork};
setPerson(nextPerson);
```

或者，写成一个函数调用：

```js
setPerson({
    ...person, // 复制其它字段的数据 
    artwork: { // 替换 artwork 字段 
        ...person.artwork, // 复制之前 person.artwork 中的数据
        city: 'New Delhi' // 但是将 city 的值替换为 New Delhi！
    }
});
```

或者可以借助`Immer`库简化代码：

1. 运行 `npm install use-immer` 添加 Immer 依赖
2. 用 `import { useImmer } from 'use-immer'` 替换掉 `import { useState } from 'react'`

```jsx
import {useImmer} from 'use-immer';

export default function Form() {
    const [person, updatePerson] = useImmer({
        name: 'Niki de Saint Phalle',
        artwork: {
            title: 'Blue Nana',
            city: 'Hamburg',
            image: 'https://i.imgur.com/Sd1AgUOm.jpg',
        }
    });

    function handleNameChange(e) {
        updatePerson(draft => {
            draft.name = e.target.value;
        });
    }

    function handleTitleChange(e) {
        updatePerson(draft => {
            draft.artwork.title = e.target.value;
        });
    }

    function handleCityChange(e) {
        updatePerson(draft => {
            draft.artwork.city = e.target.value;
        });
    }

    function handleImageChange(e) {
        updatePerson(draft => {
            draft.artwork.image = e.target.value;
        });
    }

    return (
        <>
            <label>
                Name:
                <input
                    value={person.name}
                    onChange={handleNameChange}
                />
            </label>
            <label>
                Title:
                <input
                    value={person.artwork.title}
                    onChange={handleTitleChange}
                />
            </label>
            <label>
                City:
                <input
                    value={person.artwork.city}
                    onChange={handleCityChange}
                />
            </label>
            <label>
                Image:
                <input
                    value={person.artwork.image}
                    onChange={handleImageChange}
                />
            </label>
            <p>
                <i>{person.artwork.title}</i>
                {' by '}
                {person.name}
                <br/>
                (located in {person.artwork.city})
            </p>
            <img
                src={person.artwork.image}
                alt={person.artwork.title}
            />
        </>
    );
}

```

可以看到，事件处理函数变得更简洁了。你可以随意在一个组件中同时使用 useState 和 useImmer。如果你想要写出更简洁的更新处理函数，Immer
会是一个不错的选择，尤其是当你的 state 中有嵌套，并且复制对象会带来重复的代码时。

## 更新state中的数组

### 数组视为不可变

当你想要更新存储于 state 中的数组时，你需要`创建一个新的数组`（或者创建一份已有数组的拷贝值），并使用新数组设置 state。

每次要更新一个数组时，你需要把一个新的数组传入 state 的 `setting方法`中。为此，你可以通过使用像 `filter()` 和 `map()`
这样不会直接修改原始值的方法，从原始数组生成一个新的数组。然后你就可以将 state 设置为这个新生成的数组。

### 添加元素

使用 `...` 数组展开 语法。

``` jsx
import {useState} from 'react';

let nextId = 0;

export default function List() {
    const [name, setName] = useState('');
    const [artists, setArtists] = useState([]);

    return (
        <>
            <h1>振奋人心的雕塑家们：</h1>
            <input
                value={name}
                onChange={e => setName(e.target.value)}
            />
            <button onClick={() => {
                setArtists([
                    ...artists,
                    {id: nextId++, name: name}
                ]);
            }}>添加
            </button>
            <ul>
                {artists.map(artist => (
                    <li key={artist.id}>{artist.name}</li>
                ))}
            </ul>
        </>
    );
}
```

### 删除元素

通过 `filter方法`将要删除的元素`过滤出去`.

``` jsx
import { useState } from 'react';

let initialArtists = [
  { id: 0, name: 'Marta Colvin Andrade' },
  { id: 1, name: 'Lamidi Olonade Fakeye'},
  { id: 2, name: 'Louise Nevelson'},
];

export default function List() {
  const [artists, setArtists] = useState(
    initialArtists
  );

  return (
    <>
      <h1>振奋人心的雕塑家们：</h1>
      <ul>
        {artists.map(artist => (
          <li key={artist.id}>
            {artist.name}{' '}
            <button onClick={() => {
              {/*将要删除的元素过滤掉*/}
              setArtists(
                artists.filter(a =>
                  a.id !== artist.id
                )
              );
            }}>
              删除
            </button>
          </li>
        ))}
      </ul>
    </>
  );
}

```

### 更新元素

使用 `map()` 创建一个新数组。你传入 map 的函数决定了要根据每个元素的值或索引（或二者都要）对元素做何处理。

``` jsx
import { useState } from 'react';

let initialShapes = [
  { id: 0, type: 'circle', x: 50, y: 100 },
  { id: 1, type: 'square', x: 150, y: 100 },
  { id: 2, type: 'circle', x: 250, y: 100 },
];

export default function ShapeEditor() {
  const [shapes, setShapes] = useState(
    initialShapes
  );

  function handleClick() {
    const nextShapes = shapes.map(shape => {
      if (shape.type === 'square') {
        // 不作改变
        return shape;
      } else {
        // 返回一个新的圆形，位置在下方 50px 处
        return {
          ...shape,
          y: shape.y + 50,
        };
      }
    });
    // 使用新的数组进行重渲染
    setShapes(nextShapes);
  }

  return (
    <>
      <button onClick={handleClick}>
        所有圆形向下移动！
      </button>
      {shapes.map(shape => (
        <div
          key={shape.id}
          style={{
          background: 'purple',
          position: 'absolute',
          left: shape.x,
          top: shape.y,
          borderRadius:
            shape.type === 'circle'
              ? '50%' : '',
          width: 20,
          height: 20,
        }} />
      ))}
    </>
  );
}
```

### 插入元素

想向数组特定位置插入一个元素，使用展开运算符 `...` 和 `slice()` 方法配合

``` jsx
import { useState } from 'react';

let nextId = 3;
const initialArtists = [
  { id: 0, name: 'Marta Colvin Andrade' },
  { id: 1, name: 'Lamidi Olonade Fakeye'},
  { id: 2, name: 'Louise Nevelson'},
];

export default function List() {
  const [name, setName] = useState('');
  const [artists, setArtists] = useState(
    initialArtists
  );

  function handleClick() {
    const insertAt = 1; // 可能是任何索引
    const nextArtists = [
      // 插入点之前的元素：
      ...artists.slice(0, insertAt),
      // 新的元素：
      { id: nextId++, name: name },
      // 插入点之后的元素：
      ...artists.slice(insertAt)
    ];
    setArtists(nextArtists);
    setName('');
  }

  return (
    <>
      <h1>振奋人心的雕塑家们：</h1>
      <input
        value={name}
        onChange={e => setName(e.target.value)}
      />
      <button onClick={handleClick}>
        插入
      </button>
      <ul>
        {artists.map(artist => (
          <li key={artist.id}>{artist.name}</li>
        ))}
      </ul>
    </>
  );
}

```

### 其他改变数组的情况

总会有一些事，是你仅仅依靠展开运算符和 map() 或者 filter() 等不会直接修改原值的方法所无法做到的。例如，你可能想翻转数组，或是对数组排序。而
JavaScript 中的 reverse() 和 sort() 方法会改变原数组，所以你无法直接使用它们。

然而，你可以`先拷贝这个数组`，再改变这个拷贝后的值。

``` jsx
import {useState} from 'react';

const initialList = [
    {id: 0, title: 'Big Bellies'},
    {id: 1, title: 'Lunar Landscape'},
    {id: 2, title: 'Terracotta Army'},
];

export default function List() {
    const [list, setList] = useState(initialList);

    function handleClick() {
        const nextList = [...list];
        nextList.reverse();
        setList(nextList);
    }

    return (
        <>
            <button onClick={handleClick}>
                翻转
            </button>
            <ul>
                {list.map(artwork => (
                    <li key={artwork.id}>{artwork.title}</li>
                ))}
            </ul>
        </>
    );
}
```

❗以上这种方式,仅仅适用于更新元素位置，但还是不能直接修改其内部的元素。这是因为数组的拷贝是浅拷贝——新的数组中依然保留了与原始数组相同的元素。

当你更新一个嵌套的 state 时，`你需要从想要更新的地方创建拷贝值，一直这样，直到顶层`。

``` jsx
import { useState } from 'react';

let nextId = 3;
const initialList = [
  { id: 0, title: 'Big Bellies', seen: false },
  { id: 1, title: 'Lunar Landscape', seen: false },
  { id: 2, title: 'Terracotta Army', seen: true },
];

export default function BucketList() {
  const [myList, setMyList] = useState(initialList);
  const [yourList, setYourList] = useState(
    initialList
  );

  function handleToggleMyList(artworkId, nextSeen) {
    setMyList(myList.map(artwork => {
      if (artwork.id === artworkId) {
        // 创建包含变更的*新*对象
        return { ...artwork, seen: nextSeen };
      } else {
        // 没有变更
        return artwork;
      }
    }));
  }

  function handleToggleYourList(artworkId, nextSeen) {
    setYourList(yourList.map(artwork => {
      if (artwork.id === artworkId) {
        // 创建包含变更的*新*对象
        return { ...artwork, seen: nextSeen };
      } else {
        // 没有变更
        return artwork;
      }
    }));
  }

  return (
    <>
      <h1>艺术愿望清单</h1>
      <h2>我想看的艺术清单：</h2>
      <ItemList
        artworks={myList}
        onToggle={handleToggleMyList} />
      <h2>你想看的艺术清单：</h2>
      <ItemList
        artworks={yourList}
        onToggle={handleToggleYourList} />
    </>
  );
}

function ItemList({ artworks, onToggle }) {
  return (
    <ul>
      {artworks.map(artwork => (
        <li key={artwork.id}>
          <label>
            <input
              type="checkbox"
              checked={artwork.seen}
              onChange={e => {
                onToggle(
                  artwork.id,
                  e.target.checked
                );
              }}
            />
            {artwork.title}
          </label>
        </li>
      ))}
    </ul>
  );
}
```
