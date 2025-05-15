# MobX核心

## 创建可观察状态(Observable state)

属性、整个对象、数组、Map 和 Set 都可以被设置为`可观察的`。让对象变为可观察的基本方法是通过 `makeObservable`
为每个属性指定注解。最重要的注解包括：

• `observable`：定义一个可追踪的字段，用于存储状态；  
• `action`：将某个方法标记为 action，表示该方法会修改状态；  
• `computed`：将 getter 标记为 computed，用于根据状态派生新数据，并对结果进行缓存。

### 示例

=== "class + makeObservable"

	``` javascript
	import { makeObservable, observable, computed, action, flow } from "mobx"
	
	class Doubler {
	    value
	
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

	所有带注解的字段均不可配置(non-configurable)。
	
	所有不可观察（无状态）的字段（如 action、flow）均不可写(non-writable)。

=== "class + decorators"

	在使用现代装饰器时，无需调用 `makeObservable`，下面是一个基于装饰器的类示例。请注意，`@observable 注解`应始终与 `accessor 关键字`一起使用。
	
	``` javascript
	import { observable, computed, action, flow } from "mobx"
	
	class Doubler {
	    @observable accessor value
	
	    constructor(value) {
	        this.value = value
	    }
	
	    @computed
	    get double() {
	        return this.value * 2
	    }
	
	    @action
	    increment() {
	        this.value++
	    }
	
	    @flow
	    *fetch() {
	        const response = yield fetch("/api/value")
	        this.value = response.json()
	    }
	}
	```

=== "factory function + makeAutoObservable"

	``` javascript
	import { makeAutoObservable } from "mobx"
	
	function createDoubler(value) {
	    return makeAutoObservable({
	        value,
	        get double() {
	            return this.value * 2
	        },
	        increment() {
	            this.value++
	        }
	    })
	}
	```
	
	请注意，类同样可以使用 makeAutoObservable。示例中的区别只是为了展示 MobX 如何适用于不同的编程风格。

=== "observable"

	``` javascript
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
	
	与第一个使用 `makeObservable` 的例子不同，observable 支持为对象动态添加或移除字段。这使得 observable 非常适合用于动态键值对象、数组、Map 和 Set 等集合类型。

=== "class + decorators (legacy)"

	要使用传统的装饰器，需在构造函数中调用 makeObservable(this)，以确保装饰器能够正常工作。
	
	``` javascript
	import { observable, computed, action, flow } from "mobx"
	
	class Doubler {
	    @observable value
	
	    constructor(value) {
	        makeObservable(this)
	        this.value = value
	    }
	
	    @computed
	    get double() {
	        return this.value * 2
	    }
	
	    @action
	    increment() {
	        this.value++
	    }
	
	    @flow
	    *fetch() {
	        const response = yield fetch("/api/value")
	        this.value = response.json()
	    }
	}
	```

#### makeObservable

用法：

- `makeObservable(target, annotations?, options?)`

此函数可用于让已有对象的属性变为可观察的。你可以将任何 JavaScript 对象（包括类的实例）作为 target 传入。通常，makeObservable
会在类的构造函数中使用，第一个参数为 this。annotations 参数用于为每个成员指定注解，只有被注解的成员才会受到影响。

另外，也可以在类成员上使用像 `@observable` 这样的装饰器，而不必在构造函数中调用 `makeObservable`。

派生信息并接受参数的方法（例如 findUsersOlderThan(age: number): User[]）不能被标注为 computed —— 当它们在 reaction
中被调用时，其读取操作依然会被追踪，但其输出不会被缓存，以避免内存泄漏。如果你需要对这类方法进行缓存，可以使用 [MobX-utils 的
computedFn](https://github.com/mobxjs/mobx-utils#computedfn) {🚀}。

通过使用 `override 注解`，可以实现子类化，但存在一定的限制（具体示例见[此处](https://mobx.js.org/subclassing.html)）。

#### makeAutoObservable

用法：

- `makeAutoObservable(target, overrides?, options?)`

`makeAutoObservable` 可以说是功能更强大的 makeObservable，因为它默认会`自动推断所有属性`。不过，你也可以通过 overrides
参数来用特定的注解覆盖默认行为——特别是，可以用 false 来完全排除某个属性或方法不被处理。具体用法可以参考上面的代码示例。

makeAutoObservable 函数相比 makeObservable 更加简洁，也更易于维护，因为新增成员无需显式声明。不过，makeAutoObservable 不能用于包含
super 或被继承的类。

推断规则：

• 所有自有属性都会变为可观察的（observable）。
• 所有 getter 会变为计算属性（computed）。
• 所有 setter 会变为动作（action）。
• 所有函数会变为自动动作（autoAction）。
• 所有生成器函数会变为 flow。（注意，在某些转译器配置下，生成器函数可能无法被检测到。如果 flow 没有按预期工作，请确保显式指定
flow。）
• 在 overrides 参数中被标记为 false 的成员不会被注解。例如，可用于只读字段，如标识符。

#### observable

用法

- `observable(source, overrides?, options?)`
- `@observable accessor (field decorator)`

observable 注解也可以作为一个函数调用，从而一次性让整个对象变为可观察的。源对象会被克隆，所有成员都会被设为可观察，这与
makeAutoObservable 的做法类似。同样，你也可以通过传入一个 overrides 映射，来为特定成员指定注解。具体用法可以参考上面的代码示例。

observable 返回的对象将会是一个 Proxy，这意味着即使是后续添加到该对象的属性，也同样会被监听并变为可观察的（除非禁用了 Proxy
的使用）。

observable 方法同样可以用于数组、Map 和 Set 等集合类型。这些集合会被克隆，并转换为对应的可观察对象。

以下示例创建了一个 observable，并通过 autorun 进行监听。对于 Map 和 Set 集合的操作方式也类似。

```javascript
import {observable, autorun} from "mobx"

const todos = observable([
    {title: "Spoil tea", completed: true},
    {title: "Make coffee", completed: false}
])

autorun(() => {
    console.log(
        "Remaining:",
        todos
            .filter(todo => !todo.completed)
            .map(todo => todo.title)
            .join(", ")
    )
})
// Prints: 'Remaining: Make coffee'

todos[0].completed = false
// Prints: 'Remaining: Spoil tea, Make coffee'

todos[2] = {title: "Take a nap", completed: false}
// Prints: 'Remaining: Spoil tea, Make coffee, Take a nap'

todos.shift() //移除数组的第一个元素
// Prints: 'Remaining: Make coffee, Take a nap'
```

可观察数组还提供了一些非常实用的辅助函数：

- `clear()`：移除数组中的所有当前元素。
- `replace(newItems)`：用新元素替换数组中所有现有元素。
- `remove(value)`：根据值移除数组中的单个元素。如果找到并成功移除该元素，则返回 true。

!!! note "原始类型和类实例不会被转换为可观察对象"

	由于 JavaScript 中的原始值是不可变的，MobX 无法将其设为可观察对象（不过可以通过封装实现）。不过，除了在一些库中，这种机制通常用处不大。

	将类实例传递给 observable 或赋值给可观察属性时，系统不会自动将其变为可观察对象。使类成员可观察应由类的构造函数负责。

!!! note "observable（代理模式）与 makeObservable（非代理模式）🚀"

	make(Auto)Observable 和 observable 之间的主要区别在于，前者会直接修改你作为第一个参数传入的对象，而后者则会创建一个可观察的克隆对象。

	第二个区别在于，observable 会创建一个 Proxy 对象，这样可以拦截未来对属性的新增操作，适用于你将对象用作动态查找映射的场景。如果你要变为可观察的对象结构比较固定，所有成员在一开始就已知，我们建议使用 makeObservable，因为非 Proxy 对象的性能会稍好一些，而且在调试器或 console.log 中也更容易查看。

	因此，在工厂函数中推荐使用 make(Auto)Observable 这个 API。需要注意的是，可以通过向 observable 传递 { proxy: false } 选项，来获取一个非代理的克隆对象。

### 可用注解

| 注解                         | 描述                                                                                                                                   |
|----------------------------|--------------------------------------------------------------------------------------------------------------------------------------|
| observable observable.deep | 定义一个可追踪的字段用于存储状态。如果可能，赋值给 observable 的任何值都会根据其类型自动转换为（深度）observable、autoAction 或 flow。只有普通对象、数组、Map、Set、函数和生成器函数可以被转换。类实例及其他类型则保持不变。 |
| observable.ref	            | 类似于 observable，但只会跟踪重新赋值操作。被赋的值会被完全忽略，不会自动转换为 observable、autoAction 或 flow。例如，如果你打算在 observable 字段中存储不可变数据，可以使用这种方式。                 |
| observable.shallow         | 类似于 observable.ref，但用于集合类型。被赋值的集合本身会变为可观察对象，但集合内部的元素不会自动变为可观察的。                                                                      |
| observable.struct          | 与 observable 类似，但如果赋予的值在结构上等同于当前值，则会被忽略。                                                                                             |
| action                     | 将某个方法标记为会修改状态的 action。更多详情请参见 actions。该属性不可写。                                                                                        |
| action.bound               | 类似于 action，但还会将该 action 绑定到实例上，因此 this 始终会被设置。不可写。                                                                                   |
| computed                   | 可以用于 getter 上，将其声明为可缓存的派生值。更多详情请参见 computeds。                                                                                        |
| computed.struct            | 与 computed 类似，但如果重新计算后的结果在结构上与之前的结果相同，则不会通知任何观察者。                                                                                    |
| true                       | 推断最佳注解。更多详情请参考 makeAutoObservable。                                                                                                   |
| false                      | 请勿为此属性添加注解。                                                                                                                          |
| flow                       | 创建一个用于管理异步流程的 flow。更多详情请参考 flow。请注意，TypeScript 推断的返回类型可能不准确。该属性为只读。                                                                  |
| flow.bound                 | 类似于 flow，但还会将 flow 绑定到该实例上，因此 this 始终会被设置。不可写。                                                                                       |
| override	                  | 适用于被子类重写的继承 action、flow、computed 和 action.bound。                                                                                     |
| autoAction                 | 不应被直接使用，但在底层会被 makeAutoObservable 调用，用于标记那些根据调用上下文可以作为 action 或 derivation 的方法。函数究竟是 derivation 还是 action，将在运行时动态判断。                 |

### 局限性

1. make(Auto)Observable 只支持已经定义的属性。请确保你的编译器配置正确，或者作为变通方法，在使用 make(Auto)Observable
   之前为所有属性赋值。若配置不正确，像 class X { y; } 这样声明但未初始化的字段将无法被正确识别。
2. makeObservable 只能标注其自身类定义中声明的属性。如果父类或子类引入了可观察字段，必须分别为这些属性调用 makeObservable。
3. options 参数只能传递一次。传递的 options 是“粘性的”，之后无法更改（例如在子类中）。
4. 每个字段只能被标注一次（重写除外）。字段的注解或配置在子类中不能更改。
5. 非普通对象（类）的所有被标注字段都是不可配置的。可通过 configure({ safeDescriptors: false }) 关闭此限制。
6. 所有非 observable（无状态）的字段（如 action、flow）都是不可写的。可通过 configure({ safeDescriptors: false }) 关闭此限制。
7. 只有定义在原型上的 action、computed、flow、action.bound 可以被子类重写。
8. 默认情况下，TypeScript 不允许你标注私有字段。可以通过显式将相关私有字段作为泛型参数传递来解决，例如：
   makeObservable<MyStore, "privateField" | "privateField2">(this, { privateField: observable, privateField2:
   observable })
9. 调用 make(Auto)Observable 并传递注解时，必须无条件执行，这样才能缓存推断结果。
10. 在调用 make(Auto)Observable 之后修改原型是不被支持的。
11. EcmaScript 私有字段（#field）不被 make(Auto)Observable 支持。请使用 auto-accessor + Stage-3 装饰器（@observable accessor
	#field）语法。否则，在 TypeScript 中建议使用 private 修饰符。
12. 在单一继承链中混用注解和装饰器是不被支持的，例如不能为父类使用装饰器、为子类使用注解。
13. makeObservable、extendObservable 不能用于其他内置 observable 类型（如 ObservableMap、ObservableSet、ObservableArray 等）。
14. makeObservable(Object.create(prototype)) 会将原型上的属性复制到新创建的对象上并使其可观察。此行为不正确、不可预期，因此已被弃用，未来版本可能会更改。请勿依赖此行为。

### 选项🚀

上述 API 接受一个可选的 options 参数，该参数是一个对象，支持以下选项：

- `autoBind`: true 时，默认使用 action.bound/flow.bound，而不是 action/flow。不会影响已经显式注解的成员。
- `deep`: false 时，默认使用 observable.ref，而不是 observable。不会影响已经显式注解的成员。
- `name`: <string> 为对象指定一个调试名称，该名称会在错误信息和反射 API 中显示。
- `proxy`: false 时，强制 observable(thing) 使用非 Proxy 实现。如果对象的结构不会随时间变化，建议使用此选项，因为非 Proxy
  对象更易于调试且性能更高。此选项不适用于 make(Auto)Observable，详情见 avoiding proxies。

!!! note

	options 参数只能在目标对象尚未被观察时提供。一旦可观察对象被初始化，就无法更改 options。options 会存储在目标对象上，并在后续的 makeObservable 或 extendObservable 调用中被遵循。在子类中不能传递不同的 options。

### 将可观察对象转换回原生JavaScript集合

有时候，我们需要将可观察的数据结构转换回普通的数据结构。例如，在将可观察对象传递给无法跟踪可观察对象的 React
组件时，或者当你需要一个不再被进一步修改的克隆副本时，就需要这样做。

要对集合进行浅层转换，通常可以使用常规的 JavaScript 方法：

```javascript
const plainObject = {...observableObject}
const plainArray = observableArray.slice()
const plainMap = new Map(observableMap)
```

要递归地将数据树转换为普通对象，可以使用 [toJS 工具](https://mobx.js.org/api.html#tojs)。对于类，建议实现一个 toJSON()
方法，这样在使用 JSON.stringify 时会自动调用。

### 关于类的简要说明

到目前为止，上面的例子大多偏向于使用类语法。其实，MobX 本身对此并没有明确的倾向，实际上也有很多 MobX
用户更喜欢用普通对象。不过，使用类有一些小优势，比如它们的 API 更容易被发现，尤其是在 TypeScript 里。此外，instanceof
检查在类型推断时非常强大，而且类实例不会被 Proxy 包裹，这让调试体验更好。最后，由于类的结构可预测、方法共享在原型上，JavaScript
引擎对类做了很多优化。但如果过度使用继承，很容易踩坑，所以如果你用类，建议保持简单。因此，虽然我们稍微倾向于使用类，但如果你觉得其他风格更适合自己，也完全可以选择不用类。

## 使用动作(Actions)更新状态

用法：

- `action (annotation)`
- `action(fn)`
- `action(name, fn)`
- `@action (method / field decorator)`

所有应用程序都有动作。`动作(Actions)`指的是任何会改变状态的代码。原则上，动作总是在响应某个事件时发生的，比如按钮被点击、输入内容发生变化、收到
websocket 消息等等。

MobX 要求你声明你的 actions，虽然 makeAutoObservable 可以自动化大部分工作。使用 actions 不仅有助于你更好地组织代码，还能带来以下性能优势：

1. 它们在事务中运行。在最外层的 action 执行完成之前，不会触发任何响应，这保证了 action 执行过程中产生的中间或不完整的值不会被应用的其他部分看到，直到
   action 完成为止。
2. 默认情况下，不允许在 action 之外修改状态。这有助于你在代码中清晰地定位状态更新发生的位置。

action 注解只应用于那些需要修改状态的函数。用于派生信息（如查找或过滤数据）的函数不应被标记为 action，这样 MobX 才能追踪它们的调用。被
action 注解的成员将不会被枚举。

### 示例

=== "makeObservable"

	``` javascript
	import {makeObservable, observable, action} from "mobx"
	
	class Doubler {
	    value = 0
	
	    constructor() {
	        makeObservable(this, {
	            value: observable,
	            increment: action
	        })
	    }
	
	    increment() {
	        // 中间状态不会对观察者可见。
	        this.value++
	        this.value++
	    }
	}
	```

=== "@action"

    ``` javascript
    import {observable, action} from "mobx"
    
    class Doubler {
        @observable accessor value = 0
    
        @action increment() {
            // 中间状态不会对观察者可见。
            this.value++
            this.value++
        }
    }
    ```

=== "makeAutoObservable"

    ``` javascript
    import { makeAutoObservable } from "mobx"
    
    class Doubler {
        value = 0
    
        constructor() {
            makeAutoObservable(this)
        }
    
        increment() {
            this.value++
            this.value++
        }
    }
    ```

=== "action.bound"

    ``` javascript
    import { makeObservable, observable, action } from "mobx"
    
    class Doubler {
        value = 0
    
        constructor() {
            makeObservable(this, {
                value: observable,
                increment: action.bound
            })
        }
    
        increment() {
            this.value++
            this.value++
        }
    }
    
    const doubler = new Doubler()
    
    // 以这种方式调用 increment 是安全的，因为它已经绑定好了。
    setInterval(doubler.increment, 1000)
    ```

=== "action(fn)"

    ``` javascript
    import { observable, action } from "mobx"
    
    const state = observable({ value: 0 })
    
    const increment = action(state => {
        state.value++
        state.value++
    })
    
    increment(state)
    ```

=== "runInAction(fn)"

    ``` javascript
    import {observable, runInAction} from "mobx"
    
    const state = observable({value: 0})
    
    runInAction(() => {
        state.value++
        state.value++
    })
    ```

#### 使用action包装函数

为了最大程度地利用 MobX 的事务特性，应该尽可能将 action 向外层传递。如果一个类的方法会修改状态，最好将其标记为
action。更好的做法是将事件处理函数标记为 action，因为最外层的事务才是关键。如果一个未被标记为 action 的事件处理函数依次调用了两个
action，依然会产生两个事务。

为了便于创建基于操作的事件处理器，action 不仅仅是一个注解，它还是一个高阶函数。你可以将一个函数作为参数传递给它，这样它会返回一个具有相同签名的、被
action 包裹的函数。

例如，在 React 中，可以如下包装一个 onClick 事件处理函数。

``` jsx
const ResetButton = ({ formState }) => (
    <button
        onClick={action(e => {
            formState.resetPendingUploads()
            formState.resetValues()
            e.preventDefault()
        })}
    >
        Reset form
    </button>
)
```

出于调试的考虑，我们建议为被包装的函数命名，或者将名称作为第一个参数传递给 action。

!!! note "操作未被跟踪"

    actions 的另一个特点是它们不会被追踪。当在副作用或计算值（虽然这种情况非常罕见！）中调用 action 时，action 内部读取的 observable 不会被计入该派生值的依赖项。

    makeAutoObservable、extendObservable 和 observable 使用了一种特殊类型的 action，称为 autoAction，它会在运行时自动判断函数是派生函数还是 action。

#### action.bound

用法：

- `action.bound (annotation)`

action.bound 注解可以用于自动将方法绑定到正确的实例上，从而确保在函数内部 this 始终指向正确的对象。

!!! note "使用 makeAutoObservable(o, {}, { autoBind: true }) 可以自动绑定所有的 actions 和 flows。"

    ``` javascript
    import {makeAutoObservable} from "mobx"
    
    class Doubler {
        value = 0
    
        constructor() {
            makeAutoObservable(this, {}, {autoBind: true})
        }
    
        increment() {
            this.value++
            this.value++
        }
    
        * flow() {
            const response = yield fetch("http://example.com/value")
            this.value = yield response.json()
        }
    }
    ```

#### runInAction

用法：

- `runInAction(fn)`

使用此工具可以创建一个会被立即执行的临时操作，在异步流程中非常实用。具体示例请参考上方的代码块。

### Actions与继承

只有在原型上定义的操作才能被子类重写：

```javascript
class Parent {
    // on instance
    arrowAction = () => {
    }

    // on prototype
    action() {
    }

    boundAction() {
    }

    constructor() {
        makeObservable(this, {
            arrowAction: action,
            action: action,
            boundAction: action.bound,
        })
    }
}

class Child extends Parent {
    // 抛出：TypeError：无法重新定义属性：arrowAction
    arrowAction = () => {
    }

    // OK
    action() {
    }

    boundAction() {
    }

    constructor() {
        super()
        makeObservable(this, {
            arrowAction: override,
            action: override,
            boundAction: override,
        })
    }
}
```

要为此绑定单个操作，可以使用 action.bound，而不是箭头函数。更多信息请参见子类化相关内容。

### 异步操作

本质上，在 MobX 中，异步流程并不需要特殊处理，因为所有的响应都会在它们被触发的任何时刻自动更新。而且，由于可观察对象是可变的，通常可以在一个操作的整个过程中安全地持有它们的引用。不过，在异步流程中，每一步（每个
tick）如果会更新可观察对象，都应该被标记为 action。通过上面提到的 API，有多种方式可以实现这一点，具体如下所示。

例如，在处理 Promise 时，用于更新状态的处理函数应该是 action，或者应该通过 action 包裹，如下所示。

=== "将处理程序包裹在`action`中"

    Promise 的解析处理程序会被内联处理，但会在原始操作完成后运行，因此需要用 action 包裹起来。

    ``` javascript
    import { action, makeAutoObservable } from "mobx"
    
    class Store {
        githubProjects = []
        state = "pending" // "pending", "done" or "error"
    
        constructor() {
            makeAutoObservable(this)
        }
    
        fetchProjects() {
            this.githubProjects = []
            this.state = "pending"
            fetchGithubProjectsSomehow().then(
                action("fetchSuccess", projects => {
                    const filteredProjects = somePreprocessing(projects)
                    this.githubProjects = filteredProjects
                    this.state = "done"
                }),
                action("fetchError", error => {
                    this.state = "error"
                })
            )
        }
    }
    ```

=== "在单独的actions中处理更新"

    如果 promise 处理函数是类字段，makeAutoObservable 会自动将它们包装为 action。

    ``` javascript
    import {makeAutoObservable} from "mobx"
    
    class Store {
        githubProjects = []
        state = "pending" // "pending", "done" or "error"
    
        constructor() {
            makeAutoObservable(this)
        }
    
        fetchProjects() {
            this.githubProjects = []
            this.state = "pending"
            fetchGithubProjectsSomehow().then(this.projectsFetchSuccess, this.projectsFetchFailure)
        }
    
        projectsFetchSuccess = projects => {
            const filteredProjects = somePreprocessing(projects)
            this.githubProjects = filteredProjects
            this.state = "done"
        }
    
        projectsFetchFailure = error => {
            this.state = "error"
        }
    }
    ```

=== "async/await + runInAction"

    在 await 之后的任何操作都不会在同一个事件循环中执行，因此需要用 action 包裹。在这里，我们可以利用 runInAction：

    ``` javascript
    import {runInAction, makeAutoObservable} from "mobx"
    
    class Store {
        githubProjects = []
        state = "pending" // "pending", "done" or "error"
    
        constructor() {
            makeAutoObservable(this)
        }
    
        async fetchProjects() {
            this.githubProjects = []
            this.state = "pending"
            try {
                const projects = await fetchGithubProjectsSomehow()
                const filteredProjects = somePreprocessing(projects)
                runInAction(() => {
                    this.githubProjects = filteredProjects
                    this.state = "done"
                })
            } catch (e) {
                runInAction(() => {
                    this.state = "error"
                })
            }
        }
    }
    ```

=== "`flow` + generator function"

    ``` javascript
    import { flow, makeAutoObservable, flowResult } from "mobx"
    
    class Store {
        githubProjects = []
        state = "pending"
    
        constructor() {
            makeAutoObservable(this, {
                fetchProjects: flow
            })
        }
    
        // 注意星号，这是一个生成器函数！
        *fetchProjects() {
            this.githubProjects = []
            this.state = "pending"
            try {
                // Yield instead of await.
                const projects = yield fetchGithubProjectsSomehow()
                const filteredProjects = somePreprocessing(projects)
                this.state = "done"
                this.githubProjects = filteredProjects
                return projects
            } catch (error) {
                this.state = "error"
            }
        }
    }
    
    const store = new Store()
    const projects = await flowResult(store.fetchProjects())
    ```

### 使用flow替代async/await🚀

用法：

- `flow (annotation)`
- `flow(function* (args) { })`
- `@flow (method decorator)`

`flow 包装器`是一种可选的替代 `async / await` 的方式，可以让你更轻松地处理 MobX 的 action。flow
只接受一个生成器函数作为参数。在生成器内部，你可以通过
yield 来串联 Promise（即用 yield somePromise 替代 await somePromise）。flow 机制会在每个被 yield 的 Promise
解析后，自动让生成器继续执行或抛出异常。

所以，flow 是一种替代 async/await 的方案，无需额外的 action 封装。使用方法如下：

1. 用 flow 包裹你的异步函数；
2. 用 function* 替代 async；
3. 用 yield 替代 await。

上面的 flow 和生成器函数示例展示了实际应用中的效果。

请注意，只有在使用 TypeScript 时才需要 flowResult 函数。因为用 flow 装饰方法后，返回的生成器会被包装成一个 promise。但
TypeScript 并不知道这种类型的变化，所以 flowResult 可以确保 TypeScript 能正确识别这种类型的改变。

makeAutoObservable 及相关方法会自动将生成器推断为 flow。被 flow 注解的成员将不会被枚举。

!!! note "在对象字段上使用flow"

    flow 和 action 一样，也可以直接用于包装函数。上面的例子也可以这样写：

    ``` javascript
    import { flow, makeObservable, observable } from "mobx"
    
    class Store {
        githubProjects = []
        state = "pending"
    
        constructor() {
            makeObservable(this, {
                githubProjects: observable,
                state: observable,
            })
        }
    
        fetchProjects = flow(function* (this: Store) {
            this.githubProjects = []
            this.state = "pending"
            try {
                // yield instead of await.
                const projects = yield fetchGithubProjectsSomehow()
                const filteredProjects = somePreprocessing(projects)
                this.state = "done"
                this.githubProjects = filteredProjects
            } catch (error) {
                this.state = "error"
            }
        })
    }
    
    const store = new Store()
    const projects = await store.fetchProjects()
    ```

    好处是我们不再需要 flowResult 了，缺点是现在需要进行类型声明，以确保类型能够被正确推断。

#### flow.bound

用法：

- `flow.bound (annotation)`

flow.bound 注解可以用于自动将方法绑定到正确的实例上，从而确保 this 在函数内部始终指向正确的对象。类似于 actions，flows
也可以通过 autoBind 选项默认进行绑定。

### 取消flows🚀

flows 的另一个很实用的优点是它们可以被取消。flow 的返回值是一个 promise，这个 promise 会在生成器函数最终返回值时解析。这个返回的
promise 还带有一个额外的 cancel() 方法，可以中断正在运行的生成器并取消它。任何 try / finally 代码块依然会被执行。

### 禁用强制actions🚀

从 MobX 6 开始，默认要求你必须通过 actions 来修改状态。不过，你也可以配置 MobX，关闭这一限制。具体可以参考 enforceActions
部分。例如，在单元测试的设置过程中，这个功能就非常实用，因为此时相关警告并不总是有意义。

## 使用计算属性(Computeds)推导信息

用法：

- `computed (annotation)`
- `computed(options) (annotation)`
- `computed(fn, options?)`
- `@computed (getter decorator)`
- `@computed(options) (getter decorator)`

计算属性可以用于从其他可观察对象中推导信息。它们采用惰性求值的方式，会缓存计算结果，只有在某个依赖的可观察对象发生变化时才会重新计算。如果没有任何对象对其进行观察，它们将会完全挂起，不再执行计算。

从概念上讲，它们和电子表格中的公式非常相似，而且作用不可小觑。它们有助于减少需要存储的状态量，并且经过高度优化。建议在可能的情况下尽量使用。

### 示例

可以通过在 JavaScript 的 getter 上添加 computed 注解来创建计算属性。使用 makeObservable 可以将某个 getter 声明为计算属性。如果你希望所有的 getter 都自动被声明为计算属性，可以使用 makeAutoObservable、observable 或 extendObservable。被声明为计算属性的 getter 会变成不可枚举属性。

为了更好地说明计算值的概念，下面的示例将借助 Reactions {🚀} 高级部分中的 autorun。

```javascript
import { makeObservable, observable, computed, autorun } from "mobx"

class OrderLine {
    price = 0
    amount = 1

    constructor(price) {
        makeObservable(this, {
            price: observable,
            amount: observable,
            total: computed
        })
        this.price = price
    }

    get total() {
        console.log("Computing...")
        return this.price * this.amount
    }
}

const order = new OrderLine(0)

const stop = autorun(() => {
    console.log("Total: " + order.total)
})
// Computing...
// Total: 0

console.log(order.total)
// (No recomputing!)
// 0

order.amount = 5
// Computing...
// (No autorun)

order.price = 2
// Computing...
// Total: 10

stop()

order.price = 3
// 计算和自动运行都不会被重新执行。
```

上面的例子很好地展示了计算属性的优势：它可以作为一个缓存点。即使我们修改了 amount，这会导致 total 重新计算，但并不会触发 autorun，因为 total 会检测到它的输出并未发生变化，所以没有必要去更新 autorun。

相比之下，如果没有给 total 添加注解，autorun 会因为直接依赖于 total 和 amount 而执行三次。你可以自己试试看。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505142129640.png){ loading=lazy }
  <figcaption>这是针对上述示例生成的依赖关系图。</figcaption>
</figure>

### 规则

在使用计算属性时，有几条最佳实践需要遵循：

1. 不应产生副作用或更新其他可观察对象。
2. 避免创建并返回新的可观察对象。
3. 不应依赖于非可观察的值。

### 提示

??? note "如果计算属性没有被观察，它们将会被挂起。"

    对于刚接触 MobX 的人来说，这一点有时会让人困惑，尤其是那些习惯了像 Reselect 这样的库的人。如果你创建了一个 computed 属性，但没有在任何 reaction 中使用它，那么它并不会被缓存，看起来会比实际需要的情况被重新计算得更频繁。举个例子，如果我们在上面的例子中，在调用 stop() 之后又调用了两次 console.log(order.total)，那么这个值就会被重新计算两次。


