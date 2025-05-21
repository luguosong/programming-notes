# 脱围机制

## 使用ref引用值

当你希望组件“记住”某些信息，但又不想让这些信息触发新的渲染时，你可以使用ref。

### 区别对比

| 普通变量         | useState         | useRef       |
|--------------|------------------|--------------|
| ❌组件渲染不会记住值   | ✅组件渲染会记住值        | ✅组件渲染会记住值    |
| ❌改变值不会触发组件渲染 | ✅改变值会触发组件渲染      | ❌改变值不会触发组件渲染 |
| 直接修改         | 需要通过setState方法修改 | 直接修改         |              

### 入门示例

创建ref：

```jsx
import {useRef} from 'react';

const ref = useRef(0);
```

useRef 返回一个这样的对象:

```jsx
{
    current: 0 // 你向 useRef 传入的值
}
```

调用ref的值：

```ref
ref.current
```

修改ref的值:

```jsx
ref.current = ref.current + 1;
```

### 何时使用ref

- 存储 `timeout ID`
- 存储和操作 `DOM 元素`
- 存储不需要被用来计算 JSX 的其他对象。

### ref的最佳实践

- `将 ref 视为脱围机制`。当你使用外部系统或浏览器 API 时，ref 很有用。如果你很大一部分应用程序逻辑和数据流都依赖于
  ref，你可能需要重新考虑你的方法。
- `不要在渲染过程中读取或写入 ref.current`。 如果渲染过程中需要某些信息，请使用 state 代替。由于 React 不知道 ref.current
  何时发生变化，即使在渲染时读取它也会使组件的行为难以预测。（唯一的例外是像 if (!ref.current) ref.current = new Thing()
  这样的代码，它只在第一次渲染期间设置一次 ref。）

### 使用ref操作DOM

#### 使用步骤

- 声明ref

```jsx
import {useRef} from 'react';

const myRef = useRef(null);
```

- 将 ref 作为 ref 属性值传递给想要获取的 DOM 节点的 JSX 标签：

```jsx
<div ref={myRef}>
```

`useRef` Hook 返回一个对象，该对象有一个名为 `current` 的属性。最初，`myRef.current` 是 null。当 React 为这个 `<div>` 创建一个
DOM 节点时，React 会把对该节点的引用放入 `myRef.current`。然后，你可以从 事件处理器 访问此 DOM 节点，并使用在其上定义的内置浏览器
API。

```jsx
// 你可以使用任意浏览器 API，例如：
myRef.current.scrollIntoView();
```

#### ref向子组件传递

```jsx
import {useRef} from 'react';

function MyInput({ref}) {
    return <input ref={ref}/>;
}

export default function MyForm() {
    const inputRef = useRef(null);

    function handleClick() {
        inputRef.current.focus();
    }

    return (
        <>
            <MyInput ref={inputRef}/>
            <button onClick={handleClick}>
                聚焦输入框
            </button>
        </>
    );
}
```

#### React何时添加refs

在 React 中，每次更新都分为 两个阶段：

- 在 `渲染` 阶段， React 调用你的组件来确定屏幕上应该显示什么。
- 在 `提交` 阶段， React 把变更应用于 DOM。

通常，你 不希望 在渲染期间访问 refs。这也适用于保存 DOM 节点的 refs。在第一次渲染期间，DOM 节点尚未创建，因此 `ref.current`
将为 `null`。在渲染更新的过程中，DOM 节点还没有更新。所以读取它们还为时过早。

React 在`提交阶段`设置 ref.current。在更新 DOM 之前，React 将受影响的 ref.current 值设置为 null。`更新 DOM 后`，React
立即将它们设置到相应的 DOM 节点。

⭐小结：`ref.current`会在渲染阶段DOM更新后设置。

#### refs操作DOM的最佳实践

只在你必须`跳出 React`时使用refs比如：

- 管理焦点
- 滚动位置
- 调用 React 未暴露的浏览器 API

!!! warning

	`避免更改由 React 管理的 DOM 节点`。 对 React 管理的元素进行修改、添加子元素、从中删除子元素会导致不一致的视觉结果，或导致崩溃。

	但是，这并不意味着你完全不能这样做。它需要谨慎。 `你可以安全地修改 React 没有理由更新的部分 DOM。` 例如，如果某些 `<div>` 在 JSX 中始终为空，React 将没有理由去变动其子列表。 因此，在那里手动增删元素是安全的。

## 使用Effect进行同步

`Effect`允许你在渲染结束后执行一些代码，以便将组件与 React 外部的某个系统相同步。如：

- 控制非 React 组件
- 建立服务器连接
- 当组件在页面显示时发送分析日志

`Effect 允许你指定由渲染自身，而不是特定事件引起的副作用。`

### 入门案例

#### 声明 Effect

通常 Effect 会在每次 `提交` 后运行。

```jsx hl_lines="4-6"
import {useEffect} from 'react';

function MyComponent() {
    useEffect(() => {
        // 每次渲染后都会执行此处的代码
    });
    return <div/>;
}
```

每当你的组件渲染时，React 会先更新页面，然后再运行 useEffect 中的代码。换句话说，useEffect 会`延迟`一段代码的运行，直到渲染结果反映在页面上。

```jsx title="示例：控制非React组件"
import {useEffect, useRef} from 'react';

function VideoPlayer({src, isPlaying}) {
    const ref = useRef(null);

    // ❌这段代码不能放在渲染过程中，
    // 因为此时DOM还没有创建或更新
    // if (isPlaying) {
    //     ref.current.play();
    // } else {
    //     ref.current.pause();
    // }

    useEffect(() => {
        // ✅
        if (isPlaying) {
            ref.current.play();
        } else {
            ref.current.pause();
        }
    });

    return <video ref={ref} src={src} loop playsInline/>;
}
```

#### 指定Effect依赖

大多数 Effect 应该按需运行，而不是在每次渲染后都运行。例如，淡入动画应该只在组件出现时触发。连接和断开服务器的操作只应在组件出现和消失时，或者切换聊天室时执行。你将通过指定依赖项
来学习如何控制这一点。

=== "第一次和依赖项变化时运行"

    ``` jsx
    useEffect(() => {
        if (isPlaying) { // isPlaying 在此处使用……
            // ...
        } else {
            // ...
        }
    }, [isPlaying]); // ……所以它必须在此处声明！
    ```

=== "每次渲染时运行"

    ``` jsx
    useEffect(() => {
        
    });
    ```

=== "第一次渲染时运行"

    ``` jsx
    useEffect(() => {
        
    },[]);
    ```

#### 必要时添加清理操作

一些 Effect 需要指定如何停止、撤销，或者清除它们所执行的操作。例如，“连接”需要“断开”，“订阅”需要“退订”，而“获取数据”需要“取消”或者“忽略”。你将学习如何通过返回一个
清理函数 来实现这些。

```jsx hl_lines="4-6"
  useEffect(() => {
    const connection = createConnection();
    connection.connect();
    return () => {
        connection.disconnect();
    };
}, []);
```

### 使用场景

#### 管理非React小部件

```jsx
// 比如说你想在你的页面添加一个地图组件。
// 它有一个 setZoomLevel() 方法，
// 然后你希望地图的缩放比例和代码中的 zoomLevel state 保持同步。
useEffect(() => {
    const map = mapRef.current;
    map.setZoomLevel(zoomLevel);
}, [zoomLevel]);

//在开发环境中，React 会调用 Effect 两次
// 有些 API 可能不允许你连续调用两次。
// 此时可以通过实现清理函数
useEffect(() => {
    const dialog = dialogRef.current;
    dialog.showModal();
    return () => dialog.close();
}, []);
```

#### 订阅事件

```jsx
useEffect(() => {
    function handleScroll(e) {
        console.log(window.scrollX, window.scrollY);
    }

    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
}, []);
```

#### 触发动画

```jsx
useEffect(() => {
    const node = ref.current;
    node.style.opacity = 1; // 触发动画
    return () => {
        node.style.opacity = 0; // 重置为初始值
    };
}, []);
```

#### 获取数据

```jsx
useEffect(() => {
    let ignore = false;

    async function startFetching() {
        //这段请求可能持续很长事件
        const json = await fetchTodos(userId);
        // 当请求完成，页面可能已被销毁
        if (!ignore) {
            setTodos(json);
        }
    }

    startFetching();

    return () => {
        //页面销毁时，设置ignore，防止没有意义的状态提交
        ignore = true;
    };
}, [userId]);
```

#### 发送分析报告

```jsx
useEffect(() => {
    logVisit(url); // 发送 POST 请求
}, [url]);
```

在开发环境中，对于每个 URL，logVisit 都会被调用两次,在开发环境中会记录额外的访问日志。`我们建议保持不动`
,因为logVisit不应该在开发环境中执行任何操作，因为你不会想让开发设备的日志影响生产环境的统计数据。

在生产环境中，不会有重复的访问日志。

### 不适合的场景

#### 初始化应用

某些逻辑应该只在应用启动时运行一次。你可以将它放在组件外部：

```jsx
if (typeof window !== 'undefined') { // 检查是否在浏览器中运行
    checkAuthToken();
    loadDataFromLocalStorage();
}

function App() {
    // ……
}
```

这可以确保此类逻辑只在浏览器加载页面后运行一次。

```jsx title="个人觉得将只需要执行一次的代码放到根组件的 useEffect 中也没什么问题（依赖列表为空数组）"
function App() {
    useEffect(() => {
        checkAuthToken();
        loadDataFromLocalStorage();
    }, []);

    // ……
}
```

#### 购买商品

有时，即使你编写了清理函数，也无法避免用户观察到 Effect 运行了两次。比如你的 Effect 发送了一个像购买商品这样的 POST 请求：

```jsx
useEffect(() => {
    // 🔴 错误：此处的 Effect 在开发环境中会触发两次，暴露出代码中的问题。
    fetch('/api/buy', {method: 'POST'});
}, []);
```

你肯定不希望购买两次商品。这也是为什么你不应该把这种逻辑放在 Effect 中。如果用户跳转到另一个页面，然后按下“返回”按钮，你的
Effect 就会再次运行。你不希望用户在访问页面时就购买产品，而是在他们点击“购买”按钮时才购买。

购买操作并不是由渲染引起的，而是由特定的交互引起的。它应该只在用户按下按钮时执行。因此，它不应该写在 Effect 中，应当把
/api/buy 请求移动到“购买”按钮的`事件处理程序`中。

#### 根据props或state来更新state

```jsx title="❌fullName可以通过firstName和lastName计算得出"
function Form() {
  const [firstName, setFirstName] = useState('Taylor');
  const [lastName, setLastName] = useState('Swift');

  // 🔴 避免：多余的 state 和不必要的 Effect
  const [fullName, setFullName] = useState('');
  useEffect(() => {
    setFullName(firstName + ' ' + lastName);
  }, [firstName, lastName]);
  // ...
}
```

```jsx title="✅"
function Form() {
  const [firstName, setFirstName] = useState('Taylor');
  const [lastName, setLastName] = useState('Swift');
  // ✅ 非常好：在渲染期间进行计算
  const fullName = firstName + ' ' + lastName;
  // ...
}
```

`如果一个值可以基于现有的 props 或 state 计算得出，不要把它作为一个 state，而是在渲染期间直接计算这个值。`

但如果计算过程比较复杂，可以使用`useMemo`

```jsx
import { useMemo, useState } from 'react';

function TodoList({ todos, filter }) {
  const [newTodo, setNewTodo] = useState('');
  const visibleTodos = useMemo(() => {
    // ✅ 除非 todos 或 filter 发生变化，否则不会重新执行
    return getFilteredTodos(todos, filter);
  }, [todos, filter]);
  // ...
}
```

#### 当props变化时重置所有state

现象描述：父组件中的状态通过props传递给子组件。当父组件更新该状态时，子组件中独立的状态并不会更新。因为子组件的位置没有发生变化，也没有被清除，因此子组件中的状态始终保持不变。

??? note "具体代码"

    ``` jsx title="resetting-all-state-when-a-prop-changes.html"
    --8<-- "code/front-end/react/basic/example/resetting-all-state-when-a-prop-changes.html"
    ```
    
    <iframe loading="lazy" src="../example/resetting-all-state-when-a-prop-changes.html"></iframe>

❗低效解决方案：将props属性作为子组件useEffect的依赖性，props属性发生变化时，在useEffect中重置子组件的状态。

如果需求是清空所有状态这样做就很复杂了（如果子组件状态很多），并且还需要考虑子组件中还嵌套了其它子组件，这样就会变得更加复杂了。

```jsx title="❗低效的解决思路"
export default function ProfilePage({ userId }) {
  const [comment, setComment] = useState('');

  // ❗避免：当 prop 变化时，在 Effect 中重置 state
  useEffect(() => {
    setComment('');
  }, [userId]);
  // ...
}
```

✅正确思路：你可以通过为每个用户的个人资料组件提供一个明确的键来告诉 React 它们原则上是 不同 的个人资料组件。将你的组件拆分为两个组件，并从外部的组件传递一个 key 属性给内部的组件

通过将 userId 作为 key 传递给 Profile 组件，使  React 将具有不同 userId 的两个 Profile 组件视为两个不应共享任何状态的不同组件。每当 key（这里是 userId）变化时，React 将重新创建 DOM，并 重置 Profile 组件和它的所有子组件的 state。现在，当在不同的个人资料之间导航时，comment 区域将自动被清空。

```jsx
export default function ProfilePage({ userId }) {
  return (
    <Profile
      userId={userId}
      key={userId}
    />
  );
}

function Profile({ userId }) {
  // ✅ 当 key 变化时，该组件内的 comment 或其他 state 会自动被重置
  const [comment, setComment] = useState('');
  // ...
}
```

#### 当prop变化时调整部分state

