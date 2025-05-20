# MobX与React十分钟入门

[原文地址](https://mobx.js.org/getting-started)

MobX 是一个简单、可扩展且经过实战检验的状态管理解决方案。本教程将在十分钟内带你掌握 MobX 的所有核心概念。虽然 MobX
是一个独立的库，但大多数人会将它与 React 搭配使用，本教程也将重点介绍这种组合方式。

## 核心理念

`状态(State)`
是每个应用程序的核心，而最快让应用变得漏洞百出、难以维护的方法，就是让状态变得不一致，或者让状态与那些遗留的本地变量不同步。因此，许多状态管理方案都会试图限制你修改状态的方式，比如让状态变为不可变的。但这又带来了新的问题：数据需要被规范化，引用完整性无法再得到保证，而且如果你喜欢用类这样的强大概念，几乎就变得无法实现了。

MobX 通过解决根本问题，让状态管理变得再次简单：它让产生不一致的状态变得不可能。实现这一目标的方法也很直接：
`确保所有可以从应用状态推导出来的内容，都会被自动推导出来。`

从概念上讲，MobX 将你的应用程序视为一个电子表格。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505151047499.png){ loading=lazy }
  <figcaption>Mobx状态管理</figcaption>
</figure>

1. 首先，是`应用状态(application state)`。对象、数组、原始值和引用等构成了你应用的模型，这些值就是你应用的“数据单元”。
2. 第二，是`派生值(derivations)`
   。简单来说，任何可以根据应用状态自动计算出来的值都属于派生值。这些派生值（或称计算值）可以很简单，比如未完成待办事项的数量，也可以很复杂，比如待办事项的可视化HTML展示。用电子表格来类比，这些就是你应用中的公式和图表。
3. 第三，`反应（Reactions）`和派生值很相似。主要区别在于，这些函数`不会产生一个值`，而是会自动运行来执行某些任务，通常与输入/输出相关。它们确保DOM能及时更新，或者在合适的时机自动发起网络请求。
4. 最后，是`动作（Actions）`。动作指的是所有会改变应用状态的操作。MobX 会确保所有由动作引起的状态变化，都会被所有的派生值和反应自动、同步且无误地处理。

## 一个简单的待办事项存储器

理论讲得够多了，实际操作一下可能比仔细阅读上面的内容更容易理解。为了有点新意，我们先从一个非常简单的待办事项（ToDo）存储开始。请注意，下面所有的代码块都是可编辑的，你可以点击“运行代码”按钮来执行它们。下面这个 TodoStore 非常直接，只是维护了一个待办事项的集合，还没有用到 MobX。

```javascript
class TodoStore {
  todos = [];

  get completedTodosCount() {
    return this.todos.filter(
      todo => todo.completed === true
    ).length;
  }

  report() {
    if (this.todos.length === 0)
      return "<none>";
    const nextTodo = this.todos.find(todo => todo.completed === false);
    return `Next todo: "${nextTodo ? nextTodo.task : "<none>"}". ` +
      `Progress: ${this.completedTodosCount}/${this.todos.length}`;
  }

  addTodo(task) {
    this.todos.push({
      task: task,
      completed: false,
      assignee: null
    });
  }
}

const todoStore = new TodoStore();
```

我们刚刚创建了一个包含 todos 集合的 todoStore 实例。现在是时候往 todoStore 里添加一些对象了。为了确保我们能看到每次更改带来的效果，每次修改后我们都会调用 todoStore.report 并将结果打印出来。需要注意的是，report 方法有意只打印第一个任务。虽然这样做让这个例子显得有些刻意，但正如我们之后会看到的，这很好地展示了 MobX 的依赖追踪是动态的。

```javascript
todoStore.addTodo("read MobX tutorial");
console.log(todoStore.report());

todoStore.addTodo("try MobX");
console.log(todoStore.report());

todoStore.todos[0].completed = true;
console.log(todoStore.report());

todoStore.todos[1].task = "try MobX in own project";
console.log(todoStore.report());

todoStore.todos[0].task = "grok MobX tutorial";
console.log(todoStore.report());
```

```shell title="执行结果"
Next todo: "read MobX tutorial". Progress: 0/1
Next todo: "read MobX tutorial". Progress: 0/2
Next todo: "try MobX". Progress: 1/2
Next todo: "try MobX in own project". Progress: 1/2
Next todo: "try MobX in own project". Progress: 1/2
```

## 变得响应式

到目前为止，这段代码并没有什么特别之处。但如果我们不需要显式地调用 report，而是可以声明让它在每次相关状态发生变化时自动执行，会怎么样呢？这样一来，我们就不用在代码库中任何可能影响报告的地方都手动调用 report 了。我们确实希望每次都能打印出最新的报告，但又不想为此费心去安排这些调用。

幸运的是，MobX正好可以帮我们做到这一点——`自动执行那些只依赖于状态的代码`。这样，我们的report函数就能像电子表格里的图表一样自动更新。要实现这一点，TodoStore需要变成可观察的对象，这样MobX才能追踪所有发生的更改。接下来，我们只需要对这个类做一些必要的调整即可。

此外，`completedTodosCount` 属性其实可以根据 todo 列表自动推导出来。通过使用 observable 和 computed 注解，我们可以在对象上引入可观察的属性。在下面的示例中，我们使用 makeObservable 显式地展示了这些注解，其实也可以用 makeAutoObservable(this) 来简化这个过程。

```javascript
class ObservableTodoStore {
  todos = [];
  pendingRequests = 0;

  constructor() {
    makeObservable(this, {
      todos: observable,
      pendingRequests: observable,
      completedTodosCount: computed,
      report: computed,
      addTodo: action,
    });
    // 函数内部用到的任何可观察数据发生变化，它就会自动重新运行。
    autorun(() => console.log(this.report));
  }

  get completedTodosCount() {
    return this.todos.filter(
      todo => todo.completed === true
    ).length;
  }

  get report() {
    if (this.todos.length === 0)
      return "<none>";
    const nextTodo = this.todos.find(todo => todo.completed === false);
    return `Next todo: "${nextTodo ? nextTodo.task : "<none>"}". ` +
      `Progress: ${this.completedTodosCount}/${this.todos.length}`;
  }

  addTodo(task) {
    this.todos.push({
      task: task,
      completed: false,
      assignee: null
    });
  }
}

const observableTodoStore = new ObservableTodoStore();
```

就是这样！我们将一些属性标记为可观察的，以便通知 MobX 这些值可能会随时间发生变化。计算属性使用 computed 装饰，这样可以表明它们是根据状态派生出来的，并且只要底层状态没有变化，结果就会被缓存。

pendingRequests 和 assignee 这两个属性目前还没有被使用，但在本教程后续内容中会用到。

在构造函数中，我们创建了一个用于打印报告的小函数，并将其包裹在 autorun 中。autorun 会创建一个响应函数，首次会自动运行一次，之后只要函数内部用到的任何可观察数据发生变化，它就会自动重新运行。由于 report 使用了可观察的 todos 属性，所以每当合适的时候，报告就会被打印出来。接下来的示例就演示了这一点。只需点击运行按钮即可：

```javascript
observableTodoStore.addTodo("read MobX tutorial");
observableTodoStore.addTodo("try MobX");
observableTodoStore.todos[0].completed = true;
observableTodoStore.todos[1].task = "try MobX in own project";
observableTodoStore.todos[0].task = "grok MobX tutorial";
```

很有趣，对吧？报告确实是自动、同步地打印出来的，而且没有泄露任何中间值。如果你仔细查看日志，会发现第五行并没有生成新的日志行。因为虽然底层数据发生了变化，但重命名操作实际上并没有让报告内容发生变化。另一方面，修改第一个待办事项的名称时，报告确实更新了，因为这个名称在报告中被实际使用。这很好地说明了 autorun 不仅仅是在观察 todos 数组本身，还在观察每个 todo 项内部的属性。

## 让React具备响应式能力

到目前为止，我们只是让一个简单的报表变得响应式。现在，是时候围绕同一个 store 构建一个响应式的用户界面了。React 组件（尽管名字里有“react”）本身其实并不是响应式的。mobx-react-lite 包中的 `observer` 高阶组件（HoC）可以解决这个问题，它的原理是把 React 组件包裹在 autorun 里。这样就能让组件始终与状态保持同步。其实，这和我们之前让报表响应式的做法在本质上没有区别。

下面的代码示例定义了几个 React 组件。唯一与 MobX 相关的代码就是用 observer 包裹组件。这样就足以确保每个组件在相关数据发生变化时能够单独重新渲染。我们不再需要手动调用 useState 的 setter，也不用再费心通过 selector 或需要配置的高阶组件来订阅应用状态的特定部分。基本上，所有组件都变得“智能”了，但它们的定义方式依然简单、声明式。

点击“运行代码”按钮，查看下面的代码实际效果。代码区是可编辑的，欢迎你随意尝试。例如，你可以尝试移除所有的 observer 调用，或者只移除装饰 TodoView 的那一个。右侧预览中的数字会显示每个组件被渲染的次数。

```javascript
const TodoList = observer(({store}) => {
  const onNewTodo = () => {
    store.addTodo(prompt('Enter a new todo:','coffee plz'));
  }

  return (
    <div>
      { store.report }
      <ul>
        { store.todos.map(
          (todo, idx) => <TodoView todo={ todo } key={ idx } />
        ) }
      </ul>
      { store.pendingRequests > 0 ? <marquee>Loading...</marquee> : null }
      <button onClick={ onNewTodo }>New Todo</button>
      <small> (double-click a todo to edit)</small>
      <RenderCounter />
    </div>
  );
})

const TodoView = observer(({todo}) => {
  const onToggleCompleted = () => {
    todo.completed = !todo.completed;
  }

  const onRename = () => {
    todo.task = prompt('Task name', todo.task) || todo.task;
  }

  return (
    <li onDoubleClick={ onRename }>
      <input
        type='checkbox'
        checked={ todo.completed }
        onChange={ onToggleCompleted }
      />
      { todo.task }
      { todo.assignee
        ? <small>{ todo.assignee.name }</small>
        : null
      }
      <RenderCounter />
    </li>
  );
})

ReactDOM.render(
  <TodoList store={ observableTodoStore } />,
  document.getElementById('reactjs-app')
);
```

下面的代码示例很好地说明了，我们只需修改数据，无需进行额外的记录或管理。MobX 会自动根据 store 中的状态，推导并更新用户界面的相关部分。

```javascript
const store = observableTodoStore;
store.todos[0].completed = !store.todos[0].completed;
store.todos[1].task = "Random todo " + Math.random();
store.todos.push({ task: "Find a fine cheese", completed: true });
// etc etc.. add your own statements here...
```

## 使用引用

到目前为止，我们已经创建了可观察的对象（包括原型对象和普通对象）、数组以及基本类型。你可能会好奇，MobX 是如何处理引用的？我的状态可以形成一个图结构吗？在前面的例子中，你可能注意到 todos 上有一个 assignee 属性。现在我们来给它们赋一些值——通过引入另一个“store”（其实就是一个升级版的数组）来存放人员信息，并把任务分配给他们。

```javascript
const peopleStore = observable([
  { name: "Michel" },
  { name: "Me" }
]);
observableTodoStore.todos[0].assignee = peopleStore[0];
observableTodoStore.todos[1].assignee = peopleStore[1];
peopleStore[0].name = "Michel Weststrate";
```

现在我们有了两个独立的 store，一个用于存储人员信息，另一个用于待办事项。要为待办事项分配负责人时，我们只需在 people store 中分配一个引用即可。这些更改会被 TodoView 自动检测到。使用 MobX，无需先对数据进行规范化，也不需要编写 selector 来确保组件能够及时更新。实际上，数据存储在哪里并不重要。只要对象被设置为可观察，MobX 就能追踪它们。真实的 JavaScript 引用也能正常工作。如果这些引用与派生数据相关，MobX 会自动追踪它们。

## 异步操作

由于我们这个小型待办应用中的所有内容都是由状态派生出来的，所以状态在什么时候发生变化其实并不重要。这也让创建异步操作变得非常简单。

我们首先更新 store 的 pendingRequests 属性，让界面能够反映当前的加载状态。加载完成后，我们会更新 store 中的 todos，并再次减少 pendingRequests 计数。你可以把这段代码和之前的 TodoList 定义对比一下，就能看到 pendingRequests 属性是如何被使用的。

请注意，timeout 函数被包裹在 action 中。虽然这并不是绝对必要的，但这样可以确保两个变更操作在同一个事务中处理，从而保证只有在两次更新都完成后，相关的观察者才会收到通知。

```javascript
observableTodoStore.pendingRequests++;
setTimeout(action(() => {
  observableTodoStore.addTodo('Random Todo ' + Math.random());
  observableTodoStore.pendingRequests--;
}), 2000);
```

## 结论

就这些！没有多余的模板代码。只有一些简单、声明式的组件，构成了我们完整的 UI，并且这些组件都是完全基于我们的状态响应式派生出来的。现在，你已经可以在自己的应用中开始使用 mobx 和 mobx-react-lite 这两个包了。下面是你目前学到内容的简要总结：

1. 使用 observable 装饰器或 observable（对象或数组）函数，让对象可以被 MobX 跟踪。
2. 可以用 computed 装饰器创建函数，这些函数会自动从状态中派生出值并进行缓存。
3. 使用 autorun，可以自动运行依赖某些 observable 状态的函数。这对于日志记录、发起网络请求等场景非常有用。
4. 使用 mobx-react-lite 包中的 observer 包装器，让你的 React 组件真正实现响应式。组件会自动且高效地更新，即使在大型、复杂、数据量庞大的应用中也能如此。

你可以继续在上面的可编辑代码块中多试一会儿，感受一下 MobX 对你所有更改的响应方式。比如，你可以在 report 函数里加一句日志，看看它什么时候会被调用。或者你也可以选择不显示 report，观察这对 TodoList 的渲染有什么影响。又或者，只在特定情况下才显示 report……

## MobX并不限定架构

请注意，上述示例仅为演示用途，建议在实际开发中采用更为规范的工程实践，比如将逻辑封装到方法中，并将其组织在 store、controller 或 view-model 等结构中。实际上，可以应用多种不同的架构模式，官方文档中也对其中一些做了进一步探讨。无论是上面的示例，还是官方文档中的示例，目的都是展示 MobX 的用法，而不是规定必须这样使用。正如有位 HackerNews 用户所说：

> MobX，这个库在其他地方也被提到过，但我还是忍不住要夸一夸它。用 MobX 写代码时，像控制器、分发器、动作、管理器这些用于管理数据流的东西，是否使用、怎么用，都可以根据你的应用架构需求来决定，而不是像做个待办事项（Todo）应用以外的项目时，默认就必须强制用上一套复杂的管理方案。
