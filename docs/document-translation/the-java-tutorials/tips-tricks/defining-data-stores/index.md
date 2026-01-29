# 定义数据存储

本节总结了我们在 Mendix 使用 MobX 构建**大规模、易维护**项目时发现的一些最佳实践。本节带有一定主观倾向，你完全不必强制采用这些做法。使用
MobX 和 React 的方式有很多种，这只是其中一种。

本节聚焦于一种对现有代码侵入性很低的 MobX 使用方式，它在既有代码库中表现良好，也适用于经典的 MVC 模式。另一类更“有主见”的组织方式包括
mobx-state-tree 和 mobx-keystone。它们开箱即用地提供了很多很酷的特性，例如结构共享的快照 (`structurally shared snapshots`)
、动作中间件 (`action middlewares`)、JSON Patch 支持等。

## Store

在任何 Flux 架构中都能看到 `store`，它们有点类似 MVC 模式里的控制器。`store`
的主要职责，是把逻辑和状态从组件中移出，放到一个独立、可测试的单元里，该单元既可用于前端，也可用于后端 JavaScript。

大多数应用至少会从两个 `store` 中受益：一个用于领域状态 (`domain state`)，另一个用于 UI 状态 (`UI state`)
。将二者分离的好处是：你可以以通用方式复用并测试领域状态，并且很可能还能在其他应用中复用它。

## 领域 Stores

你的应用会包含一个或多个领域 `store`。这些 `store` 保存的是应用真正关心的数据：待办事项、用户、图书、电影、订单等等。你的应用几乎肯定至少会有一个领域
`store`。

一个领域 `store` 应该只负责应用中的一个概念。单个 `store` 往往会组织成树状结构，内部包含多个领域对象 (`domain objects`)。

例如：产品使用一个领域 `store`，订单与订单行使用另一个。经验法则是：如果两个条目之间的关系本质上是“包含”，通常就应该放在同一个
`store` 里。因此，`store` 只负责管理领域对象。

`store` 的职责包括：

- 实例化领域对象。确保领域对象知道自己属于哪个 `store`。
- 确保每个领域对象在内存中只有一个实例。同一个用户、订单或待办事项不应在内存里存两份。这样你就可以安全地使用引用，并确信看到的是最新实例，而无需解析引用。这既快、直接，也便于调试。
- 提供后端集成。在需要时持久化数据。
- 如果从后端收到更新，更新现有实例。
- 提供一个独立、通用、可测试的应用组件。
- 为了让 `store` 可测试并能在服务端运行，你很可能会把实际的 websocket / http 请求挪到一个单独对象中，从而抽象通信层。
- 每个 `store` 只应有一个实例。

### 领域对象

每个领域对象都应该用自己的类（或构造函数）来表达。没有必要把客户端应用状态当成某种数据库。真实引用、循环数据结构以及实例方法，都是
JavaScript 中非常强大的概念。领域对象允许直接引用来自其他 `store` 的领域对象。记住：我们希望让 `action` 和 `view`
尽可能简单；如果还得自己管理引用并做垃圾回收，那可能反而是在倒退。不同于 Redux 等许多 Flux 架构，在 MobX
中无需对数据做归一化 (`normalize`)；这会让你更容易构建应用中本就复杂的部分：业务规则、`action` 和用户界面。

如果你的应用适合，领域对象也可以把所有逻辑都委托给它所属的 `store`。当然也可以把领域对象表达为普通对象 (`plain objects`)
，但类相对于普通对象有一些重要优势：

- 可以拥有方法。这让领域概念更容易被独立使用，也减少了应用对上下文的依赖。你只需要传递对象即可；不必到处传 `store`
  ，也不必再去判断某个对象能应用哪些 `action`——因为它们直接作为实例方法存在。这在大型应用中尤其重要。
- 能对属性和方法的可见性进行更细粒度的控制。
- 通过构造函数创建的对象，可以自由混用可观测属性与方法 (`observable`) 以及不可观测的属性与方法。
- 更容易识别，并且可以进行严格的类型检查。

### 领域store示例

```javascript
import {makeAutoObservable, runInAction, reaction} from "mobx"
import uuid from "node-uuid"

export class TodoStore {
    authorStore
    transportLayer
    todos = []
    isLoading = true

    constructor(transportLayer, authorStore) {
        makeAutoObservable(this)
        this.authorStore = authorStore // 可解析作者的存储。
        this.transportLayer = transportLayer // 可以发起服务器请求的东西。
        this.transportLayer.onReceiveTodoUpdate(updatedTodo =>
            this.updateTodoFromServer(updatedTodo)
        )
        this.loadTodos()
    }

    // 从服务器获取所有 `Todo`。
    loadTodos() {
        this.isLoading = true
        this.transportLayer.fetchTodos().then(fetchedTodos => {
            runInAction(() => {
                fetchedTodos.forEach(json => this.updateTodoFromServer(json))
                this.isLoading = false
            })
        })
    }

    // 使用来自服务器的信息更新一个 `Todo`。确保每个 `Todo` 只会存在一份。
    // 可能会新建一个 `Todo`、更新已有的 `Todo`，或者在该 `Todo` 已在服务器端被删除时将其移除。
    updateTodoFromServer(json) {
        let todo = this.todos.find(todo => todo.id === json.id)
        if (!todo) {
            todo = new Todo(this, json.id)
            this.todos.push(todo)
        }
        if (json.isDeleted) {
            this.removeTodo(todo)
        } else {
            todo.updateFromJson(json)
        }
    }

    // 在客户端和服务器端创建一个全新的 `Todo`。
    createTodo() {
        const todo = new Todo(this)
        this.todos.push(todo)
        return todo
    }

    // 某个 `Todo` 不知为何被删除了，把它从客户端内存中清理掉。
    removeTodo(todo) {
        this.todos.splice(this.todos.indexOf(todo), 1)
        todo.dispose()
    }
}

// `Todo` 领域对象。
export class Todo {
    id = null // 此 `Todo` 的唯一 `id`，不可变。
    completed = false
    task = ""
    author = null // 对 `Author` 对象的引用（来自 `authorStore`）。
    store = null
    autoSave = true // 用于指示将此 Todo 中的更改提交到服务器的标记。
    saveHandler = null // 处理自动保存此 Todo 的副作用（dispose）。

    constructor(store, id = uuid.v4()) {
        makeAutoObservable(this, {
            id: false,
            store: false,
            autoSave: false,
            saveHandler: false,
            dispose: false
        })
        this.store = store
        this.id = id

        this.saveHandler = reaction(
            () => this.asJson, // 观察 JSON 中使用到的所有内容。
            json => {
                // 如果 `autoSave` 为 true，就把 JSON 发送到服务器。
                if (this.autoSave) {
                    this.store.transportLayer.saveTodo(json)
                }
            }
        )
    }

    // 从客户端和服务器端移除这个 `Todo`。
    delete() {
        this.store.transportLayer.deleteTodo(this.id)
        this.store.removeTodo(this)
    }

    get asJson() {
        return {
            id: this.id,
            completed: this.completed,
            task: this.task,
            authorId: this.author ? this.author.id : null
        }
    }

    // 用服务器端的信息更新此 `Todo`。
    updateFromJson(json) {
        this.autoSave = false // 防止把我们的更改回传到服务器。
        this.completed = json.completed
        this.task = json.task
        this.author = this.store.authorStore.resolveAuthor(json.authorId)
        this.autoSave = true
    }

    // 清理 `observer`。
    dispose() {
        this.saveHandler()
    }
}
```

## UI 存储

`ui-state-store` 往往非常贴合你的应用，但通常也很简单。这个存储一般不包含太多逻辑，而是用来保存大量彼此松散耦合的 UI
信息。这一点非常理想，因为大多数应用在开发过程中会频繁变更 UI 状态。

你通常会在 UI 存储里看到：

- 会话信息
- 应用加载进度相关信息
- 不会存到后端的信息
- 影响全局 UI 的信息
	- 窗口尺寸
	- 无障碍信息 (Accessibility)
	- 当前语言
	- 当前启用的主题
- 一旦 UI 状态开始影响多个、且彼此无关的组件，就应放在这里：
	- 当前选中项
	- 工具栏等的可见性
	- 向导流程的状态
	- 全局遮罩层的状态

这些信息很可能一开始只是某个特定组件的内部状态（例如工具栏的可见性），但过一段时间你会发现，在应用的其他地方也需要用到它。遇到这种情况，与其像在纯
React 应用里那样把状态沿着组件树向上提升，不如直接把这部分状态移动到 `ui-state-store` 中。

对于同构应用 (Isomorphic) 来说，你可能还需要为这个存储提供一个带有合理默认值的桩实现 (Stub)，以便所有组件都能按预期渲染。你也可以通过把
`ui-state-store` 作为 React 上下文 (React Context) 传递的方式，在整个应用中分发它。

使用 ES6 语法的存储示例：

``` javascript
import { makeAutoObservable, observable, computed } from "mobx"

export class UiState {
    language = "en_US"
    pendingRequestCount = 0

    // `.struct` 确保除非 `dimensions` 对象以 `deepEqual` 的方式发生变化，
    // 否则不会触发 `observer` 的通知。
    windowDimensions = {
        width: window.innerWidth,
        height: window.innerHeight
    }

    constructor() {
        makeAutoObservable(this, { windowDimensions: observable.struct })
        window.onresize = () => {
            this.windowDimensions = getWindowDimensions()
        }
    }

    get appIsInSync() {
        return this.pendingRequestCount === 0
    }
}
```

## 组合多个 store

一个常见问题是：在不使用单例 (Singleton) 的情况下，如何组合多个 store？它们如何彼此感知并协作？

一种行之有效的模式是创建一个 `RootStore`，由它来实例化所有 store，并在它们之间共享引用。该模式的优势是：

1. 易于搭建。
2. 对强类型支持很好。
3. 复杂的单元测试更容易：你只需要实例化一个根 store 即可。

示例：

``` javascript
class RootStore {
    constructor() {
        this.userStore = new UserStore(this)
        this.todoStore = new TodoStore(this)
    }
}

class UserStore {
    constructor(rootStore) {
        this.rootStore = rootStore
    }

    getTodos(user) {
        // Access todoStore through the root store.
        return this.rootStore.todoStore.todos.filter(todo => todo.author === user)
    }
}

class TodoStore {
    todos = []
    rootStore

    constructor(rootStore) {
        makeAutoObservable(this)
        this.rootStore = rootStore
    }
}
```

在使用 React 时，通常会通过 React `context` 将这个根 `store` 注入到组件树中。


