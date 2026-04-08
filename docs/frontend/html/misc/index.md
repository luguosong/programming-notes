---
title: 字符实体与交互元素
---

# 字符实体与交互元素

在 HTML 中，有些字符有特殊含义（比如 `<` 和 `>`），不能直接写到页面里；还有些字符键盘上根本打不出来（比如版权符号 `©`）。字符实体就是解决这类问题的。此外，HTML 还提供了原生可折叠内容和对话框，无需 JavaScript 框架即可实现常见的交互效果。

🎯 理解字符实体的作用，掌握常用字符实体的写法
💡 学会用 `details` + `summary` 创建可折叠内容
🔧 学会用 `dialog` 创建原生对话框

## 🔣 字符实体

### 语法

字符实体就像键盘上的 Shift 键——帮你输入那些直接敲不出来的特殊字符。

字符实体以 `&` 开头、`;` 结尾，中间是实体的名称或编号：

```html title="字符实体语法"
--8<-- "docs/frontend/html/misc/demo/character-entity-syntax.html"
```

<iframe class="html-demo" loading="lazy" src="demo/character-entity-syntax.html"></iframe>

💡 推荐使用实体名称（如 `&lt;`），比编号更易读、更容易记忆。但实体编号的兼容性更好——极少数冷门字符可能没有名称，这时就需要用编号。

⚠️ 注意：在 HTML 中，`<`、`>`、`&` 这三个字符有特殊含义（标签标记、实体起始），**必须**用字符实体转义，否则浏览器会将它们误认为标签或实体声明，导致页面解析错误。

### 常用字符实体速查

以下是最常用的字符实体，建议收藏备用：

| 实体 | 显示结果 | 说明 |
|------|---------|------|
| `&lt;` | < | 小于号（left angle bracket） |
| `&gt;` | > | 大于号（right angle bracket） |
| `&amp;` | & | 和号（ampersand） |
| `&quot;` | " | 双引号（quotation mark） |
| `&apos;` | ' | 单引号（apostrophe） |
| `&nbsp;` | （空格） | 不换行空格（non-breaking space） |
| `&copy;` | © | 版权符号（copyright） |
| `&reg;` | ® | 注册商标（registered trademark） |
| `&trade;` | ™ | 商标符号（trademark） |
| `&yen;` | ¥ | 日元符号 |
| `&euro;` | € | 欧元符号 |
| `&pound;` | £ | 英镑符号 |
| `&cent;` | ¢ | 分币符号 |
| `&times;` | × | 乘号 |
| `&divide;` | ÷ | 除号 |
| `&mdash;` | — | 长破折号（em dash） |
| `&ndash;` | – | 短破折号（en dash） |

💡 `&nbsp;` 是最常被忽略的实体——普通空格在 HTML 中会被浏览器折叠（多个连续空格只显示一个），而 `&nbsp;` 不会被折叠，常用于保持单词间的间距。

📝 **小结**：字符实体是 HTML 的「转义机制」，核心作用有两个：一是显示特殊字符（如 `<`、`&`），二是输入键盘上没有的符号（如 `©`、`€`）。日常开发中，`&lt;`、`&gt;`、`&amp;` 这三个用得最多，务必熟记。

---

## 📂 可折叠内容

### details 与 summary

!!! note "MDN"
    `<details>` 创建一个可折叠/展开的 disclosure widget，默认处于收起状态。`<summary>` 作为 `<details>` 的标题，用户点击后切换展开/收起。

以前实现可折叠内容需要写不少 JavaScript，现在 HTML 原生就支持了——只需要两个标签：

```html title="details 与 summary 基础用法"
--8<-- "docs/frontend/html/misc/demo/details-basic.html"
```

<iframe class="html-demo" loading="lazy" src="demo/details-basic.html"></iframe>

💡 `details` 默认收起。加上 `open` 属性可以使其默认展开：

```html title="默认展开的 details"
--8<-- "docs/frontend/html/misc/demo/details-open.html"
```

<iframe class="html-demo" loading="lazy" src="demo/details-open.html"></iframe>

!!! note "MDN"
    `<details>` 的 `open` 属性使其默认显示内容。可以通过 JavaScript 的 `toggle` 事件监听展开/收起状态变化。

`details` 里面不只能放文字，可以放任意 HTML 内容：

```html title="details 包含富内容"
--8<-- "docs/frontend/html/misc/demo/details-rich-content.html"
```

<iframe class="html-demo" loading="lazy" src="demo/details-rich-content.html"></iframe>

⚠️ 注意：`<details>` 内部**必须**以 `<summary>` 作为第一个子元素。如果没有 `<summary>`，浏览器会自动生成一个默认标题（通常显示为"详细信息"）。

📝 **小结**：`details` + `summary` 是 HTML 原生的折叠组件，零 JavaScript 即可工作，非常适合 FAQ、代码说明、可选详情等场景。

---

## 💬 原生对话框

### dialog 标签

!!! note "MDN"
    `<dialog>` 是原生对话框元素。添加 `open` 属性使其默认显示（非模态）；通过 JavaScript 的 `showModal()` 方法可以打开模态对话框，`close()` 方法关闭。

以前弹窗要么用 `alert()`（简陋且无法自定义样式），要么自己用 `div` 模拟。现在 HTML 提供了原生的 `<dialog>` 标签：

```html title="dialog 基础用法"
--8<-- "docs/frontend/html/misc/demo/dialog-basic.html"
```

<iframe class="html-demo" loading="lazy" src="demo/dialog-basic.html"></iframe>

💡 `<dialog>` 有两种显示模式：
- `非模态`：用 `show()` 方法打开，用户可以同时操作对话框和页面
- `模态`：用 `showModal()` 方法打开，对话框后面的页面会被遮罩覆盖，用户必须先处理对话框

```html title="模态 vs 非模态对话框"
--8<-- "docs/frontend/html/misc/demo/dialog-modal-vs-modeless.html"
```

<iframe class="html-demo" loading="lazy" src="demo/dialog-modal-vs-modeless.html"></iframe>

💡 模态对话框中，`<form method="dialog">` 的按钮会自动关闭对话框——不需要手动调用 `close()`。按钮的 `value` 属性会作为 `dialog` 元素的 `returnValue`，方便后续逻辑判断用户点击了哪个按钮。

⚠️ 注意：带 `open` 属性的 `<dialog>` 只是「显示在页面上」，并不具备模态行为（没有遮罩层、不阻止页面交互）。如果要模态效果，必须用 `showModal()` 方法打开。

📝 **小结**：`<dialog>` 是 HTML 原生的对话框方案，支持模态和非模态两种模式。配合 `<form method="dialog">` 可以轻松实现带确认/取消的交互，无需任何 JavaScript 框架。
