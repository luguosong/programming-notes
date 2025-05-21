# 描述UI

## React概述

React 是一个流行的`声明式`库，您可以使用它构建交互式用户界面（user interfaces 、UI)。

> 用户界面由`按钮`、`文本`和`图像`等小单元内容构建而成。React帮助你把它们组合成可重用、可嵌套的`组件`。

## React入门案例

### 纯Html

``` html title="hello_react.html"
--8<-- "code/front-end/react/basic/example/hello_react.html"
```

<iframe loading="lazy" src="../example/hello_react.html"></iframe>

### Create React App

Create React App 是官方支持的创建单页 React 应用程序的方式。它提供了一个现代的构建设置，无需配置。

```shell
# 创建React项目
npx create-react-app hello-react
```

``` html title="index.html"
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8" />
    <link rel="icon" href="%PUBLIC_URL%/favicon.ico" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <meta name="theme-color" content="#000000" />
    <meta
      name="description"
      content="Web site created using create-react-app"
    />
    <link rel="apple-touch-icon" href="%PUBLIC_URL%/logo192.png" />
    <link rel="manifest" href="%PUBLIC_URL%/manifest.json" />
    <title>React App</title>
  </head>
  <body>
    <noscript>You need to enable JavaScript to run this app.</noscript>
    <div id="root"></div>
  </body>
</html>
```

``` javascript title="index.js"
import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);

reportWebVitals();
```

### 使用vite创建项目

```shell
# 使用vite创建react项目
npm create vite@latest my-vue-app -- --template react
# 或
yarn create vite my-vue-app --template react
```

## 组件入门

React 组件是一段可以 使用标签进行扩展 的 JavaScript 函数，是用程序中可复用的 UI 元素。

``` html title="hello_component.html"
--8<-- "code/front-end/react/basic/example/hello_component.html"
```

<iframe loading="lazy" src="../example/hello_component.html"></iframe>

## JSX

### 简介

`JSX`（JavaScript XML，正式称为JavaScript语法扩展）是JavaScript语言语法的类似XML的扩展。最初由Facebook创建以用于React，JSX已被多个Web框架采用。

将`渲染逻辑`和`标签`共同存放在组件中。可以让js更好的控制HTML内容。

JSX 规则：

- 只能返回一个根元素
- 标签必须闭合，像 `<img>` 这样的自闭合标签必须书写成 `<img />`
- 使用驼峰式命名法给大部分属性命名！

!!! warning

	由于历史原因，`aria-*` 和 `data-*` 属性是以带 `-` 符号的 HTML 格式书写的。

JSX代码示例:

```jsx
const App = () => {
    return (
        <div>
            <p>Header</p>
            <p>Content</p>
            <p>Footer</p>
        </div>
    );
}
```

!!! note

	在JSX中编写的代码需要使用诸如`Babel`之类的工具进行转换，以便能够被`Web浏览器`理解。这种处理通常是在软件`构建`
	过程中进行的，在应用程序部署之前。

### JSX中使用大括号

JSX中`大括号`的作用：可以在标签中添加一些 `JavaScript逻辑`或者`引用动态的属性`。

具体有以下功能：

- 动态的指定标签`属性`，通过大括号可以在`属性`中使用`JavaScript 变量`。
- 动态的指定标签`内容`，通过大括号可以在标签中使用`JavaScript 变量`或`表达式`。

``` jsx title="在大括号中使用变量"
export default function Avatar() {
  const avatar = 'https://i.imgur.com/7vQD0fPs.jpg';
  const description = 'Gregorio Y. Zara';
  return (
    <img
      className="avatar"
      src={avatar}
      alt={description}
    />
  );
}
```

``` jsx title="在大括号中使用对象"
export default function TodoList() {
  return (
    <ul style={{
      backgroundColor: 'black',
      color: 'pink'
    }}>
      <li>Improve the videophone</li>
      <li>Prepare aeronautics lectures</li>
      <li>Work on the alcohol-fuelled engine</li>
    </ul>
  );
}
```

!!! warning

	内联 style 属性 使用驼峰命名法编写。例如，HTML `<ul style="background-color: black">` 在你的组件里应该写成 `<ul style={{ backgroundColor: 'black' }}>`。

``` jsx title="在大括号中使用变量"
export default function TodoList() {
  const name = 'Gregorio Y. Zara';
  return (
    <h1>{name}的待办事项列表</h1>
  );
}
```

``` jsx title="在大括号中使用表达式（函数调用）"
const today = new Date();

function formatDate(date) {
  return new Intl.DateTimeFormat(
    'zh-CN',
    { weekday: 'long' }
  ).format(date);
}

export default function TodoList() {
  return (
    <h1>To Do List for {formatDate(today)}</h1>
  );
}
```

``` jsx title="在大括号中使用对象中的属性"
const person = {
  name: 'Gregorio Y. Zara',
  theme: {
    backgroundColor: 'black',
    color: 'pink'
  }
};

export default function TodoList() {
  return (
    <div style={person.theme}>
      <h1>{person.name}'的待办事项</h1>
      <img
        className="avatar"
        src="https://i.imgur.com/7vQD0fPs.jpg"
        alt="Gregorio Y. Zara"
      />
      <ul>
        <li>优化视屏电话</li>
        <li>准备航空学课程</li>
        <li>研究乙醇燃料引擎</li>
      </ul>
    </div>
  );
}
```

## 将Props传递给组件

### 基础概念

每个`父组件`都可以提供`props`给它的`子组件`，从而将一些信息传递给它。

props可以传递任何 JavaScript 值，包括`对象`、`数组`和`函数`。

!!! note

	Props 使你独立思考父组件和子组件。 

	父组件可以改变props，而无需考虑子组件如何使用它们。

	同样，可以改变子组件使用props的方式，不必考虑父组件如何设置它们。

``` jsx title="props_demo.html"
--8<-- "code/front-end/react/basic/example/props_demo.html"
```

<iframe loading="lazy" src="../example/props_demo.html"></iframe>

### props默认值

```jsx
/*
    * 属性设置默认值
    * */
function RedText3({color = "red"}) {
    return (
        <p style={{color: color}}>
            Hello,React component!
        </p>
    );
}

/*
   * 父组件
   * */
function MyApp() {
    return (
        <>
            <RedText3/>
        </>
    );
}
```

### 使用展开语法传递 props

有时候，传递 props 会变得非常重复。因为这些组件不直接使用他们本身的任何 props，所以可以使用更简洁的`展开`语法:

```jsx
// Profile中并没有直接使用props属性，代码不够整洁
function Profile({person, size, isSepia, thickBorder}) {
    return (
        <div className="card">
            <Avatar
                person={person}
                size={size}
                isSepia={isSepia}
                thickBorder={thickBorder}
            />
        </div>
    );
}

/*
* 优化后的代码
* 
* 这会将 Profile 的所有 props 转发到 Avatar，而不列出每个名字。
* */
function Profile(props) {
    return (
        <div className="card">
            <Avatar {...props} />
        </div>
    );
}
```

!!! warning "请克制地使用展开语法"

	 如果你在所有其他组件中都使用它，那就有问题了。 通常，它表示你应该拆分组件，并将子组件作为 JSX 传递。 接下来会详细介绍！

### 将JSX作为子组件传递(插槽)

``` jsx title="passing_jsx_as_children.html"
--8<-- "code/front-end/react/basic/example/passing_jsx_as_children.html"
```

<iframe loading="lazy" src="../example/passing_jsx_as_children.html"></iframe>

### props是不变的

不要尝试`更改 props`。 props 是 `不可变的`（一个计算机科学术语，意思是“不可改变”）。当一个组件需要改变它的props（例如，响应用户交互或新数据）时，它不得不通过它的父组件传递
`不同的props` —— 一个新对象！它的旧 props 将被丢弃，最终 JavaScript 引擎将回收它们占用的内存。

## 条件渲染

组件会需要根据不同的情况显示不同的内容。

### if语句

``` jsx
function Item({ name, isPacked }) {
  if (isPacked) {
    return <li className="item">{name} ✅</li>;
  }
  return <li className="item">{name}</li>;
}
```

### 三元运算符

相比于if语句，三元运算符减少了很多重复代码。

``` jsx
function Item({name, isPacked}) {
    return (
        <li className="item">
            {isPacked ? name + ' ✅' : name}
        </li>
    );
}
```

对于简单的条件判断，这样的风格可以很好地实现，但需要适量使用。如果你的组件里有很多的嵌套式条件表达式，则需要考虑通过提取为子组件来简化这些嵌套表达式。在
React 里，标签也是你代码中的一部分，所以你可以使用变量和函数来整理一些复杂的表达式。

### 与运算符（&&）

当 `JavaScript && 表达式` 的左侧（我们的条件）为 `true` 时，它则返回其右侧的值。但条件的结果是 `false`，则整个表达式会变成
`false`。在 JSX 里，React 会将 `false` 视为一个`空值`，就像 `null` 或者 `undefined`，这样 React 就不会在这里进行任何渲染。

``` jsx
return (
  <li className="item">
    {name} {isPacked && '✅'}
  </li>
);
```

### 通过变量实现

``` jsx
function Item({ name, isPacked }) {

  let itemContent = name;
  // 不仅可以使用文本，也可以在变量中插入标签
  if (isPacked) {
    itemContent = (
      <del>
        {name + " ✅"}
      </del>
    );
  }
  
  return (
    <li className="item">
      {itemContent}
    </li>
  );
}
```

## 渲染列表

### 列表渲染

``` jsx
const people = [{
  id: 0,
  name: '凯瑟琳·约翰逊',
  profession: '数学家',
}, {
  id: 1,
  name: '马里奥·莫利纳',
  profession: '化学家',
}, {
  id: 2,
  name: '穆罕默德·阿卜杜勒·萨拉姆',
  profession: '物理学家',
}, {
  id: 3,
  name: '珀西·莱温·朱利亚',
  profession: '化学家',
}, {
  id: 4,
  name: '苏布拉马尼扬·钱德拉塞卡',
  profession: '天体物理学家',
}];

export default function List() {
    const listItems = people.map(person =>
        <li key={person.id}>{person.name}</li>
    );
    
    return <ul>{listItems}</ul>;
}
```

### 列表过滤

``` jsx
const people = [{
  id: 0,
  name: '凯瑟琳·约翰逊',
  profession: '数学家',
}, {
  id: 1,
  name: '马里奥·莫利纳',
  profession: '化学家',
}, {
  id: 2,
  name: '穆罕默德·阿卜杜勒·萨拉姆',
  profession: '物理学家',
}, {
  id: 3,
  name: '珀西·莱温·朱利亚',
  profession: '化学家',
}, {
  id: 4,
  name: '苏布拉马尼扬·钱德拉塞卡',
  profession: '天体物理学家',
}];

export default function List() {
	/*
	* 先进行条件过滤
	* */
	const chemists = people.filter(person =>
		person.profession === '化学家'
	);
	
    /*
    * 再进行列表渲染
    * */
	const listItems = chemists.map(person =>
		<li key={person.id}>
			<p>
				<b>{person.name}:</b>
				{' ' + person.profession + ' '}
			</p>
		</li>
	);
	
	return <ul>{listItems}</ul>;
}
```

### key值

直接放在 `map() 方法`里的 `JSX 元素`一般都需要指定` key 值`！

这些 key 会告诉 React，每个组件对应着数组里的哪一项，所以 React 可以把它们匹配起来。这在数组项进行移动（例如排序）、插入或删除等操作时非常重要。一个合适的
key 可以帮助 React 推断发生了什么，从而得以正确地更新 DOM 树。

- key 值在兄弟节点之间必须是唯一的。 不过不要求全局唯一，在不同的数组中可以使用相同的 key。
- key 值不能改变，否则就失去了使用 key 的意义！所以千万不要在渲染时动态地生成 key。

用作 key 的值应该在数据中提前就准备好，而不是在运行时才随手生成。

如果你想让每个列表项都输出多个 DOM 节点而非一个的话，该怎么做呢？
Fragment 语法的简写形式 <> </> 无法接受 key 值，所以你只能要么把生成的节点用一个 `<div>`
标签包裹起来，要么使用长一点但更明确的 <Fragment> 写法：

``` jsx
import { Fragment } from 'react';

// ...

const listItems = people.map(person =>
<Fragment key={person.id}>
<h1>{person.name}</h1>
<p>{person.bio}</p>
</Fragment>
);
```

这里的 Fragment 标签本身并不会出现在 DOM 上，这串代码最终会转换成 `<h1>、<p>、<h1>、<p>…… `的列表。

!!! note "React 中为什么需要 key"

	设想一下，假如你桌面上的文件都没有文件名，取而代之的是，你需要通过文件的位置顺序来区分它们———第一个文件，第二个文件，以此类推。也许你也不是不能接受这种方式，可是一旦你删除了其中的一个文件，这种组织方式就会变得混乱无比。原来的第二个文件可能会变成第一个文件，第三个文件会成为第二个文件……

	React 里需要 key 和文件夹里的文件需要有文件名的道理是类似的。它们（key 和文件名）都让我们可以从众多的兄弟元素中唯一标识出某一项（JSX 节点或文件）。而一个精心选择的 key 值所能提供的信息远远不止于这个元素在数组中的位置。即使元素的位置在渲染的过程中发生了改变，它提供的 key 值也能让 React 在整个生命周期中一直认得它。

!!! warning "使用数组的索引作为 key值"

	你可能会想直接把数组项的索引当作 key 值来用，实际上，如果你没有显式地指定 key 值，React 确实默认会这么做。但是数组项的顺序在插入、删除或者重新排序等操作中会发生改变，此时把索引顺序用作 key 值会产生一些微妙且令人困惑的 bug。

	与之类似，请不要在运行过程中动态地产生 key，像是 `key={Math.random()}` 这种方式。这会导致每次重新渲染后的 key 值都不一样，从而使得所有的组件和 DOM 元素每次都要重新创建。这不仅会造成运行变慢的问题，更有可能导致用户输入的丢失。所以，使用能从给定数据中稳定取得的值才是明智的选择。

	有一点需要注意，组件不会把 key 当作 props 的一部分。Key 的存在只对 React 本身起到提示作用。如果你的组件需要一个 ID，那么请把它作为一个单独的 prop 传给组件：` <Profile key={id} userId={id} />`。

## 保持组件纯粹

### 纯函数

在计算机科学中（尤其是函数式编程的世界中），纯函数 通常具有如下特征：

- `只负责自己的任务。`它不会更改在该函数调用前就已存在的对象或变量。
- `输入相同，则输出相同。`给定相同的输入，纯函数应总是返回相同的结果。

`纯函数`不会改变函数作用域外的变量、或在函数调用前创建的对象——这会使函数变得`不纯粹`！

``` javascript
// double() 就是一个 纯函数。如果你传入 3 ，它将总是返回 6 。
function double(number) {
  return 2 * number;
}
```

⭐React 便围绕着这个概念进行设计。`React 假设你编写的所有组件都是纯函数。`也就是说，对于相同的输入，你所编写的 React
组件必须总是返回相同的 JSX。

React使用纯函数的好处：

- 你的组件可以在不同的环境下运行 — 例如，在服务器上！由于它们针对相同的输入，总是返回相同的结果，因此一个组件可以满足多个用户请求。
- 你可以为那些输入未更改的组件来 跳过渲染，以提高性能。这是安全的做法，因为纯函数总是返回相同的结果，所以可以安全地缓存它们。
- 如果在渲染深层组件树的过程中，某些数据发生了变化，React 可以重新开始渲染，而不会浪费时间完成过时的渲染。纯粹性使得它随时可以安全地停止计算。

### 副作用

以下组件正在读写其外部声明的 guest 变量。这意味着 `多次调用这个组件会产生不同的 JSX`！并且，如果 其他 组件读取 guest
，它们也会产生不同的 JSX，其结果取决于它们何时被渲染！这是无法预测的。

``` jsx
let guest = 0;

function Cup() {
  // Bad：正在更改预先存在的变量！
  guest = guest + 1;
  return <h2>Tea cup for guest #{guest}</h2>;
}

export default function TeaSet() {
  return (
    <>
      <Cup />
      <Cup />
      <Cup />
    </>
  );
}
```

React 提供了`严格模式`，可以用`<React.StrictMode>`包裹根组件引入严格模式。在严格模式下开发时，它将会调用每个组件函数两次。
`通过重复调用组件函数，严格模式有助于找到违反这些规则的组件`。

在 React 中，副作用通常属于 `事件处理程序`。事件处理程序是 React 在你执行某些操作（如单击按钮）时运行的函数。即使事件处理程序是在你的组件
内部 定义的，它们也`不会在渲染期间运行`！ 因此`事件处理程序无需是纯函数`。

如果你用尽一切办法，仍无法为副作用找到合适的事件处理程序，你还可以调用组件中的 `useEffect` 方法将其附加到返回的 JSX 中。这会告诉
React 在渲染结束后执行它。然而，这种方法应该是你最后的手段。

<figure markdown="span">
  ![](https://edrawcloudpubliccn.oss-cn-shenzhen.aliyuncs.com/viewer/self/1059758/share/2025-1-7/1736231321/main.svg){ loading=lazy }
  <figcaption>副作用</figcaption>
</figure>

## 将UI视为树

### 渲染树

`组件`的一个主要特性是能够由`其他组件组合而成`。在 嵌套组件 中有`父组件`和`子组件`的概念，其中每个父组件本身可能是另一个组件的子组件。

当渲染 React 应用程序时，可以在一个称为渲染树的树中建模这种关系。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202501071001094.png){ loading=lazy }
  <figcaption>React 创建的 UI 树是由渲染过的组件构成的，被称为渲染树</figcaption>
</figure>

这棵树由节点组成，每个节点代表一个组件。例如，App、FancyText、Copyright 等都是我们树中的节点。

在 React 渲染树中，根节点是应用程序的 根组件。在这种情况下，根组件是 App，它是 React 渲染的第一个组件。树中的每个箭头从父组件指向子组件。

渲染树表示 React 应用程序的`单个渲染过程`。在 `条件渲染` 中，父组件可以根据传递的数据渲染不同的子组件。

尽管渲染树可能在不同的渲染过程中有所不同，但通常这些树有助于识别 React
应用程序中的顶级和叶子组件。顶级组件是离根组件最近的组件，它们影响其下所有组件的渲染性能，通常包含最多复杂性。叶子组件位于树的底部，没有子组件，通常会频繁重新渲染。

识别这些组件类别有助于理解应用程序的数据流和性能。

### 模块依赖树

当 拆分组件 和逻辑到不同的文件中时，就创建了 JavaScript 模块，在这些模块中可以导出组件、函数或常量。

模块依赖树中的每个节点都是一个模块，每个分支代表该模块中的 import 语句。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202501071033076.png){ loading=lazy }
  <figcaption>模块依赖树</figcaption>
</figure>

树的根节点是根模块，也称为入口文件。它通常包含根组件的模块。

与同一应用程序的渲染树相比，存在相似的结构，但也有一些显著的差异：

- 构成树的节点代表模块，而不是组件。
- 非组件模块，如 inspirations.js，在这个树中也有所体现。渲染树仅封装组件。
- Copyright.js 出现在 App.js 下，但在渲染树中，Copyright 作为 InspirationGenerator 的子组件出现。这是因为
  InspirationGenerator 接受 JSX 作为 children props，因此它将 Copyright 作为子组件渲染，但不导入该模块。

依赖树对于确定运行 React 应用程序所需的模块非常有用。在为生产环境构建 React 应用程序时，通常会有一个构建步骤，该步骤将捆绑所有必要的
JavaScript 以供客户端使用。负责此操作的工具称为 bundler（捆绑器），并且 bundler 将使用依赖树来确定应包含哪些模块。

随着应用程序的增长，捆绑包大小通常也会增加。大型捆绑包大小对于客户端来说下载和运行成本高昂，并延迟 UI
绘制的时间。了解应用程序的依赖树可能有助于调试这些问题。



