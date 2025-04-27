# Mobx

## 入门案例

``` html title="hello.html"
--8<-- "code/front_end/react/global-state-management/mobx/example/hello.html"
```

## 创建可观察状态

### 注解

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202501312100302.png){ loading=lazy }
  <figcaption>可用的注解</figcaption>
</figure>

### makeObservable

`makeObservable(target, annotations?, options?)`

```js
import { makeObservable, observable, computed, action, flow } from "mobx"

class Doubler {
    value=1;

    constructor(value) {
        makeObservable(this, {
            value: observable,
            double: computed,
            increment: action,
            fetch: flow
        })
        this.value = value
    }

    get double() {
        return this.value * 2
    }

    increment() {
        this.value++
    }

    *fetch() {
        const response = yield fetch("/api/value")
        this.value = response.json()
    }
}
```

### makeAutoObservable

`makeAutoObservable(target, overrides?, options?)`

`makeAutoObservable` 就像是加强版的 `makeObservable`，在默认情况下它将推断所有的属性。

!!! warning

	makeAutoObservable 不能被用于带有 super 的类或 子类。

推断规则：

- 所有 自有 属性都成为 observable。
- 所有 getters 都成为 computed。
- 所有 setters 都成为 action。
- 所有 prototype 中的 functions 都成为 autoAction。
- 所有 prototype 中的 generator functions 都成为 flow。（需要注意，generators 函数在某些编译器配置中无法被检测到，如果 flow 没有正常运行，请务必明确地指定 flow 注解。）
- 在 overrides 参数中标记为 false 的成员将不会被添加注解。例如，将其用于像标识符这样的只读字段。

```js
class Doubler {
    value = 1;

    constructor(value) {
        makeAutoObservable(this)
        this.value = value
    }

    get double() {
        return this.value * 2
    }

    increment() {
        this.value++
    }
}
```

```js title="另一种方式"
import { makeAutoObservable } from "mobx"

function createDoubler(value) {
    return makeAutoObservable({
        value:1,
        get double() {
            return this.value * 2
        },
        increment() {
            this.value++
        }
    })
}
```

### observable

`observable(source, overrides?, options?)`

observable 注解可以作为一个函数进行调用，从而一次性将整个对象变成可观察的。

observable 支持为对象添加（和删除）字段。 这使得 observable 非常适合用于像动态键控的对象、数组、Maps 和 Sets 之类的集合。

由 observable 返回的对象将会使用 Proxy 包装，这意味着之后被添加到这个对象中的属性也将被侦测并使其转化为可观察对象（除非禁用 proxy）。

```js
import { observable } from "mobx"

const todosById = observable({
    "TODO-123": {
        title: "find a decent task management system",
        done: false
    }
})

todosById["TODO-456"] = {
    title: "close all tickets older than two weeks",
    done: true
}

const tags = observable(["high prio", "medium prio", "low prio"])
tags.push("prio: for fun")
```

## 集成React

### 入门案例

```jsx
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

//被`observer`包裹的函数式组件会被监听在它每一次调用前发生的任何变化
const TimerView = observer(({ timer }) => <span>Seconds passed: {timer.secondsPassed}</span>)

ReactDOM.render(<TimerView timer={myTimer} />, document.body)

setInterval(() => {
    myTimer.increaseTimer()
}, 1000)
```


