---
title: 列表
---

# 列表

列表是 HTML 中组织内容的基础结构，能让信息层次分明、易于阅读。HTML 提供了三种列表类型：无序列表、有序列表和描述列表，分别适用于不同的场景。

本文你会学到：

- 🎯 掌握三种 HTML 列表类型的语法与语义
- 🔧 了解 `ol` 的 `reversed`、`start`、`type` 等实用属性
- 🧱 学会列表的嵌套写法与注意事项
- 📊 根据内容特点选择最合适的列表类型

## 📋 列表类型概览

三种列表各有分工，下表帮助你快速建立整体认知：

| 类型 | 标签 | 默认样式 | 适用场景 | 类比 |
|------|------|---------|---------|------|
| 无序列表 | `ul` + `li` | 圆点标记 | 项目之间没有先后顺序 | 购物清单 |
| 有序列表 | `ol` + `li` | 数字编号 | 项目之间存在明确的顺序 | 菜谱步骤 |
| 描述列表 | `dl` + `dt` + `dd` | 无标记，缩进排列 | 术语解释、键值对 | 词典词条 |

`核心原则`：列表的语义比样式更重要。不要因为「想要圆点」就用 `ul`——如果内容有明确的先后顺序，应该用 `ol`。

## 🔘 无序列表

无序列表（Unordered List）表示各项内容之间`没有先后顺序`，浏览器默认在每个 `li` 前显示一个圆点（`•`）。

### ul 与 li

`ul`（Unordered List）是列表容器，`li`（List Item）是列表中的每一项。一个 `ul` 中可以包含任意数量的 `li`。

``` html title="无序列表基本结构"
--8<-- "docs/frontend/html/list/demo/unordered-basic-structure.html"
```

<iframe class="html-demo" loading="lazy" src="demo/unordered-basic-structure.html"></iframe>

💡 `ul` 的子元素只能是 `li`（或脚本/模板元素）。不要在 `ul` 内直接放 `div`、`p` 等其他标签——如果需要更复杂的结构，把内容放在 `li` 内部。

``` html title="li 内部可以包含其他元素"
--8<-- "docs/frontend/html/list/demo/li-contains-other-elements.html"
```

<iframe class="html-demo" loading="lazy" src="demo/li-contains-other-elements.html"></iframe>

## 🔢 有序列表

有序列表（Ordered List）表示各项内容之间存在`明确的顺序关系`，浏览器默认在每个 `li` 前显示数字编号。

### ol 与 li

`ol`（Ordered List）的用法与 `ul` 类似，只是容器标签不同：

``` html title="有序列表基本结构"
--8<-- "docs/frontend/html/list/demo/ordered-basic-structure.html"
```

<iframe class="html-demo" loading="lazy" src="demo/ordered-basic-structure.html"></iframe>

⚠️ 有序列表的编号是由浏览器自动生成的。如果用 CSS 去掉了默认样式，但内容本身有顺序语义，`ol` 仍然是正确的选择——语义不会因为样式的改变而消失。

### 有序列表的额外属性

!!! note "MDN"
    `ol` 元素支持以下属性来控制编号行为，这些属性让有序列表更加灵活。

| 属性 | 说明 | 示例 |
|------|------|------|
| `reversed` | 倒序编号（布尔属性，无需赋值） | `<ol reversed>` |
| `start` | 指定起始编号 | `<ol start="5">` |
| `type` | 指定编号类型 | `<ol type="A">` |

`type` 属性支持的编号类型：

| 值 | 编号样式 | 示例 |
|----|---------|------|
| `1`（默认） | 阿拉伯数字 | 1, 2, 3 |
| `A` | 大写字母 | A, B, C |
| `a` | 小写字母 | a, b, c |
| `I` | 大写罗马数字 | I, II, III |
| `i` | 小写罗马数字 | i, ii, iii |

``` html title="从第 5 步开始的有序列表"
--8<-- "docs/frontend/html/list/demo/ordered-start-attribute.html"
```

<iframe class="html-demo" loading="lazy" src="demo/ordered-start-attribute.html"></iframe>

``` html title="使用 type 指定大写字母编号"
--8<-- "docs/frontend/html/list/demo/ordered-type-attribute.html"
```

<iframe class="html-demo" loading="lazy" src="demo/ordered-type-attribute.html"></iframe>

``` html title="倒序列表"
--8<-- "docs/frontend/html/list/demo/ordered-reversed.html"
```

<iframe class="html-demo" loading="lazy" src="demo/ordered-reversed.html"></iframe>

💡 `reversed` + `start` 可以组合使用，例如 `<ol reversed start="10">` 会从 10 倒数到 1。

## 📖 描述列表

描述列表（Description List）用于展示`术语与描述`的配对关系。它由三部分组成：

- `dl`（Description List）— 列表容器
- `dt`（Description Term）— 术语 / 标题
- `dd`（Description Details）— 描述 / 内容

### dl / dt / dd

``` html title="描述列表基本结构"
--8<-- "docs/frontend/html/list/demo/description-basic-structure.html"
```

<iframe class="html-demo" loading="lazy" src="demo/description-basic-structure.html"></iframe>

💡 一个 `dt` 可以对应多个 `dd`（一个术语有多个描述），多个 `dt` 也可以共用一个 `dd`（多个术语指向同一描述）。

``` html title="一对多和多对一的描述列表"
--8<-- "docs/frontend/html/list/demo/description-one-to-many.html"
```

<iframe class="html-demo" loading="lazy" src="demo/description-one-to-many.html"></iframe>

⚠️ `dl` 的直接子元素只能是 `dt` 和 `dd`（以及脚本/模板元素）。不要在 `dl` 内直接嵌套其他标签。

## 🔗 列表嵌套

列表可以嵌套使用，即在 `li` 内部再放入一个完整的列表。嵌套时`内层列表必须放在 li 标签内部`，而不是 `li` 之间。

``` html title="正确的嵌套写法"
--8<-- "docs/frontend/html/list/demo/nested-list-correct.html"
```

<iframe class="html-demo" loading="lazy" src="demo/nested-list-correct.html"></iframe>

❌ 以下写法是`错误的`——内层 `ul` 放在了 `li` 的外面：

``` html title="错误的嵌套写法——内层列表不在 li 内部"
--8<-- "docs/frontend/html/list/demo/nested-list-incorrect.html"
```

<iframe class="html-demo" loading="lazy" src="demo/nested-list-incorrect.html"></iframe>

💡 不同类型的列表也可以互相嵌套，例如在 `ol` 中嵌套 `ul` 来展示步骤的细节：

``` html title="有序列表中嵌套无序列表"
--8<-- "docs/frontend/html/list/demo/ordered-nested-unordered.html"
```

<iframe class="html-demo" loading="lazy" src="demo/ordered-nested-unordered.html"></iframe>

⚠️ 嵌套层级建议不超过 3 层。过深的嵌套不仅影响可读性，也会给屏幕阅读器用户带来导航困难。

## 📊 列表使用场景对比

!!! note "MDN"
    选择列表类型时，关注的是`内容的语义关系`，而非视觉呈现。如果需要更改列表的标记样式，应通过 CSS 实现，而不是更换列表类型。

| 场景 | 推荐列表 | 原因 |
|------|---------|------|
| 功能特性清单 | `ul` | 各功能之间没有顺序关系 |
| 操作步骤 / 排行榜 | `ol` | 顺序本身就是关键信息 |
| 术语表 / FAQ | `dl` | 术语和描述是一对一的映射关系 |
| 导航菜单 | `ul`（嵌套） | 菜单项无先后顺序，嵌套表示层级 |
| 食谱步骤（含子步骤） | `ol`（嵌套 `ul`） | 外层有序，内层补充细节无序 |

📝 小结：三种列表的分工可以用一句话概括——**无序看项目、有序看顺序、描述看配对**。在编写页面时，先问自己「这些内容之间是什么关系」，答案自然会指向合适的列表类型。
