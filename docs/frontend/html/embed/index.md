---
title: 嵌入与脚本
---

# 嵌入与脚本

本文你会学到：

- 🎯 理解如何用 `iframe` 在网页中嵌入另一个网页
- 🔍 了解 `embed` 与 `object` 标签的用途与区别
- 💻 掌握 `script` 标签引入 JavaScript 的常见方式
- 🎨 认识 `canvas` 绘图画布的基本概念
- 🧩 初步了解 `template` 与 `slot` 在组件化中的作用

## 🖼️ 怎么在页面里"开窗户"？——嵌入外部内容

HTML 提供了多种方式将外部内容嵌入到当前页面中。就像在墙上挂一幅画、装一扇窗户——你可以在页面中"打开一个窗口"看到别处的内容。

### iframe 嵌入网页

`iframe`（inline frame）就像在网页中开了一扇窗户——透过这扇窗户，你可以看到另一个完整的网页。浏览器会在 `iframe` 内部独立渲染一个页面，它拥有自己的浏览上下文（browsing context），与父页面相互隔离。

```html title="iframe 基本用法"
--8<-- "docs/frontend/html/embed/demo/iframe-basic.html"
```

<iframe class="html-demo" loading="lazy" src="demo/iframe-basic.html"></iframe>

⚠️ 始终为 `iframe` 设置 `title` 属性——屏幕阅读器依赖它来告知用户这个框架的内容是什么，缺少 `title` 会导致无障碍访问体验很差。

#### 常用属性

| 属性 | 说明 |
|------|------|
| `src` | 嵌入页面的 URL |
| `width` / `height` | 框架尺寸，单位为像素 |
| `sandbox` | 安全沙箱限制，控制嵌入页面的权限 |
| `srcdoc` | 直接内联 HTML 内容，替代 `src` 加载外部页面 |
| `loading` | `lazy` 延迟加载（进入视口时才加载），`eager` 立即加载 |
| `title` | 框架的描述性标题（无障碍必需） |

#### sandbox 安全限制

嵌入第三方内容就像允许陌生人进你家——你需要限制他能做什么。`sandbox` 属性正是一种权限控制机制，默认情况下它会施加最严格的限制：

```html title="sandbox 基本用法——施加最严格限制"
--8<-- "docs/frontend/html/embed/demo/sandbox-basic.html"
```

<iframe class="html-demo" loading="lazy" src="demo/sandbox-basic.html"></iframe>

默认的 `sandbox` 会禁止嵌入页面执行脚本、提交表单、弹出窗口等操作。你可以通过添加空格分隔的值来逐项放行权限：

```html title="sandbox 逐项放行权限"
--8<-- "docs/frontend/html/embed/demo/sandbox-permissions.html"
```

<iframe class="html-demo" loading="lazy" src="demo/sandbox-permissions.html"></iframe>

⚠️ **安全提醒**：嵌入第三方内容始终存在安全风险（如点击劫持、恶意脚本）。始终使用 `sandbox` 施加最小必要权限，不要使用无限制的 `iframe`。

#### srcdoc 内联 HTML

`srcdoc` 让你直接在属性中写 HTML，无需加载外部页面：

```html title="srcdoc 内联示例"
--8<-- "docs/frontend/html/embed/demo/srcdoc-inline.html"
```

<iframe class="html-demo" loading="lazy" src="demo/srcdoc-inline.html"></iframe>

#### loading 延迟加载

设置 `loading="lazy"` 后，浏览器会在 `iframe` 进入视口时才加载内容，适合页面底部或用户可能不会滚动到的嵌入区域：

```html title="延迟加载示例"
--8<-- "docs/frontend/html/embed/demo/loading-lazy.html"
```

<iframe class="html-demo" loading="lazy" src="demo/loading-lazy.html"></iframe>

### embed 与 object

除了 `iframe`，HTML 还提供了 `embed` 和 `object` 两个标签来嵌入外部内容。它们的历史比 `iframe` 更久远，主要用于嵌入 PDF、多媒体插件等非网页内容。

#### embed

`embed` 是一个自闭合标签，用法简单直接。它本身没有后备内容的能力——如果浏览器不支持该类型的内容，用户什么都看不到。

```html title="embed 嵌入 PDF"
--8<-- "docs/frontend/html/embed/demo/embed-pdf.html"
```

<iframe class="html-demo" loading="lazy" src="demo/embed-pdf.html"></iframe>

#### object

`object` 比 `embed` 更强大，因为它可以在标签内部嵌套后备内容（fallback）。当浏览器无法渲染 `object` 指定的内容时，会显示标签内部的内容：

```html title="object 嵌套后备内容"
--8<-- "docs/frontend/html/embed/demo/object-fallback.html"
```

<iframe class="html-demo" loading="lazy" src="demo/object-fallback.html"></iframe>

💡 简单来说：`embed` 是"要么显示，要么空白"；`object` 是"要么显示，要么显示你准备好的替代内容"。

| 标签 | 后备内容 | 典型用途 |
|------|---------|---------|
| `embed` | 不支持 | 简单嵌入 PDF、SVG、多媒体 |
| `object` | 支持（标签内部） | 嵌入外部资源并提供降级方案 |
| `iframe` | 支持（标签内部） | 嵌入完整网页 |

## 💻 怎么让页面动起来？——脚本与绘图

现代网页的交互性和动态效果离不开 JavaScript。HTML 提供了 `script` 和 `canvas` 两个标签来支撑脚本执行和绘图能力。

### script 引入 JavaScript

`script` 标签是 HTML 中引入 JavaScript 的标准方式。你可以在标签内直接写代码，也可以通过 `src` 属性引入外部文件。

```html title="内联脚本"
--8<-- "docs/frontend/html/embed/demo/inline-script.html"
```

<iframe class="html-demo" loading="lazy" src="demo/inline-script.html"></iframe>

```html title="引入外部脚本文件"
--8<-- "docs/frontend/html/embed/demo/external-script.html"
```

<iframe class="html-demo" loading="lazy" src="demo/external-script.html"></iframe>

#### 常用属性

| 属性 | 说明 |
|------|------|
| `src` | 外部脚本文件的 URL |
| `type` | 脚本类型，默认为 `text/javascript`；可设为 `module` 启用 ES 模块 |
| `defer` | 延迟执行，等 HTML 解析完毕后再执行脚本（按顺序） |
| `async` | 异步加载，下载完成后立即执行（不保证顺序） |
| `integrity` | 子资源完整性校验，确保文件未被篡改 |

`defer` 和 `async` 的区别是高频考点：

```html title="defer vs async"
--8<-- "docs/frontend/html/embed/demo/defer-async.html"
```

<iframe class="html-demo" loading="lazy" src="demo/defer-async.html"></iframe>

💡 简单来说：`defer` 是"等文档读完再执行，且按书写顺序"，`async` 是"谁先下载完谁先执行"。现代开发中，大多数脚本推荐使用 `defer`。

!!! tip "延伸阅读"
    JavaScript 的详细用法（变量、函数、DOM 操作、事件处理等）将在 JavaScript 笔记中展开。

### canvas 绘图

`canvas` 是 HTML5 引入的绘图标签，它本身只是一块空白的矩形画布——所有图形、动画都需要通过 JavaScript 来绘制。

```html title="canvas 基本结构"
--8<-- "docs/frontend/html/embed/demo/canvas-basic.html"
```

<iframe class="html-demo" loading="lazy" src="demo/canvas-basic.html"></iframe>

配合 JavaScript，你可以在这块画布上绘制各种图形：

```javascript title="绘制一个矩形"
--8<-- "docs/frontend/html/embed/demo/draw-rectangle.js"
```

<iframe class="html-demo" loading="lazy" src="demo/draw-rectangle.html"></iframe>

💡 `canvas` 的应用场景非常广泛——数据可视化图表（如 Chart.js）、游戏画面、图片处理、动画效果等都依赖它。但它的本质就是一块"让 JavaScript 画画的板子"，标签本身不提供任何绘图能力。

!!! tip "延伸阅读"
    `canvas` 的详细绘图 API（路径、渐变、动画、像素操作等）将在 JavaScript 笔记中展开。

## 🧩 怎么复用 HTML 片段？——template 与 slot

现代前端开发越来越强调"组件化"——将页面拆分成独立、可复用的模块。HTML 原生也提供了支持组件化的标签。

### template 与 slot

`template` 和 `slot` 是 Web Components 标准的基础标签，它们让开发者可以定义可复用的 HTML 模板。

`template` 标签中的内容在页面加载时`不会渲染`，它只是一段"待用"的 HTML 片段。你可以用 JavaScript 在需要时克隆它、插入到页面中：

```html title="template 定义与使用"
--8<-- "docs/frontend/html/embed/demo/template-usage.html"
```

<iframe class="html-demo" loading="lazy" src="demo/template-usage.html"></iframe>

`slot` 则用于定义模板中的"插槽"——让使用模板的人可以在指定位置插入自定义内容：

```html title="slot 插槽示例"
--8<-- "docs/frontend/html/embed/demo/slot-demo.html"
```

<iframe class="html-demo" loading="lazy" src="demo/slot-demo.html"></iframe>

!!! note "MDN"
    `template` 和 `slot` 是 Web Components 规范的一部分，与 `Custom Elements`、`Shadow DOM` 配合使用可以实现完整的原生组件化方案。这部分内容较为进阶，将在后续独立整理。目前只需了解它们的存在和基本概念即可。

💡 现代前端框架（React、Vue）中的"组件"概念，本质上就是从这些原生能力中演化而来的——只不过框架提供了更简洁、更强大的语法糖。
