---
title: 表格
---

# 表格

HTML 表格就像 Excel 中的电子表格——用行和列组织结构化数据。浏览器会将表格元素渲染为网格布局，让信息整齐排列。

本文你会学到：
- 🎯 表格的基本结构和各标签的职责
- 🔧 使用 `thead`、`tbody`、`tfoot` 对表格进行语义分组
- 🧩 通过 `colspan` 和 `rowspan` 合并单元格
- 💡 使用 `colgroup`、`scope` 等高级特性优化表格

## 📊 基本结构

一个最基本的 HTML 表格只需要几个标签就能运转。先认识它们各自的角色：

| 标签 | 全称 | 职责 |
|------|------|------|
| `table` | Table | 表格容器，所有表格内容都放在它里面 |
| `caption` | Caption | 表格标题，描述表格内容，提升无障碍性 |
| `tr` | Table Row | 定义一行 |
| `th` | Table Header | 表头单元格，默认加粗居中 |
| `td` | Table Data | 数据单元格，存放具体内容 |

### table / caption / tr / th / td

下面是一个最简表格——`table` 包裹整体，`caption` 提供标题，`tr` 定义行，`th` 定义表头，`td` 填充数据：

``` html title="基本表格结构"
--8<-- "docs/frontend/html/table/demo/basic.html"
```

<iframe class="html-demo" loading="lazy" src="demo/basic.html" style="height:300px"></iframe>

⚠️ 注意：`caption` 标签必须是 `table` 的第一个子元素（紧跟 `<table>` 开始标签之后），不能放在其他位置。

## 🏗️ 结构分组

当表格数据量较大时，可以把行分成三个逻辑区域：`thead`（表头）、`tbody`（表体）、`tfoot`（表尾）。这样做有两个好处：
- `语义更清晰`：浏览器和屏幕阅读器能区分表头和数据
- `打印友好`：长表格分页时，`thead` 和 `tfoot` 会在每一页重复显示

### thead / tbody / tfoot

下面是一个包含完整分组的表格示例：

``` html title="结构分组的完整表格"
--8<-- "docs/frontend/html/table/demo/structural-grouping.html"
```

<iframe class="html-demo" loading="lazy" src="demo/structural-grouping.html" style="height:300px"></iframe>

📌 小结：实际开发中，`<thead>` + `<tbody>` 几乎是表格的标准写法。`<tfoot>` 用得较少，但在财务报表、数据统计等需要"合计行"的场景中很实用。

## 🔲 合并单元格

有时候一个单元格需要占据多列或多行的空间。HTML 提供了两个属性来实现这一点：

- `colspan`：跨列合并（水平方向）
- `rowspan`：跨行合并（垂直方向）

### colspan 跨列合并

`colspan` 让一个单元格横向占据多个列。最常见的场景是表头分组——用一个"大表头"覆盖多个子列：

``` html title="colspan 跨列合并"
--8<-- "docs/frontend/html/table/demo/colspan.html"
```

<iframe class="html-demo" loading="lazy" src="demo/colspan.html" style="height:300px"></iframe>

上面的例子中，`<th colspan="2">期中考试</th>` 让"期中考试"这个表头横跨两列（语文和数学）。

### rowspan 跨行合并

`rowspan` 让一个单元格纵向占据多行。最常见的场景是表格左侧有"分类"列——多个数据行共享同一个分类名：

``` html title="rowspan 跨行合并"
--8<-- "docs/frontend/html/table/demo/rowspan.html"
```

<iframe class="html-demo" loading="lazy" src="demo/rowspan.html" style="height:300px"></iframe>

### 合并原理：左上原则

合并单元格时遵循`左上原则`，三步操作：

1. `确定方向`：是要跨行（`rowspan`）还是跨列（`colspan`），或者两者兼有
2. `找到左上角`：在合并区域中找到最左上角的那个单元格，在它上面写 `colspan` / `rowspan` 属性
3. `删除多余单元格`：把被合并掉的单元格从 HTML 中删掉

💡 举个例子——假设要把下面 2×2 区域合并成一个单元格：

```
┌────┬────┐
│ A  │ B  │    →    ┌─────────┐
├────┼────┤         │   A B    │
│ C  │ D  │         │   C D    │
└────┴────┘         └─────────┘
```

具体操作：
- 左上角是 A 所在的 `<td>`，给它加上 `colspan="2" rowspan="2"`
- 删掉 B、C、D 对应的 `<td>` 标签

``` html title="左上原则示例"
--8<-- "docs/frontend/html/table/demo/merge-principle.html"
```

<iframe class="html-demo" loading="lazy" src="demo/merge-principle.html" style="height:300px"></iframe>

⚠️ 注意：合并后，被覆盖位置的那个 `<td>` 必须从 HTML 中删除，否则表格布局会错乱。如果删除后某行的 `<td>` 数量不对，浏览器会自动补齐空单元格。

## ⚙️ 高级特性

### colgroup 与 col

!!! note "MDN"
    `<colgroup>` 配合 `<col>` 可以对表格的整列统一设置样式，而无需逐个单元格添加 CSS。

`<colgroup>` 放在 `<table>` 内、`<thead>` 之前，用 `<col>` 定义每一列的样式：

``` html title="colgroup 与 col"
--8<-- "docs/frontend/html/table/demo/colgroup.html"
```

<iframe class="html-demo" loading="lazy" src="demo/colgroup.html" style="height:300px"></iframe>

`<col>` 也可以用 `span` 属性一次定义多列：

``` html title="col 的 span 属性"
--8<-- "docs/frontend/html/table/demo/col-span.html"
```

<iframe class="html-demo" loading="lazy" src="demo/col-span.html" style="height:300px"></iframe>

💡 实际开发中，列样式更多用 CSS 的 `:nth-child()` 选择器实现，`<colgroup>` 的样式能力有限（只能设置背景、宽度、边框、可见性等少数属性）。

### scope 无障碍属性

!!! note "MDN"
    `scope` 属性用于 `<th>` 元素，告诉屏幕阅读器该表头关联的是行、列还是列组/行组，取值为 `col`、`colgroup`、`row`、`rowgroup`。

对于简单的行列表头，`scope` 帮助屏幕阅读器正确定位表头与数据的关系：

``` html title="scope 无障碍属性"
--8<-- "docs/frontend/html/table/demo/scope.html"
```

<iframe class="html-demo" loading="lazy" src="demo/scope.html" style="height:300px"></iframe>

`scope` 的四个取值说明：

| 值 | 含义 | 适用场景 |
|----|------|---------|
| `col` | 表头关联当前列 | 最常见，用于普通列头 |
| `row` | 表头关联当前行 | 用于每行开头的行标题 |
| `colgroup` | 表头关联 `colgroup` 划分的列组 | 配合 `colgroup span` 使用 |
| `rowgroup` | 表头关联 `thead`/`tbody`/`tfoot` 划分的行组 | 复杂表格中的分组表头 |

📌 小结：对于简单的表格，只加 `scope="col"` 和 `scope="row"` 就够了。复杂的双层表头表格，可以进一步使用 `scope="colgroup"` 和 `scope="rowgroup"` 提升无障碍体验。
