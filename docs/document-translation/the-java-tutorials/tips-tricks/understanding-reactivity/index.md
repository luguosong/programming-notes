# 理解响应式

MobX 通常会**精准地**对你期望它响应的内容做出反应，这意味着在 90% 的使用场景里，MobX
基本上可以“开箱即用”。不过，总有一天你会遇到某个场景，它的行为和你的预期不一致。这时，理解 MobX 是如何判断要对什么做出响应就变得非常关键。

!!! note

	MobX 会对**在某个被追踪函数执行期间**被读取到的任何**已存在的可观察 (observable)** 属性做出响应。

- “读取 (reading)”指对对象属性进行解引用，可以通过“点访问”（例如 `user.name`）、方括号语法（例如 `user['name']`、`todos[3]`
  ），或者解构（例如 `const {name} = user`）来完成。
- “被追踪函数 (tracked functions)”包括：`computed` 的表达式、`observer` 的 React 函数组件渲染过程、基于 `observer` 的 React
  类组件的 `render()` 方法，以及作为 `autorun`、`reaction`、`when` 第一个参数传入的函数。
- “期间 (during)”意味着只会追踪函数执行过程中被读取到的那些可观察对象。无论这些值是被追踪函数直接使用还是间接使用都没关系。但从该函数“派生”出去的异步执行内容不会被追踪（例如
  `setTimeout`、`promise.then`、`await` 等）。

换句话说，MobX **不会**对以下情况做出响应：

- 从可观察对象中取得的值，但发生在被追踪函数之外
- 在异步触发的代码块中读取到的可观察对象

## MobX 追踪的是属性访问，而不是值

为了用例子更清楚地说明上述规则，假设你有如下的可观察 (observable) 实例：

``` javascript
class Message {
    title
    author
    likes
    constructor(title, author, likes) {
        makeAutoObservable(this)
        this.title = title
        this.author = author
        this.likes = likes
    }

    updateTitle(title) {
        this.title = title
    }
}

let message = new Message("Foo", { name: "Michel" }, ["Joe", "Sara"])
```

在内存中，它看起来如下所示。绿色方框表示可观察属性（observable properties）。注意：这些`值`本身并不是可观察的！

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260129131541775.png){ loading=lazy }
  <figcaption></figcaption>
</figure>

MobX 本质上做的事情，就是记录你在函数里用到了哪些 `arrow`。之后，只要这些 `arrow` 中有任何一个发生变化——也就是它们开始指向别的东西——它就会重新运行。

## 示例

我们用一组示例来说明（基于上面定义的 `message` 变量）：

**正确：在被追踪的函数内进行解引用**

``` javascript
autorun(() => {
    console.log(message.title)
})
message.updateTitle("Bar")
```

这会按预期触发响应。`title` 属性已被 `autorun` 解引用，之后又发生了变化，因此能够检测到这次变更。

你可以在被追踪的函数里调用 `trace()`，来验证 MobX 会追踪哪些内容。以上函数的场景下，它会输出如下内容：

``` javascript
import { trace } from "mobx"

const disposer = autorun(() => {
    console.log(message.title)
    trace()
})
// Outputs:
// [mobx.trace] 'Autorun@2' tracing enabled

message.updateTitle("Hello")
// Outputs:
// [mobx.trace] 'Autorun@2' is invalidated due to a change in: 'Message@1.title'
Hello
```

也可以通过使用 `getDependencyTree` 来获取内部的依赖（或观察者）树：

```javascript
import {getDependencyTree} from "mobx"

// 打印与 `disposer` 关联的 reaction 的依赖树。
console.log(getDependencyTree(disposer))
// Outputs:
// { name: 'Autorun@2', dependencies: [ { name: 'Message@1.title' } ] }
```

错误：更改了不可观察的引用（non-observable reference）

``` javascript
autorun(() => {
    console.log(message.title)
})
message = new Message("Bar", { name: "Martijn" }, ["Felicia", "Marcus"])
```

这不会触发响应。`message` 确实变了，但 `message` 本身不是可观察对象 (Observable)，它只是一个指向可观察对象的变量；而这个变量（引用）本身并不可观察。

不正确：在被跟踪函数之外解引用（dereference）

``` javascript
let title = message.title
autorun(() => {
    console.log(title)
})
message.updateMessage("Bar")
```

这段代码不会触发响应。`message.title` 是在 `autorun` 之外被解引用的，它只保留了解引用当时 `message.title` 的值（字符串
“Foo”）。`title` 不是可观察对象 (observable)，因此 `autorun` 永远不会响应。

正确做法：在被追踪的函数内部解引用。

```javascript
autorun(() => {
    console.log(message.author.name)
})

runInAction(() => {
    message.author.name = "Sara"
})
runInAction(() => {
    message.author = {name: "Joe"}
})
```

这会同时响应两种变化。`author` 和 `author.name` 都会被“点”进去访问，从而让 MobX 能跟踪这些引用。

注意，这里我们必须使用 `runInAction`，才能在 `action` 之外进行修改。

错误示例：在不进行跟踪的情况下，把对某个可观察对象 (observable object) 的本地引用缓存起来

``` javascript
const author = message.author
autorun(() => {
    console.log(author.name)
})

runInAction(() => {
    message.author.name = "Sara"
})
runInAction(() => {
    message.author = { name: "Joe" }
})
```

第一个改动会被捕获：`message.author` 和 `author` 是同一个对象，并且在 `autorun` 中解引用了 `.name` 属性。不过，第二个改动不会被捕获，因为
`autorun` 并没有跟踪 `message.author` 这个关系。`autorun` 仍然在使用“旧的” `author`。

常见坑：`console.log`

``` javascript
autorun(() => {
    console.log(message)
})

// 不会触发重新运行。
message.updateTitle("Hello world")
```

在上面的例子里，更新后的消息标题不会被打印出来，因为它并没有在 `autorun` 里被使用。`autorun` 只依赖 `message`，而 `message`
不是一个可观察值（observable），只是一个普通变量。换句话说，对 MobX 来说，`autorun` 里并没有用到 `title`。

如果你在浏览器的调试工具里查看，可能最终还是能看到 `title` 的更新值，但这其实会让人误解——`autorun`
确实只在第一次调用时运行了一次。之所以会这样，是因为 `console.log` 是异步函数，对象会在稍后的时间点才被格式化展示。这意味着，如果你在调试面板里展开并追踪
`title`，会看到更新后的值，但 `autorun` 并不会跟踪任何更新。

要让它按预期工作，关键是确保传给 `console.log` 的始终是不可变数据，或是经过防御性拷贝的数据。因此，下面这些方案都会对
`message.title` 的变化做出响应：

``` javascript
autorun(() => {
    console.log(message.title) // 显然，使用了 `.title` `observable`。
})

autorun(() => {
    console.log(mobx.toJS(message)) // `toJS` 会创建一个深度克隆，因此会读取该消息。
})

autorun(() => {
    console.log({ ...message }) // 创建一个浅克隆，同时在过程中也会使用 `.title`。
})

autorun(() => {
    console.log(JSON.stringify(message)) // 也会读取整个结构。
})
```

正确：在跟踪函数中访问数组属性

``` javascript
autorun(() => {
    console.log(message.likes.length)
})
message.likes.push("Jennifer")
```

这会按预期触发响应。`.length`
会被计入属性依赖。注意，只要数组发生任何变化，这里都会触发响应。数组不会按索引/属性分别跟踪（不像可观察对象 (observable
objects) 和映射 (maps)），而是作为一个整体进行跟踪。

错误：在被跟踪函数中访问越界索引

```javascript
autorun(() => {
    console.log(message.likes[0])
})
message.likes.push("Jennifer")
```

这会对上面的示例数据产生响应，因为数组下标也算作属性访问。但前提是你提供的下标 `< length`。MobX
不会跟踪尚不存在的数组索引。因此，凡是通过数组下标访问，都要先用 `.length` 做边界检查。

正确做法：在被跟踪的函数里访问数组函数。

```javascript
autorun(() => {
    console.log(message.likes.join(", "))
})
message.likes.push("Jennifer")
```

这会按预期运行。所有**不会修改数组本身**的数组函数都会被自动跟踪。

```javascript
autorun(() => {
    console.log(message.likes.join(", "))
})
message.likes[2] = "Jennifer"
```

这会按预期响应。所有对数组索引的赋值都会被检测到，但前提是索引 `index` <= 长度 `length`。

不正确：只是“使用”了一个可观察对象 (observable)，却没有访问它的任何属性。

``` javascript
autorun(() => {
    message.likes
})
message.likes.push("Jennifer")
```

这不会触发响应。原因很简单：`autorun` 并没有直接使用 `likes` 数组本身，而只是使用了对该数组的引用。因此，相比之下，
`message.likes = ["Jennifer"]` 会被捕获；这条语句并不是在修改数组内容，而是在修改 `likes` 属性本身。

正确做法：使用尚不存在的 `map` 条目（entries）

``` javascript
const twitterUrls = observable.map({
    Joe: "twitter.com/joey"
})

autorun(() => {
    console.log(twitterUrls.get("Sara"))
})

runInAction(() => {
    twitterUrls.set("Sara", "twitter.com/horsejs")
})
```

这会触发响应。`observable map` 支持监听那些可能尚不存在的条目。注意：这里一开始会打印 `undefined`。你可以先用
`twitterUrls.has("Sara")` 检查该条目是否存在。因此，在一个对“动态键集合”缺少 `Proxy` 支持的环境中，务必使用
`observable map`。如果你确实有 `Proxy` 支持，也同样可以使用 `observable map`，但你也可以选择使用普通对象。

MobX 不会跟踪以异步方式访问的数据。

``` javascript
function upperCaseAuthorName(author) {
    const baseName = author.name
    return baseName.toUpperCase()
}
autorun(() => {
    console.log(upperCaseAuthorName(message.author))
})

runInAction(() => {
    message.author.name = "Chesterton"
})
```

这会触发响应。即使传给 `autorun` 的函数本身没有解引用 `author.name`，MobX 仍然会追踪 `upperCaseAuthorName` 中发生的解引用，因为它是在
`autorun` 执行期间发生的。

---

``` javascript
autorun(() => {
    setTimeout(() => console.log(message.likes.join(", ")), 10)
})

runInAction(() => {
    message.likes.push("Jennifer")
})
```

这不会触发响应，因为在执行 `autorun` 的过程中并没有访问任何可观察对象 (observable)；只有在 `setTimeout`（一个异步 (
asynchronous) 函数）里才访问到了。

也请一并查看“异步操作 (Asynchronous actions)”这一节。

使用非可观察对象 (non-observable) 的属性

``` javascript
autorun(() => {
    console.log(message.author.age)
})

runInAction(() => {
    message.author.age = 10
})
```

如果你在支持 `Proxy` 的环境里运行 React，这段代码会产生响应。注意：这只对通过 `observable` 或 `observable.object`
创建的对象生效。类实例上的新属性不会被自动设为可观察 (observable)。

不支持 `Proxy` 的环境

这段代码不会产生响应。MobX 只能跟踪可观察 (observable) 属性，而上面的 `age` 并没有被定义为可观察 (observable) 属性。

不过，你可以使用 MobX 暴露的 `get` 和 `set` 方法来绕过这个限制：

``` javascript
import { get, set } from "mobx"

autorun(() => {
    console.log(get(message.author, "age"))
})
set(message.author, "age", 10)
```

`[不支持代理 (Proxy)]` 错误：使用了尚未存在的可观察对象属性

``` javascript
autorun(() => {
    console.log(message.author.age)
})
extendObservable(message.author, {
    age: 10
})
```

这不会触发响应。MobX 不会对在追踪开始时尚不存在的可观察 (observable)
属性做出响应。如果把这两条语句对调，或者有任何其他可观察 (observable) 数据导致 `autorun` 重新执行，那么 `autorun` 也会开始追踪
`age`。

[无 Proxy 支持] 正确做法：使用 MobX 工具函数来读取/写入对象

如果你所处的环境不支持 Proxy，但仍希望把可观察 (observable) 对象当作动态集合来使用，可以通过 MobX 的 `get` 和 `set` API
来处理它们。

下面的示例同样会触发响应：

``` javascript
import { get, set, observable } from "mobx"

const twitterUrls = observable.object({
    Joe: "twitter.com/joey"
})

autorun(() => {
    console.log(get(twitterUrls, "Sara")) // `get` 可以跟踪尚未存在的属性。
})

runInAction(() => {
    set(twitterUrls, { Sara: "twitter.com/horsejs" })
})
```

查看 `Collection` 工具类 API 了解更多细节。

!!! note

    MobX 会对在受跟踪函数执行期间被读取的任何已有可观察属性 (observable property) 作出响应。
