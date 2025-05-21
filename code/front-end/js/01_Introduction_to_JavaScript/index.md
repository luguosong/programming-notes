# JavaScript简介

`JavaScript` 是web的编程语言。绝大多数网站都使用 JavaScript，所有现代的`网页浏览器`——无论是在桌面、平板还是手机上——都包含
JavaScript 解释器，使得 JavaScript 成为历史上部署最广泛的编程语言。在过去的十年中，`Node.js` 使得 JavaScript
能够在网页浏览器之外进行编程，Node 的巨大成功意味着 JavaScript 现在也是软件开发人员中使用最多的编程语言。无论你是从零开始还是已经在专业地使用
JavaScript，这本书都将帮助你掌握这门语言。

如果你已经熟悉其他编程语言，了解以下信息可能会对你有所帮助：JavaScript是一种高级、动态、解释型编程语言，非常适合`面向对象`和
`函数式编程`
风格。JavaScript的变量是无类型的。它的语法松散地基于Java，但两者在其他方面并无关联。JavaScript从Scheme语言中继承了一等函数的概念，从鲜为人知的Self语言中继承了基于原型的继承机制。不过，你不需要了解这些语言或熟悉这些术语，就可以使用本书来学习JavaScript。

`JavaScript`这个名称相当具有误导性。除了表面上的语法相似之外，`JavaScript`与`Java`
编程语言完全不同。而且，JavaScript早已超越了其脚本语言的根源，发展成为一种强大且高效的通用语言，适用于严肃的软件工程和大型代码库项目。

!!! note "JavaScript：名称、版本和模式"

    `JavaScript`是在网络早期由Netscape创建的，技术上来说，`JavaScript`是从Sun Microsystems（现为Oracle）获得许可的商标，用于描述Netscape（现为Mozilla）对该语言的实现。Netscape将该语言提交给`欧洲计算机制造商协会（ECMA）`进行标准化，由于商标问题，该语言的标准化版本被命名为`ECMAScript`，这个名字显得有些别扭。实际上，大家都称这门语言为JavaScript。本书使用`ECMAScript`及其缩写`ES`来指代该语言标准及其各个版本。

    在大部分的2010年代，所有的网络浏览器都支持ECMAScript标准的第5版。本书将ES5视为兼容性基准，不再讨论该语言的早期版本。ES6于2015年发布，增加了重要的新特性——包括类和模块语法——将JavaScript从一种脚本语言转变为适合大规模软件工程的严肃通用语言。自ES6以来，ECMAScript规范已转为每年发布一次，语言版本——ES2016、ES2017、ES2018、ES2019和ES2020——现在以发布年份命名。

    随着JavaScript的发展，语言设计者试图修正早期（ES5之前）版本中的缺陷。为了保持向后兼容性，即使某些遗留特性存在缺陷，也无法将其移除。但在ES5及之后的版本中，程序可以选择使用JavaScript的严格模式，该模式修正了许多早期语言的错误。选择使用严格模式的机制是通过“use strict”指令，这在§5.6.3中有描述。该部分还总结了遗留JavaScript与严格JavaScript之间的差异。在ES6及之后的版本中，使用新语言特性通常会隐式启用严格模式。例如，如果使用ES6的class关键字或创建ES6模块，那么类或模块中的所有代码都会自动处于严格模式，并且在这些上下文中无法使用旧的、有缺陷的特性。本书将涵盖JavaScript的遗留特性，但会特别指出这些特性在严格模式下不可用。

为了实用，每种语言都必须有一个`平台`或`标准库`
，用于执行诸如基本输入和输出之类的操作。核心的JavaScript语言定义了一个用于处理数字、文本、数组、集合、映射等的最小API，但不包含任何输入或输出功能。输入和输出（以及更复杂的功能，如网络、存储和图形）是JavaScript所嵌入的
`宿主环境`的责任。

JavaScript 最初的`宿主环境`是`网页浏览器`，而这仍然是 JavaScript 代码最常见的执行环境。`网页浏览器`环境允许 JavaScript
代码通过用户的鼠标和键盘获取输入，并通过发送 HTTP 请求获取数据。同时，它也允许 JavaScript 代码通过 HTML 和 CSS 向用户展示输出。

自2010年以来，JavaScript代码有了另一种可用的`宿主环境`。`Node`不再将JavaScript限制于使用网页浏览器提供的API，而是让JavaScript可以
`访问整个操作系统`，使得JavaScript程序能够读写文件、通过网络发送和接收数据，以及创建和处理HTTP请求。Node是实现Web服务器的热门选择，也是编写简单实用脚本的便捷工具，可以替代Shell脚本。

这本书的大部分内容集中在JavaScript语言本身。第11章记录了JavaScript标准库，第15章介绍了网页浏览器宿主环境，第16章介绍了Node宿主环境。

这本书首先介绍低层次的基础知识，然后在此基础上逐步引入更高级和更高层次的抽象概念。章节设计为大致按顺序阅读。然而，学习一门新的编程语言从来不是一个线性的过程，描述一门语言也不是线性的：每个语言特性都与其他特性相关，本书中充满了与相关内容的交叉引用——有时是向后，有时是向前。本介绍章节快速浏览了这门语言，介绍了一些关键特性，以便更容易理解后续章节中的深入讲解。如果你已经是一名实践中的JavaScript程序员，你可以跳过这一章。（不过在继续之前，你可能会喜欢阅读本章末尾的示例1-1。）

## 探索 JavaScript

学习一门新的编程语言时，尝试书中的示例非常重要，然后对其进行修改并再次尝试，以测试你对该语言的理解。为此，你需要一个JavaScript解释器。

要尝试几行 JavaScript，最简单的方法是打开网页浏览器中的开发者工具（使用 F12、Ctrl-Shift-I 或
Command-Option-I），然后选择“控制台”选项卡。你可以在提示符下输入代码，并在输入时查看结果。浏览器开发者工具通常显示在浏览器窗口的底部或右侧，但你通常可以将它们分离为独立窗口（如图
1-1 所示），这通常非常方便。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409021104106.png){ loading=lazy }
  <figcaption>图1-1. Firefox开发者工具中的JavaScript控制台</figcaption>
</figure>

另一种尝试 JavaScript 代码的方法是从 `https://nodejs.org` 下载并安装 Node。安装完成后，你只需打开终端窗口，输入
node，就可以开始一个这样的交互式 JavaScript 会话。

```shell
$ node
Welcome to Node.js v12.13.0.
Type ".help" for more information.
> .help
.break Sometimes you get stuck, this gets you out
.clear Alias for .break
.editor Enter editor mode
.exit Exit the repl
.help Print this help message
.load Load JS from a file into the REPL session
.save Save all evaluated commands in this REPL session to a file
Press ^C to abort current expression, ^D to exit the repl
> let x = 2, y = 3;
undefined
> x + y
5
> (x === 2) && (y === 3)
true
> (x > 3) || (y < 3)
false
```

## Hello World

当你准备开始尝试更长的代码段时，这种逐行交互环境可能不再适用，你可能会更倾向于在文本编辑器中编写代码。然后，你可以将代码复制粘贴到
JavaScript 控制台或 Node 会话中。或者，你可以将代码保存到一个文件中（JavaScript 代码的传统文件扩展名是 .js），然后使用 Node
运行该 JavaScript 代码文件：

```shell
$ node snippet.js
```

如果你以这种非交互方式使用 Node，它不会自动打印出你运行的所有代码的值，因此你需要自己来完成这项工作。你可以使用函数
`console.log()` 在终端窗口或浏览器的开发者工具控制台中显示文本和其他 JavaScript 值。例如，如果你创建一个包含以下代码行的
hello.js 文件：

``` javascript
console.log("Hello World!");
```

并使用 `node hello.js` 执行该文件，你会看到消息“Hello World!”被打印出来。

如果你想在网页浏览器的 JavaScript 控制台中看到相同的信息输出，请创建一个名为 hello.html 的新文件，并将以下文本放入其中：

```html

<script src="hello.js"></script>
```

然后使用类似这样的 file:// URL 将 hello.html 加载到您的网页浏览器中：

```text
file:///Users/username/javascript/hello.html
```

打开开发者工具窗口，在控制台查看问候语。

## JavaScript 导览

本节通过代码示例对JavaScript语言进行简要介绍。在这一介绍性章节之后，我们将深入探讨JavaScript的基础：第二章解释了JavaScript的注释、分号以及Unicode字符集等内容。第三章则开始变得更有趣：它讲解了JavaScript变量以及可以赋予这些变量的值。

以下是一些示例代码，用于展示这两章的重点内容：

``` javascript
// 双斜杠后面的任何内容都是英文注释。
// 仔细阅读注释：它们解释了JavaScript代码。


// 变量是值的符号名称。 
// 变量使用 let 关键字声明：
let x; // 声明一个名为 x 的变量。


// 可以使用等号（=）将值赋给变量
x = 0; // 现在变量 x 的值为 0。
x // => 0: 变量的值即为其本身。


// JavaScript支持多种类型的值。
x = 1; // 数值
x = 0.01; // 数值可以是整数或实数。
x = "hello world"; // 用引号括起来的文本字符串。
x = 'JavaScript'; // 单引号也用于界定字符串。
x = true; // 布尔值
x = false; // 另一个布尔值
x = null; // Null 是一个特殊的值，表示“无值”。
x = undefined; // Undefined 也是一个特殊值，与null类似
```

JavaScript 程序可以操作的另外两种非常重要的类型是对象和数组。这些内容将在第 6 章和第 7
章中详细介绍，但由于它们非常重要，你在阅读到这些章节之前会多次遇到它们。

``` javascript
// JavaScript 最重要的数据类型是对象。
// 一个对象是名称/值对的集合，或者是一个字符串到值的映射。
let book = { // 对象用大括号括起来。
    topic: "JavaScript", // “属性“topic”的值是“JavaScript”。”
    edition: 7 // “edition”属性的值为7
}; // 大括号标志着对象的结束。


// 使用 . 或 [] 访问对象的属性:
book.topic // => "JavaScript"
book["edition"] // => 7: 访问属性值的另一种方法。
book.author = "Flanagan"; // 通过赋值创建新属性。
book.contents = {}; // {} 是一个没有属性的空对象。


// 使用?.有条件地访问属性（ES2020）:
book.contents?.ch01?.sect1 // => undefined: book.contents没有ch01属性。


// JavaScript 还支持数组（按数字索引的列表）值:
let primes = [2, 3, 5, 7]; // 一个包含4个值的数组，用 [ 和 ] 分隔。
primes[0] // => 2：数组的第一个元素（索引0）。
primes.length // => 4: 数组中有多少元素。
primes[primes.length - 1] // => 7: 数组的最后一个元素。
primes[4] = 9; // 通过赋值添加一个新元素.
primes[4] = 11; // 或者通过赋值更改现有元素。
let empty = []; // []是一个没有元素的空数组。
empty.length // => 0


// 数组和对象可以包含其他数组和对象：
let points = [ // 一个包含2个元素的数组。
    {x: 0, y: 0}, // 每个元素都是一个对象。
    {x: 1, y: 1}
];
let data = { // 具有两个属性的对象
    trial1: [[1, 2], [3, 4]], // 每个属性的值是一个数组。
    trial2: [[2, 3], [4, 5]]  // 数组的元素是数组。
};
```

!!! note "代码示例中的注释语法"

    您可能已经注意到，在前面的代码中，有些注释以箭头 (`=>`) 开头。这些箭头表示注释前代码产生的值，这是我尝试在印刷书籍中模拟类似于网页浏览器控制台的交互式 JavaScript 环境。

    这些 `// =>` 注释也起到断言的作用，我编写了一个工具来测试代码，并验证它是否生成注释中指定的值。我希望这能帮助减少书中的错误。

    有两种相关的注释/断言风格。如果你看到形如 `// a == 42` 的注释，这意味着在注释前的代码运行后，变量 a 的值将为 42。如果你看到形如 `// !` 的注释，这意味着注释前一行的代码会抛出异常（感叹号后的注释通常解释抛出的是哪种异常）。

    你会在整本书中看到这些评论。

这里展示的在方括号中列出数组元素或在花括号中将对象属性名映射到属性值的语法称为`初始化表达式`
，这是第4章的主题之一。表达式是JavaScript中的一个短语，可以被求值以产生一个值。例如，使用 . 和 [] 来引用对象属性或数组元素的值就是一种表达式。

在 JavaScript 中，形成表达式最常见的方法之一是使用运算符：

``` javascript
// 运算符对值（操作数）进行操作以产生新值。
// 算术运算符是最简单的运算符之一：
3 + 2 // => 5: 加法
3 - 2 // => 1: 减法
3 * 2 // => 6: 乘法
3 / 2 // => 1.5: 除法
points[1].x - points[0].x // => 1: 更复杂的操作数也有效
"3" + "2" // => "32": +号用于拼接字符串


// JavaScript 定义了一些简写算术运算符
let count = 0; // 定义一个变量
count++; // 自增
count--; // 自减
count += 2; // 加2：等同于 count = count + 2;
count *= 3; // 乘以3：等同于 count = count * 3;
count // => 6: 变量名也是表达式。


// 相等和关系运算符用于测试两个值是否相等，
// 不等于、小于、大于，等等。它们的结果为真或假。
let x = 2, y = 3; // 这些=符号是赋值，而不是相等性测试
x === y // => false: 相等操作符
x !== y // => true: 不相等操作符
x < y // => true: 小于操作符
x <= y // => true: 小于等于操作符
x > y // => false: 大于操作符
x >= y // => false: 大于等于操作符
"two" === "three" // => false: 两个字符串不相等
"two" > "three" // => true: “tw” 在字母顺序上大于 “th”
false === (x > y) // => true: false等于false


// 逻辑运算符用于组合或取反布尔值
(x === 2) && (y === 3) // => true: 两个比较都为真。&& 表示与
(x > 3) || (y < 3) // => false: 两个比较都不成立。|| 是或运算符
!(x === y) // => true: ! 反转布尔值
```

如果说 JavaScript 表达式像短语，那么 JavaScript `语句`就像完整的句子。`语句`是第 5
章的主题。大致来说，表达式是计算一个值但不执行任何操作的东西：它不会以任何方式改变程序状态。而`语句`
则没有值，但它们会改变状态。你已经在上面看到变量声明和赋值语句。另一大类语句是`控制结构`，比如`条件语句`和`循环`。在我们讲解
`函数`之后，你会看到下面的例子。

函数是一个具名且带参数的 JavaScript 代码块，你可以定义一次，然后反复调用。函数的正式讲解在第 8
章，但就像对象和数组一样，你会在到达那一章之前多次看到它们。以下是一些简单的例子：

``` javascript
// 函数是参数化的 JavaScript 代码块，我们可以调用它们。
function plus1(x) { // 定义一个名为"plus1"、带有参数"x"的函数
    return x + 1; // 返回一个比传入值大一的值
} // 函数用大括号括起来

plus1(y) // => 4: y is 3, so this invocation returns 3+1

let square = function (x) { // 函数是值，可以赋给变量
    return x * x; // 计算函数的值
}; // 分号表示赋值的结束。

square(plus1(y)) // => 16: 在一个表达式中调用两个函数
```

在 ES6 及之后的版本中，有一种简洁的语法用于定义函数。这种简洁的语法使用 `=>` 将参数列表与函数体分开，因此以这种方式定义的函数被称为
`箭头函数`。箭头函数通常用于在需要将一个匿名函数作为参数传递给另一个函数时使用。使用箭头函数重写后的代码如下所示：

``` javascript
const plus1 = x => x + 1; // The input x maps to the output x + 1
const square = x => x * x; // The input x maps to the output x * x
plus1(y) // => 4: 函数调用是相同的
square(plus1(y)) // => 16
```

当我们将函数用于对象时，我们称其为`方法`：

``` javascript
// 当函数被分配给一个对象的属性时，我们称之为
// “它们的方法。” 所有 JavaScript 对象（包括数组）都有方法：
let a = []; // 创建一个空数组
a.push(1, 2, 3); // push() 方法向数组添加元素
a.reverse(); // 另一种方法：颠倒元素的顺序


// 我们也可以定义我们自己的方法。"this"关键字指的是对象。
// 该方法定义于：在这种情况下，是前面提到的点数组。
points.dist = function () { // 定义一种计算点之间距离的方法
    let p1 = this[0]; // 我们调用的数组的第一个元素
    let p2 = this[1]; // this对象的第二个元素
    let a = p2.x - p1.x; // x 坐标的差异
    let b = p2.y - p1.y; // y坐标的差异
    return Math.sqrt(a * a + // 勾股定理
        b * b); // Math.sqrt() 计算平方根
};
points.dist() // => Math.sqrt(2): 我们两个点之间的距离
```

现在，正如承诺的那样，这里有一些函数，其主体展示了常见的 JavaScript `控制结构语句`：

``` javascript
// JavaScript 语句包括使用 C、C++、Java 及其他语言语法的条件语句和循环语句。
function abs(x) {
    // if语句...
    if (x >= 0) {
        return x;
    } else {
        return -x;
    }
}

abs(-10) === abs(10) // => true


function sum(array) {
    let sum = 0;
    // for循环语句
    for (let x of array) {
        sum += x;
    }
    return sum;
}

sum(primes)


function factorial(n) {
    let product = 1;
    // while循环语句
    while (n > 1) {
        product *= n;
        n--;
    }
    return product;
}

factorial(4)

function factorial2(n) {
    let i, product = 1;
    // for循环语句
    for (i = 2; i <= n; i++)
        product *= i;
    return product;
}

factorial2(5) 
```

JavaScript 支持面向对象的编程风格，但与“经典”面向对象编程语言有显著不同。第 9 章详细介绍了 JavaScript
中的面向对象编程，并提供了大量示例。以下是一个非常简单的示例，展示了如何定义一个 JavaScript 类来表示二维几何点。该类的实例对象有一个名为
distance() 的方法，用于计算点到原点的距离：

``` javascript
class Point {
    constructor(x, y) { // 构造函数用于初始化新实例.
        this.x = x;
        this.y = y;
    }

    distance() { // 计算从原点到点的距离的方法。
        return Math.sqrt(
            this.x * this.x +
            this.y * this.y
        );
    }
}

// 使用 "new" 关键字与 Point() 构造函数来创建 Point 对象。
let p = new Point(1, 1);
// 现在使用 Point 对象 p 的方法
p.distance() // => Math.SQRT2
```

本书对JavaScript基本语法和功能的介绍到此结束，但接下来还有独立章节，涵盖该语言的其他特性：

- 第10章：模块
- 地11章：JavaScript标准库
- 第12章：迭代器与生成器
- 第13章：异步JavaScript
- 第14章：元编程
- 第15章：浏览器中的JavaScript
- 第16章：Node服务器端JavaScript
- 第17章：JavaScript工具和扩展
