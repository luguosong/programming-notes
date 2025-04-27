# 状态管理

## 用State响应输入

### 声明式 UI 与命令式 UI 的比较

- `命令式 UI`：必须去根据要发生的事情写一些明确的命令去操作 UI
- `声明式 UI`：不必直接去操作 UI,只需要`声明你想要显示的内容`，通过计算得出该如何去更新UI。

`React`就属于声明式UI。

### React如何实现声明式UI

假设我们现在需要开发一个表单提交组件。

#### 定位组件中不同的视图状态

首先，你需要去可视化UI界面中用户可能看到的所有不同的`状态`：

- `无数据`：表单有一个不可用状态的“提交”按钮。
- `输入中`：表单有一个可用状态的“提交”按钮。
- `提交中`：表单完全处于不可用状态，加载动画出现。
- `成功时`：显示“成功”的消息而非表单。
- `错误时`：与输入状态类似，但会多错误的消息。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202501081711504.png){ loading=lazy }
  <figcaption>定位组件中不同的视图状态</figcaption>
</figure>

``` jsx
export default function Form({
  // Try 'submitting', 'error', 'success':
  status = 'empty'
}) {
  if (status === 'success') {
    return <h1>That's right!</h1>
  }
  return (
    <>
      <h2>City quiz</h2>
      <p>
        In which city is there a billboard that turns air into drinkable water?
      </p>
      <form>
        <textarea disabled={
          status === 'submitting'
        } />
        <br />
        <button disabled={
          status === 'empty' ||
          status === 'submitting'
        }>
          Submit
        </button>
        {status === 'error' &&
          <p className="Error">
            Good guess but a wrong answer. Try again!
          </p>
        }
      </form>
      </>
  );
}
```

#### 确定是什么触发了这些状态的改变

你可以触发 state 的更新来响应两种输入：

- `人为输入`。比如点击按钮、在表单中输入内容，或导航到链接。
- `计算机输入`。比如网络请求得到反馈、定时器被触发，或加载一张图片。

以上两种情况中，你必须设置 `state变量` 去更新 UI。对于正在开发中的表单来说，你需要改变 state 以响应几个不同的输入：

- `改变输入框中的文本时`（人为）应该根据输入框的内容是否是空值，从而决定将表单的状态从`空值状态`切换到`输入中`或切换回
  `原状态`。
- `点击提交按钮时`（人为）应该将表单的状态切换到`提交中的状态`。
- `网络请求成功后`（计算机）应该将表单的状态切换到`成功的状态`。
- `网络请求失败后`（计算机）应该将表单的状态切换到`失败的状态`，与此同时，显示错误信息。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202501081722190.png){ loading=lazy }
  <figcaption>表单的各种状态</figcaption>
</figure>

#### 通过useState表示内存中的state

接下来你会需要在内存中通过 useState 表示组件中的视图状态。诀窍很简单：state 的每个部分都是“处于变化中的”，并且你需要让
`“变化的部分”尽可能的少`。更复杂的程序会产生更多 bug！

先从绝对必须存在的状态开始。例如，你需要存储输入的 answer 以及用于存储最后一个错误的 error （如果存在的话）：

``` jsx
const [answer, setAnswer] = useState('');
const [error, setError] = useState(null);
```

接下来，你需要一个状态变量来代表你想要显示的那个可视状态。通常有多种方式在内存中表示它，因此你需要进行实验。

如果你很难立即想出最好的办法，那就先从添加足够多的 state 开始，确保所有可能的视图状态都囊括其中：

``` jsx
const [isEmpty, setIsEmpty] = useState(true);
const [isTyping, setIsTyping] = useState(false);
const [isSubmitting, setIsSubmitting] = useState(false);
const [isSuccess, setIsSuccess] = useState(false);
const [isError, setIsError] = useState(false);
```

#### 删除任何不必要的state变量

- `这个 state 是否会导致矛盾？`例如，isTyping 与 isSubmitting 的状态不能同时为 true。矛盾的产生通常说明了这个 state
  没有足够的约束条件。两个布尔值有四种可能的组合，但是只有三种对应有效的状态。为了将“不可能”的状态移除，你可以将他们合并到一个 '
  status' 中，它的值必须是 'typing'、'submitting' 以及 'success' 这三个中的一个。
- `相同的信息是否已经在另一个 state 变量中存在？`另一个矛盾：isEmpty 和 isTyping 不能同时为 true。通过使它们成为独立的
  state 变量，可能会导致它们不同步并导致 bug。幸运的是，你可以移除 isEmpty 转而用 message.length === 0。
- `你是否可以通过另一个 state 变量的相反值得到相同的信息？`isError 是多余的，因为你可以检查 error !== null。

在清理之后，你只剩下 3 个（从原本的 7 个！）必要的 state 变量：

``` jsx
const [answer, setAnswer] = useState('');
const [error, setError] = useState(null);
const [status, setStatus] = useState('typing'); // 'typing', 'submitting', or 'success'
```

#### 连接事件处理函数以设置state

```jsx
import {useState} from 'react';

export default function Form() {
    const [answer, setAnswer] = useState('');
    const [error, setError] = useState(null);
    const [status, setStatus] = useState('typing');

    if (status === 'success') {
        return <h1>That's right!</h1>
    }

    async function handleSubmit(e) {
        e.preventDefault();
        setStatus('submitting');
        try {
            await submitForm(answer);
            setStatus('success');
        } catch (err) {
            setStatus('typing');
            setError(err);
        }
    }

    function handleTextareaChange(e) {
        setAnswer(e.target.value);
    }

    return (
        <>
            <h2>City quiz</h2>
            <p>
                In which city is there a billboard that turns air into drinkable water?
            </p>
            <form onSubmit={handleSubmit}>
        <textarea
            value={answer}
            onChange={handleTextareaChange}
            disabled={status === 'submitting'}
        />
                <br/>
                <button disabled={
                    answer.length === 0 ||
                    status === 'submitting'
                }>
                    Submit
                </button>
                {error !== null &&
                    <p className="Error">
                        {error.message}
                    </p>
                }
            </form>
        </>
    );
}

function submitForm(answer) {
    // Pretend it's hitting the network.
    return new Promise((resolve, reject) => {
        setTimeout(() => {
            let shouldError = answer.toLowerCase() !== 'lima'
            if (shouldError) {
                reject(new Error('Good guess but a wrong answer. Try again!'));
            } else {
                resolve();
            }
        }, 1500);
    });
}
```

## 选择State结构

### 合并关联的state

如果你总是同时更新两个或更多的 state 变量，请考虑将它们合并为一个单独的 state 变量。

```jsx title="方式一"
const [x, setX] = useState(0);
const [y, setY] = useState(0);
```

```jsx title="👍🏻方式二"
const [position, setPosition] = useState({x: 0, y: 0});
```

方式二要优于方式一。因为`如果某两个 state 变量总是一起变化，则将它们统一成一个 state 变量可能更好。`这样你就不会忘记让它们始终保持同步。

另一种你需要将数据整合到一个对象或一个数组的情况是，`你不知道未来需要多少个 state 片段`。例如，有一个用户可以添加自定义字段的表单时，这将会很有帮助。

### 避免矛盾的state

当 state 结构中存在多个相互矛盾或“不一致”的 state 时，你就可能为此会留下隐患。应尽量避免这种情况。

```jsx title="方式一"
// 表示正在发送
const [isSending, setIsSending] = useState(false);
// 表示已发送
const [isSent, setIsSent] = useState(false);
```

尽管这段代码是有效的，但也会让一些 state “极难处理”。例如，如果你忘记同时调用 setIsSent 和 setIsSending，则可能会出现
isSending 和 isSent 同时为 true 的情况。你的组件越复杂，你就越难理解发生了什么。

因为 isSending 和 isSent 不应同时为 true，所以最好用一个 status 变量来代替它们，这个 state
变量可以采取三种有效状态其中之一：'typing' (初始), 'sending', 和 'sent':

```jsx
const [status, setStatus] = useState('typing');
```

你仍然可以声明一些常量，以提高可读性：

```jsx
const isSending = status === 'sending';
const isSent = status === 'sent';
```

但它们不是 state 变量，所以你不必担心它们彼此失去同步。

### 避免冗余的state

如果你能在渲染期间从组件的 props 或其现有的 state 变量中计算出一些信息，则不应将这些信息放入该组件的 state 中。

```jsx
const [firstName, setFirstName] = useState('');
const [firstName, setLastName] = useState('');
const [fullName, setFullName] = useState('');
```

fullName可以用firstName和firstName计算出，没必要通过状态来保存它。

```jsx 
const [firstName, setFirstName] = useState('');
const [lastName, setLastName] = useState('');

const fullName = firstName + ' ' + lastName;
```

### 避免重复的state

当同一数据在多个 state 变量之间或在多个嵌套对象中重复时，这会很难保持它们同步。应尽可能减少重复。

```jsx title="方式一"
const initialItems = [
    {title: 'pretzels', id: 0},
    {title: 'crispy seaweed', id: 1},
    {title: 'granola bar', id: 2},
];

const [selectedItem, setSelectedItem] = useState(
    items[0]
);
```

如果使用方式一，title被修改后，更新selectedItem，title会被重新变为初始化值。

```jsx title="👍🏻方式二"
const initialItems = [
    {title: 'pretzels', id: 0},
    {title: 'crispy seaweed', id: 1},
    {title: 'granola bar', id: 2},
];

const [selectedId, setSelectedId] = useState(0);
```

### 避免深度嵌套的state

深度分层的 state 更新起来不是很方便。如果可能的话，最好以扁平化方式构建 state。

## 在组件间共享状态

### 状态提升

要协调好两个子组件，我们需要分 3 步将状态`提升`到他们的父组件中。

- 从子组件中 `移除` state 。
- 从父组件 `传递` 硬编码数据。
- 为共同的父组件添加 state ，并将其与事件处理函数一起向下传递。

```jsx
import {useState} from 'react';

export default function Accordion() {
    const [activeIndex, setActiveIndex] = useState(0);
    return (
        <>
            <h2>哈萨克斯坦，阿拉木图</h2>
            <Panel
                title="关于"
                isActive={activeIndex === 0}
                onShow={() => setActiveIndex(0)}
            >
                阿拉木图人口约200万，是哈萨克斯坦最大的城市。它在 1929 年到 1997 年间都是首都。
            </Panel>
            <Panel
                title="词源"
                isActive={activeIndex === 1}
                onShow={() => setActiveIndex(1)}
            >
                这个名字来自于 <span lang="kk-KZ">алма</span>，哈萨克语中“苹果”的意思，经常被翻译成“苹果之乡”。事实上，阿拉木图的周边地区被认为是苹果的发源地，<i
                lang="la">Malus sieversii</i> 被认为是现今苹果的祖先。
            </Panel>
        </>
    );
}

function Panel({
                   title,
                   children,
                   isActive,
                   onShow
               }) {
    return (
        <section className="panel">
            <h3>{title}</h3>
            {isActive ? (
                <p>{children}</p>
            ) : (
                <button onClick={onShow}>
                    显示
                </button>
            )}
        </section>
    );
}

```

### 受控组件和非受控组件

通常我们把包含`不受控制状态`的组件称为`非受控组件`。

非受控组件通常很简单，因为它们不需要太多配置。但是当你想把它们组合在一起使用时，就不那么灵活了。

---

当组件中的重要信息是由 `props` 而不是其自身状态驱动时，就可以认为该组件是`受控组件`。这就允许父组件完全指定其行为。

受控组件具有最大的灵活性，但它们需要父组件使用 props 对其进行配置。

!!! note

	在实践中，“受控”和“非受控”并不是严格的技术术语——通常每个组件都同时拥有内部状态和 props。然而，这对于组件该如何设计和提供什么样功能的讨论是有帮助的。

	当编写一个组件时，你应该考虑哪些信息应该受控制（通过 props），哪些信息不应该受控制（通过 state）。当然，你可以随时改变主意并重构代码。

### 每个状态都对应唯一的数据源

在 React 应用中，很多组件都有自己的状态。一些状态可能`活跃`在叶子组件（树形结构最底层的组件）附近，例如输入框。另一些状态可能在应用程序顶部
`活动`。例如，客户端路由库也是通过将当前路由存储在 React 状态中，利用 props 将状态层层传递下去来实现的！

对于每个独特的状态，都应该存在且只存在于一个指定的组件中作为 state。这一原则也被称为拥有`可信单一数据源`
。它并不意味着所有状态都存在一个地方——对每个状态来说，都需要一个特定的组件来保存这些状态信息。你应该 将状态提升 到公共父级，或
将状态传递 到需要它的子级中，而`不是在组件之间复制共享的状态`。

你的应用会随着你的操作而变化。当你将状态上下移动时，你依然会想要确定每个状态在哪里`活跃`。这都是过程的一部分！

## 对state进行保留和重置

根据组件在 UI 树中的位置，React 可以跟踪哪些 state 属于哪个组件。`只要一个组件还被渲染在 UI 树的相同位置，React 就会保留它的 state。`
如果它被移除，或者一个不同的组件被渲染在相同的位置，那么 React 就会丢掉它的 state。

React 会在将一个组件从树中移除时销毁它的 state。

### 相同位置多个相同组件进行区分

```jsx title="❌方式一"
{
    isPlayerA ? (
        <Counter person="Taylor"/>
    ) : (
        <Counter person="Sarah"/>
    )
}
```

方式一中，这两个 Counter 出现在相同的位置，所以 React 会认为它们是 同一个 Counter，只是传了不同的 person
prop。当isPlayerA切换时，Counter中的属性不会重置。

```jsx title="👍🏻方式二"
{
    isPlayerA &&
    <Counter person="Taylor"/>
}
{
    !isPlayerA &&
    <Counter person="Sarah"/>
}
```

方式二中，isPlayerA 的值是 true。所以第一个位置包含了 Counter 的 state，而第二个位置是空的。isPlayerA切换时第一个位置会被清空，而第二个位置现在包含了一个
Counter。每当 Counter 组件从 DOM 中移除时，它的 state 会被销毁。每次Counter内部的状态就会被重置。

```jsx title="👍🏻方式三"
{
    isPlayerA ? (
        <Counter key="Taylor" person="Taylor"/>
    ) : (
        <Counter key="Sarah" person="Sarah"/>
    )
}
```

方式三中，可以使用 `key` 来让 React 区分任何组件。默认情况下，React 使用父组件内部的顺序（“第一个计数器”、“第二个计数器”）来区分组件。但是
key 可以让你告诉 React 这不仅仅是 第一个 或者 第二个 计数器，而且还是一个特定的计数器——例如，Taylor 的 计数器。这样无论它出现在树的任何位置，
React 都会知道它是 Taylor 的 计数器！

## 迁移状态逻辑至Reducer中

对于拥有许多状态更新逻辑的组件来说，过于分散的事件处理程序可能会令人不知所措。对于这种情况，你可以将组件的所有状态更新逻辑整合到一个外部函数中，这个函数叫作
`reducer`。

``` jsx
import { useState } from 'react';
import AddTask from './AddTask.js';
import TaskList from './TaskList.js';

export default function TaskApp() {
  const [tasks, setTasks] = useState(initialTasks);

    {/*添加*/}
  function handleAddTask(text) {
    setTasks([
      ...tasks,
      {
        id: nextId++,
        text: text,
        done: false,
      },
    ]);
  }

	{/*更新*/}
  function handleChangeTask(task) {
    setTasks(
      tasks.map((t) => {
        if (t.id === task.id) {
          return task;
        } else {
          return t;
        }
      })
    );
  }

	{/*删除*/}
  function handleDeleteTask(taskId) {
    setTasks(tasks.filter((t) => t.id !== taskId));
  }

  return (
    <>
      <h1>布拉格的行程安排</h1>
      <AddTask onAddTask={handleAddTask} />
      <TaskList
        tasks={tasks}
        onChangeTask={handleChangeTask}
        onDeleteTask={handleDeleteTask}
      />
    </>
  );
}

let nextId = 3;
const initialTasks = [
  {id: 0, text: '参观卡夫卡博物馆', done: true},
  {id: 1, text: '看木偶戏', done: false},
  {id: 2, text: '打卡列侬墙', done: false},
];

```

Reducer 是处理状态的另一种方式。你可以通过三个步骤将 useState 迁移到 useReducer：

- 将设置状态的逻辑 修改 成 dispatch 的一个 action；
- 编写 一个 reducer 函数；
- 在你的组件中 使用 reducer。

```js
export default function tasksReducer(tasks, action) {
    switch (action.type) {
        case 'added': {
            return [
                ...tasks,
                {
                    id: action.id,
                    text: action.text,
                    done: false,
                },
            ];
        }
        case 'changed': {
            return tasks.map((t) => {
                if (t.id === action.task.id) {
                    return action.task;
                } else {
                    return t;
                }
            });
        }
        case 'deleted': {
            return tasks.filter((t) => t.id !== action.id);
        }
        default: {
            throw Error('未知 action：' + action.type);
        }
    }
}
```

```jsx
import {useReducer} from 'react';
import AddTask from './AddTask.js';
import TaskList from './TaskList.js';
import tasksReducer from './tasksReducer.js';

export default function TaskApp() {
    const [tasks, dispatch] = useReducer(tasksReducer, initialTasks);

    function handleAddTask(text) {
        dispatch({
            type: 'added',
            id: nextId++,
            text: text,
        });
    }

    function handleChangeTask(task) {
        dispatch({
            type: 'changed',
            task: task,
        });
    }

    function handleDeleteTask(taskId) {
        dispatch({
            type: 'deleted',
            id: taskId,
        });
    }

    return (
        <>
            <h1>布拉格的行程安排</h1>
            <AddTask onAddTask={handleAddTask}/>
            <TaskList
                tasks={tasks}
                onChangeTask={handleChangeTask}
                onDeleteTask={handleDeleteTask}
            />
        </>
    );
}

let nextId = 3;
const initialTasks = [
    {id: 0, text: '参观卡夫卡博物馆', done: true},
    {id: 1, text: '看木偶戏', done: false},
    {id: 2, text: '打卡列侬墙', done: false},
];
```

当像这样分离关注点时，我们可以更容易地理解组件逻辑。现在，事件处理程序只通过派发 action 来指定 发生了什么，而 reducer 函数通过响应
actions 来决定 状态如何更新。

### 对比useState和useReducer

- `代码体积`： 通常，在使用 useState 时，一开始只需要编写少量代码。而 useReducer 必须提前编写 reducer 函数和需要调度的
  actions。但是，当多个事件处理程序以相似的方式修改 state 时，useReducer 可以减少代码量。
- `可读性`： 当状态更新逻辑足够简单时，useState 的可读性还行。但是，一旦逻辑变得复杂起来，它们会使组件变得臃肿且难以阅读。在这种情况下，useReducer
  允许你将状态更新逻辑与事件处理程序分离开来。
- `可调试性`： 当使用 useState 出现问题时, 你很难发现具体原因以及为什么。 而使用 useReducer 时， 你可以在 reducer
  函数中通过打印日志的方式来观察每个状态的更新，以及为什么要更新（来自哪个 action）。 如果所有 action 都没问题，你就知道问题出在了
  reducer 本身的逻辑中。 然而，与使用 useState 相比，你必须单步执行更多的代码。
- `可测试性`： reducer 是一个不依赖于组件的纯函数。这就意味着你可以单独对它进行测试。一般来说，我们最好是在真实环境中测试组件，但对于复杂的状态更新逻辑，针对特定的初始状态和
  action，断言 reducer 返回的特定状态会很有帮助。
- `个人偏好`： 并不是所有人都喜欢用 reducer，没关系，这是个人偏好问题。你可以随时在 useState 和 useReducer 之间切换，它们能做的事情是一样的！

### 编写一个好的reducer

- `reducer 必须是纯粹的。` 这一点和 状态更新函数 是相似的，reducer 是在渲染时运行的！（actions 会排队直到下一次渲染)。 这就意味着
  reducer 必须纯净，即当输入相同时，输出也是相同的。它们不应该包含异步请求、定时器或者任何副作用（对组件外部有影响的操作）。它们应该以不可变值的方式去更新
  对象 和 数组。
- `每个 action 都描述了一个单一的用户交互，即使它会引发数据的多个变化。` 举个例子，如果用户在一个由 reducer
  管理的表单（包含五个表单项）中点击了 重置按钮，那么 dispatch 一个 reset_form 的 action 比 dispatch 五个单独的 set_field 的
  action 更加合理。如果你在一个 reducer 中打印了所有的 action 日志，那么这个日志应该是很清晰的，它能让你以某种步骤复现已发生的交互或响应。这对代码调试很有帮助！

## 使用Context深层传递参数

### 传递props带来的问题

但是当你需要在组件树中深层传递参数以及需要在组件间复用相同的参数时，传递 props 就会变得很麻烦。最近的根节点父组件可能离需要数据的组件很远，状态提升
到太高的层级会导致 “逐层传递 props” 的情况。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202501091357593.png){ loading=lazy }
  <figcaption>逐层传递props问题</figcaption>
</figure>

### 使用Context传递props

你可以通过以下三个步骤来实现Context：

- `创建`一个 context。（你可以将其命名为 LevelContext, 因为它表示的是标题级别。）

```js
import {createContext} from 'react';

export const LevelContext = createContext(1);
```

- 在需要数据的组件内 `使用`刚刚创建的context。（Heading 将会使用 LevelContext。）

```jsx
import {useContext} from 'react';
import {LevelContext} from './LevelContext.js';

export default function Heading({children}) {
    const level = useContext(LevelContext);
    // ...
}
```

- 在指定数据的组件中`提供这个context`。（Section 将会提供 LevelContext。）

```jsx
import {LevelContext} from './LevelContext.js';

export default function Section({level, children}) {
    return (
        <section className="section">
            <LevelContext.Provider value={level}>
                {children}
            </LevelContext.Provider>
        </section>
    );
}
```

### Context的使用场景

- `主题`： 如果你的应用允许用户更改其外观（例如暗夜模式），你可以在应用顶层放一个 context provider，并在需要调整其外观的组件中使用该
  context。
- `当前账户`： 许多组件可能需要知道当前登录的用户信息。将它放到 context
  中可以方便地在树中的任何位置读取它。某些应用还允许你同时操作多个账户（例如，以不同用户的身份发表评论）。在这些情况下，将 UI
  的一部分包裹到具有不同账户数据的 provider 中会很方便。
- `路由`： 大多数路由解决方案在其内部使用 context 来保存当前路由。这就是每个链接“知道”它是否处于活动状态的方式。如果你创建自己的路由库，你可能也会这么做。
- `状态管理`： 随着你的应用的增长，最终在靠近应用顶部的位置可能会有很多 state。许多遥远的下层组件可能想要修改它们。通常 将
  reducer 与 context 搭配使用来管理复杂的状态并将其传递给深层的组件来避免过多的麻烦。

### Context整合Reducer

假设我们现在已经有一个Reducer。

```jsx
const [tasks, dispatch] = useReducer(tasksReducer, initialTasks);
```

- 创建 context。

你将 创建 两个不同的 context，`TasksContext`提供当前的 tasks 列表。`TasksDispatchContext`提供了一个函数可以让组件分发动作。

```js
import {createContext} from 'react';

export const TasksContext = createContext(null);
export const TasksDispatchContext = createContext(null);
```

- 将 state 和 dispatch 放入 context。

```jsx
import {TasksContext, TasksDispatchContext} from './TasksContext.js';

export default function TaskApp() {
    const [tasks, dispatch] = useReducer(tasksReducer, initialTasks);
    // ...
    return (
        <TasksContext.Provider value={tasks}>
            <TasksDispatchContext.Provider value={dispatch}>
                ...
            </TasksDispatchContext.Provider>
        </TasksContext.Provider>
    );
}
```

- 在组件树的任何地方 使用 context。

``` jsx title="任何需要 tasks 的组件都可以从 TaskContext 中读取它" hl_lines="2"
export default function TaskList() {
  const tasks = useContext(TasksContext);
  // ...
```

``` jsx title="任何组件都可以从 context 中读取 dispatch 函数并调用它，从而更新任务列表：" hl_lines="3 9-13"
export default function AddTask() {
  const [text, setText] = useState('');
  const dispatch = useContext(TasksDispatchContext);
  // ...
  return (
    // ...
    <button onClick={() => {
      setText('');
      dispatch({
        type: 'added',
        id: nextId++,
        text: text,
      });
    }}>Add</button>
    // ...
```

#### 完整代码

将Context和Reducer相关逻辑迁移到一个文件当中,最终示例代码如下:

```jsx title="App.js"
      import AddTask from './AddTask.js';
      import TaskList from './TaskList.js';
      import {TasksProvider} from './TasksContext.js';
      
      export default function TaskApp() {
          return (
              <TasksProvider>
                  <h1>Day off in Kyoto</h1>
                  <AddTask/>
                  <TaskList/>
              </TasksProvider>
          );
      }
```

```jsx title="TasksContext.js"
import {createContext, useReducer} from 'react';

/*
* Context相关
* */
export const TasksContext = createContext(null);
export const TasksDispatchContext = createContext(null);


/*
* Reducer相关
* */
export function TasksProvider({children}) {
    const [tasks, dispatch] = useReducer(
        tasksReducer,
        initialTasks
    );

    return (
        <TasksContext.Provider value={tasks}>
            <TasksDispatchContext.Provider value={dispatch}>
                {children}
            </TasksDispatchContext.Provider>
        </TasksContext.Provider>
    );
}

function tasksReducer(tasks, action) {
    switch (action.type) {
        case 'added': {
            return [...tasks, {
                id: action.id,
                text: action.text,
                done: false
            }];
        }
        case 'changed': {
            return tasks.map(t => {
                if (t.id === action.task.id) {
                    return action.task;
                } else {
                    return t;
                }
            });
        }
        case 'deleted': {
            return tasks.filter(t => t.id !== action.id);
        }
        default: {
            throw Error('Unknown action: ' + action.type);
        }
    }
}

const initialTasks = [
    {id: 0, text: 'Philosopher’s Path', done: true},
    {id: 1, text: 'Visit the temple', done: false},
    {id: 2, text: 'Drink matcha', done: false}
];
```

```jsx title="AddTask.js"
import {useState, useContext} from 'react';
import {TasksDispatchContext} from './TasksContext.js';

export default function AddTask() {
    const [text, setText] = useState('');
    const dispatch = useContext(TasksDispatchContext);
    return (
        <>
            <input
                placeholder="Add task"
                value={text}
                onChange={e => setText(e.target.value)}
            />
            <button onClick={() => {
                setText('');
                dispatch({
                    type: 'added',
                    id: nextId++,
                    text: text,
                });
            }}>Add
            </button>
        </>
    );
}

let nextId = 3;
```

```jsx title="TaskList.js"
import {useState, useContext} from 'react';
import {TasksContext, TasksDispatchContext} from './TasksContext.js';

export default function TaskList() {
    const tasks = useContext(TasksContext);
    return (
        <ul>
            {tasks.map(task => (
                <li key={task.id}>
                    <Task task={task}/>
                </li>
            ))}
        </ul>
    );
}

function Task({task}) {
    const [isEditing, setIsEditing] = useState(false);
    const dispatch = useContext(TasksDispatchContext);
    let taskContent;
    if (isEditing) {
        taskContent = (
            <>
                <input
                    value={task.text}
                    onChange={e => {
                        dispatch({
                            type: 'changed',
                            task: {
                                ...task,
                                text: e.target.value
                            }
                        });
                    }}/>
                <button onClick={() => setIsEditing(false)}>
                    Save
                </button>
            </>
        );
    } else {
        taskContent = (
            <>
                {task.text}
                <button onClick={() => setIsEditing(true)}>
                    Edit
                </button>
            </>
        );
    }
    return (
        <label>
            <input
                type="checkbox"
                checked={task.done}
                onChange={e => {
                    dispatch({
                        type: 'changed',
                        task: {
                            ...task,
                            done: e.target.checked
                        }
                    });
                }}
            />
            {taskContent}
            <button onClick={() => {
                dispatch({
                    type: 'deleted',
                    id: task.id
                });
            }}>
                Delete
            </button>
        </label>
    );
}
```

