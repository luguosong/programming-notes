# 子类化

支持子类化，但有一定限制。最重要的是：你只能在原型（prototype）上重写 `actions`/`flows`/`computeds`，不能重写字段声明（field
declarations）。对于在子类中被重写的方法/`getter`，请使用 `override` 注解——示例如下。尽量保持简单，优先选择组合（composition）而不是继承（inheritance）。

``` javascript
import { makeObservable, observable, computed, action, override } from "mobx"

class Parent {
    // 带注解的实例字段`不能被重写`
    observable = 0
    arrowAction = () => {}

    // `未使用注解`的实例字段是可以被重写的
    overridableArrowAction = action(() => {})

    // 带注解的原型 (prototype) 方法/`getter` 都可以被重写（override）
    action() {}
    actionBound() {}
    get computed() {}

    constructor(value) {
        makeObservable(this, {
            observable: observable,
            arrowAction: action
            action: action,
            actionBound: action.bound,
            computed: computed,
        })
    }
}

class Child extends Parent {
    /* --- INHERITED --- */
    // THROWS - TypeError: Cannot redefine property
    // observable = 5
    // arrowAction = () = {}

    // OK - not annotated
    overridableArrowAction = action(() => {})

    // OK - prototype
    action() {}
    actionBound() {}
    get computed() {}

    /* --- NEW --- */
    childObservable = 0;
    childArrowAction = () => {}
    childAction() {}
    childActionBound() {}
    get childComputed() {}

    constructor(value) {
        super()
        makeObservable(this, {
            // inherited
            action: override,
            actionBound: override,
            computed: override,
            // new
            childObservable: observable,
            childArrowAction: action
            childAction: action,
            childActionBound: action.bound,
            childComputed: computed,
        })
    }
}
```

## 限制（Limitations）

1. 只有在原型（prototype）上定义的 `action`、`computed`、`flow`、`action.bound` 才能被子类重写。
2. 子类里不能对字段重新添加注解，除非使用 `override`。
3. `makeAutoObservable` 不支持子类化。
4. 不支持扩展内置类型（builtins），例如 `ObservableMap`、`ObservableArray` 等。
5. 在子类中无法为 `makeObservable` 提供不同的选项（options）。
6. 在同一条单继承链中，不能混用注解/装饰器（annotations/decorators）。
7. 其他所有限制同样适用。

### TypeError: Cannot redefine property

如果你看到了这个错误，很可能是你在子类中尝试重写箭头函数 `x = () => {}`
。这行不通，因为类中所有被注解的字段都是不可配置的（non-configurable）（见上述限制）。你有两个选择：

??? note "将函数移到原型（prototype），并改用 `action.bound` 注解替代"

	```javascript
	class Parent {
	    // action = () => {};
	    // =>
	    action() {}
	
	    constructor() {
	        makeObservable(this, {
	            action: action.bound
	        })
	    }
	}
	class Child {
	    action() {}
	
	    constructor() {
	        super()
	        makeObservable(this, {
	            action: override
	        })
	    }
	}
	```

??? note "移除 `action` 注解，并手动用 `action` 包裹该函数：x = action(() => {})"

	```javascript
	class Parent {
	    // action = () => {};
	    // =>
	    action = action(() => {})
	
	    constructor() {
	        makeObservable(this, {}) // <-- annotation removed
	    }
	}
	class Child {
	    action = action(() => {})
	
	    constructor() {
	        super()
	        makeObservable(this, {}) // <-- annotation removed
	    }
	}
	```
