# 优化React组件渲染

MobX 非常快，很多时候甚至比 Redux 更快。不过，这里有一些建议，帮助你更充分地发挥 React 和 MobX 的性能。大多数建议也适用于 React 本身，并不特定于 MobX。需要注意的是：了解这些模式当然有益，但通常即使你完全不去考虑它们，你的应用也已经足够快了。

只有当性能确实成了问题时，才把 `性能优化` 放在优先级最高的位置！

## 使用更多、更小的组件

`observer` 组件会跟踪它所使用的所有值，只要其中任何一个发生变化，就会重新渲染。因此，组件越小，需要因变化而重新渲染的范围就越小。这意味着，你的用户界面中会有更多部分能够彼此独立地完成渲染。

## 在专门的组件中渲染列表

在渲染大型集合时，上述建议尤其重要。React 在渲染大集合方面口碑并不理想，因为每次集合发生变化时，`reconciler`（协调器）都需要重新评估由该集合生成的组件。因此，建议使用专门的组件只负责对集合进行 `map` 并渲染列表，不要在同一个组件里再渲染其他内容。

不好的:

``` javascript
const MyComponent = observer(({ todos, user }) => (
    <div>
        {user.name}
        <ul>
            {todos.map(todo => (
                <TodoView todo={todo} key={todo.id} />
            ))}
        </ul>
    </div>
))
```

在上面的代码清单中，当 `user.name` 发生变化时，React 会被迫对所有 `TodoView` 组件进行不必要的协调 (reconcile)。它们不会重新渲染，但光是协调过程本身就很耗费资源。

好的:

``` javascript
const MyComponent = observer(({ todos, user }) => (
    <div>
        {user.name}
        <TodosView todos={todos} />
    </div>
))

const TodosView = observer(({ todos }) => (
    <ul>
        {todos.map(todo => (
            <TodoView todo={todo} key={todo.id} />
        ))}
    </ul>
))
```

## 不要用数组索引当作key

不要把数组索引或任何未来可能变化的值当作 `key`。如有需要，请为你的对象生成 `id`。可以参考这篇博文。

## 尽量晚一些再解引用值

在使用 mobx-react 时，建议尽可能晚地对值进行解引用 (dereference)。这是因为 MobX 会自动重新渲染那些解引用了可观察 (observable) 值的组件。如果这种解引用发生在组件树更深的位置，需要重新渲染的组件就会更少。

慢的:

``` javascript
<DisplayName name={person.name} />
```

快的:

```javascript
<DisplayName person={person} />
```

在更快的示例中，`name` 属性的变更只会触发 `DisplayName` 重新渲染；而在更慢的示例里，组件的拥有者也必须一起重新渲染。这并没有问题，而且如果拥有该组件的渲染足够快（通常确实如此！），这种做法就很有效。

### 函数型 props

你可能会注意到，为了在更晚的时候再解引用这些值，你不得不创建大量小型的 `observer` 组件，每个组件都专门用于渲染数据的不同部分，例如：

``` javascript
const PersonNameDisplayer = observer(({ person }) => <DisplayName name={person.name} />)

const CarNameDisplayer = observer(({ car }) => <DisplayName name={car.model} />)

const ManufacturerNameDisplayer = observer(({ car }) => 
    <DisplayName name={car.manufacturer.name} />
)
```

如果你有大量形态各异的数据，这很快就会变得繁琐。另一种做法是使用一个函数，返回你希望 `Displayer` 渲染的数据：

``` javascript
const GenericNameDisplayer = observer(({ getName }) => <DisplayName name={getName()} />)
```

然后，你就可以这样使用这个`组件 (component)`：

``` javascript
const MyComponent = ({ person, car }) => (
    <>
        <GenericNameDisplayer getName={() => person.name} />
        <GenericNameDisplayer getName={() => car.model} />
        <GenericNameDisplayer getName={() => car.manufacturer.name} />
    </>
)
```

这种方式可以让 `GenericNameDisplayer` 在整个应用中复用，用来渲染任何名称，同时还能把组件的重新渲染控制在最低限度。
