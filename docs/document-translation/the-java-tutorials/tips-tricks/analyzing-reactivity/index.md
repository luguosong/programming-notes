# 分析响应性 {🚀}

## 使用trace进行调试

`trace` 是一个小型工具，可帮助你查明为何你的计算值（computed values）、响应（reactions）或组件正在被重新求值。

只需导入 `import { trace } from "mobx"`，然后将其放入某个响应或计算值内部即可使用。它会打印出当前派生（derivation）被重新求值的原因。

你还可以选择传入 `true` 作为最后一个参数，以自动进入调试器。这样，触发响应重新运行的确切变更仍会保留在调用栈中，通常位于向上约
8 层的栈帧处。参见下图。

在调试器模式下，调试信息还会揭示影响当前计算/响应的完整派生树（derivation tree）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260129135644863.png){ loading=lazy }
  <figcaption></figcaption>
</figure>

<figure markdown="span">
  ![](https://mobx.js.org/assets/trace.gif){ loading=lazy }
  <figcaption></figcaption>
</figure>

### 在线示例

一个简单的 `CodeSandbox` 上的 `trace()` 示例。

这是一个已部署的示例，用于探索 `调用栈 (stack)`。务必尝试使用 `Chrome 调试器 (Chrome debugger)` 的 `blackbox` 功能！

### 用法示例

调用 `trace()` 有不同的方式，举例如下：

``` javascript
import { observer } from "mobx-react"
import { trace } from "mobx"

const MyComponent = observer(() => {
    trace(true) // 每当某个`可观察值` (observable value) 导致此`组件` (component) 重新运行时，进入`debugger`。
    return <div>{this.props.user.name}</name>
})
```

通过使用 `reaction` / `autorun` 提供的 `reaction` 参数来启用跟踪：

``` javascript
mobx.autorun("logger", reaction => {
    reaction.trace()
    console.log(user.fullname)
})
```

传入一个计算属性的`属性名称`：

``` javascript
trace(user, "fullname") 
```

## 内部检查 API

如果你在调试时想查看 MobX 的内部状态，或希望基于 MobX 构建一些很酷的工具，下面这些 API 会很有用。同时也可以参考各种
`isObservable*` API。

`getDebugName`

用法：

- `getDebugName(thing, property?)`

返回某个可观察对象、属性、响应 (reaction)、计算 (computation) 等的（生成的）易读调试名称。例如，MobX 开发者工具就会用到它。

`getDependencyTree`

用法：

- `getDependencyTree(thing, property?).`

返回一个树形结构，包含指定的响应 (reaction) / 计算 (computation) 当前依赖的所有可观察对象。

`getObserverTree`

用法：

- `getObserverTree(thing, property?).`

返回一个树形结构，包含所有正在观察给定可观察对象的响应 (reaction) / 计算 (computation)。

`getAtom`

用法：

- `getAtom(thing, property?).`

返回给定可观察对象、属性、响应 (reaction) 等背后的 Atom。

## Spy

用法：

- `spy(listener)`

注册一个全局 `spy` 监听器，用来监听 MobX 中发生的所有事件。它有点像一次性给所有可观察对象都挂上 `observe`
监听器，但它还会通知正在运行的（事务/重新执行的）actions 以及计算 (computations)。例如，MobX 开发者工具就会用到它。

监听所有 actions 的示例用法：

``` javascript
spy(event => {
    if (event.type === "action") {
        console.log(`${event.name} with args: ${event.arguments}`)
    }
})
```

`Spy` 监听器始终会收到一个对象，这个对象通常至少包含一个 `type` 字段。默认情况下，`spy` 会发出以下事件：

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260129142150460.png){ loading=lazy }
  <figcaption></figcaption>
</figure>

报告结束事件属于之前触发的、带有 `spyReportStart: true` 的事件的一部分。该事件用于标记某个事件的结束，从而将带有子事件的事件组串联起来。它也可能会汇报总执行时间。

针对可观察值 (Observable) 的 `spy` 事件，与传递给 `observe` 的事件完全一致。在生产环境构建中，`spy` API 不会执行任何操作，因为它会在压缩优化时被最小化移除。

如需更全面的概览，请查看 `Intercept & observe {🚀}` 章节。
