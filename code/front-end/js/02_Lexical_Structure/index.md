# 词法结构

## JavaScript程序的文本

- JS代码区分大小写。
- JavaScript 会忽略程序中标记之间出现的`空格`。在大多数情况下，JavaScript 也会忽略`换行符`。由于可以在程序中自由使用`空格`和
  `换行符`，你可以以整洁一致的方式格式化和缩进程序，使代码易于阅读和理解。
- 除了常规的`空格字符（\u0020）`外，JavaScript 还识别`制表符`、各种 `ASCII 控制字符`以及多种 `Unicode 空格字符`
  作为空白字符。JavaScript 将`换行符`、`回车符`以及`回车/换行序列`识别为`行终止符`。

## 注释

``` javascript
// 这是一个单行注释.

/* 这也是一个注释 */ // 这里还有另一个注释.

/*
* 这是一个多行注释。每行开头的 * 字符并不是语法的一部分，它们只是看起来很酷！
*/
```

## 字面量

`字面量`是直接出现在程序中的数据值。

``` javascript
12 // 数字 12
1.2 // 数字 1.2
"hello world" // 一串文本
'Hi' // 另一串文本
true // 布尔值 =>真
false // 布尔值 =>假
null // 无对象
```

## 标识符

`标识符`用于在JS中命名常量、变量、属性、函数和类,以及为某些循环提供标记（label）。

`标识符`命名规范：

- 以字母、下划线（`_`）或美元符号(`$`)开头。
- 后续字符可以是字母、数字、下划线（`_`）或美元符号(`$`)。

## 保留字

以下词汇是 JavaScript 语言的一部分。

许多词（如 if、while 和 for）是保留关键字，不能用作常量、变量、函数或类的名称（尽管它们可以用作对象属性的名称）。

其他词（如 from、of、get 和 set）在特定上下文中使用，没有语法歧义，可以合法用作标识符。

还有一些关键字（如 let）为了保持与旧程序的向后兼容性，不能完全保留，因此有复杂的规则来决定它们何时可以用作标识符，何时不能。（例如，let
可以在类外用 var 声明时作为变量名，但不能在类内或用 const 声明时使用。）

最简单的做法是避免将这些词用作标识符，除了 from、set 和 target，这些词是安全的并且已经被广泛使用。

|       |          |          |            |        |        |       |
|-------|----------|----------|------------|--------|--------|-------|
| as    | const    | export   | get        | null   | target | void  |
| async | continue | extends  | if         | of     | this   | while |
| await | debugger | false    | import     | return | throw  | with  |
| break | default  | finally  | in         | set    | true   | yield |
| case  | delete   | for      | instanceof | static | try    |       |
| catch | do       | from     | let        | super  | typeof |       |
| class | else     | function | new        | switch | var    |       |

JavaScript 还保留或限制了一些当前未被语言使用但可能在未来版本中使用的关键字：

|      |            |           |         |         |           |        |
|------|------------|-----------|---------|---------|-----------|--------|
| enum | implements | interface | package | private | protected | public |

由于历史原因，在某些情况下，arguments 和 eval 不能用作标识符，最好完全避免使用。

## Unicode

JS采用Unicode字符集进行程序编写。

!!!warning "编程惯例"

    考虑可移植性和易于编辑。建议标识符中只使用ASCII字母和数字。

### 转义字符

某些老旧的硬件或软件无法处理或显示Unicode字符。为了让JS代码在老的系统中也能兼容展示。可以使用ASCII转义字符来替换Unicode字符。

``` javascript
let café = 1; // 使用 Unicode 字符定义变量
caf\u00e9 // => 1; 使用转义序列访问变量
caf\u{E9} // => 1; 同一转义序列的另一种形式
```

!!! warning

    Unicode 转义字符也可以出现在注释中，但由于注释会被忽略，因此在这种情况下，它们仅被视为 ASCII 字符，而不会被解释为 Unicode。

### 归一化


