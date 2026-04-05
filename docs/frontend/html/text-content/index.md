# 文本内容

HTML 文档的核心就是「文本」——标题、段落、强调、引用、代码……这些标签构成了网页内容的基本骨架。本节将系统学习所有与文本内容相关的 HTML 标签。

🎯 掌握 `h1` ~ `h6` 标题的层级关系与使用原则
💡 理解块级容器 `div` 与行内容器 `span` 的区别
🔧 学会用语义化标签（`strong`/`em`/`del`/`ins`）替代无语义标签（`b`/`i`/`s`/`u`）
⚡ 认识行内语义标签（`mark`、`abbr`、`time` 等）的使用场景

## 🔤 标题

想象你在写一篇报告——封面大标题是最大的，章标题次之，节标题更小，依次递减。HTML 的标题标签就是干这个的，它用六个级别来表达内容的层次结构。

### h1 ~ h6 层级

`h1` 到 `h6` 是标题标签，数字越小级别越高、文字越大：

``` html title="标题标签演示"
--8<-- "docs/frontend/html/text-content/demo/headings.html"
```

<iframe class="html-demo" loading="lazy" src="demo/headings.html"></iframe>

### 标题使用原则

标题不仅是视觉上的大字，更是文档结构的骨架。搜索引擎和屏幕阅读器都依赖标题来理解页面结构，所以使用时要注意以下几点：

✅ `每页只有一个 h1`，它是整个页面的主标题
✅ `层级不能跳跃`——`h1` 下面应该接 `h2`，`h2` 下面接 `h3`，不要从 `h1` 直接跳到 `h3`
✅ `不要为了改字号而用标题`——字号应该用 CSS 控制，标题只用于表达内容的层次关系

``` html title="✅ 正确的标题层级"
--8<-- "docs/frontend/html/text-content/demo/correct-heading-levels.html"
```

<iframe class="html-demo" loading="lazy" src="demo/correct-heading-levels.html"></iframe>

``` html title="❌ 错误的标题层级"
--8<-- "docs/frontend/html/text-content/demo/incorrect-heading-levels.html"
```

<iframe class="html-demo" loading="lazy" src="demo/incorrect-heading-levels.html"></iframe>

📝 **小结**：标题的核心价值在于「语义层次」，而非「视觉大小」。写标题时问自己：这段内容在整篇文章中处于什么层级？答案自然就是该用 `h几`。

---

## 📄 段落与分隔

如果说标题是文章的骨架，那段落就是文章的血肉。段落和分隔线负责把文本组织成易于阅读的块。

### p 段落

`p`（paragraph，段落）是最基础的文本容器，浏览器会自动在段落前后添加间距，文字显示不开时会自动换行：

``` html title="段落演示"
--8<-- "docs/frontend/html/text-content/demo/paragraph.html"
```

<iframe class="html-demo" loading="lazy" src="demo/paragraph.html"></iframe>

💡 `p` 是一个「块级元素」——它默认独占一整行，不会和别的 `p` 并排显示。

### br 换行与 hr 分隔线

!!! note "MDN"
    `br`（break）产生一个换行；`hr`（horizontal rule）在页面中插入一条主题分隔线，用于分隔不同主题的内容区域。

``` html title="br 与 hr 演示"
--8<-- "docs/frontend/html/text-content/demo/br-hr.html"
```

<iframe class="html-demo" loading="lazy" src="demo/br-hr.html"></iframe>

⚠️ 注意：`br` 只用于`诗歌、地址等确实需要手动换行`的场景。普通段落换行应该用多个 `p`，而不是用 `br` 硬换行。

---

## 📦 通用容器

`div` 和 `span` 是 HTML 中最「无个性」的两个标签——它们本身没有任何语义，纯粹用来把内容「装起来」方便你设置样式或操作。

### div 块级容器

`div`（division，分区）是一个`块级元素`，默认独占一行，用于搭建页面的布局结构：

``` html title="div 块级容器"
--8<-- "docs/frontend/html/text-content/demo/div-block.html"
```

<iframe class="html-demo" loading="lazy" src="demo/div-block.html"></iframe>

💡 可以把 `div` 想象成一个「盒子」——这个盒子独占一整行，里面可以放任何东西。

### span 行内容器

`span` 是一个`行内元素`，不会换行，仅包裹文本或行内元素的某一部分，用于对局部内容设置样式或操作：

``` html title="span 行内容器"
--8<-- "docs/frontend/html/text-content/demo/span-inline.html"
```

<iframe class="html-demo" loading="lazy" src="demo/span-inline.html"></iframe>

💡 如果 `div` 是一个「盒子」，那 `span` 就是一截「胶带」——它只包裹内容的一小段，不影响整体排版。

### div 与 span 对比

| 特性 | `div` | `span` |
|------|-------|--------|
| 元素类型 | 块级元素（block） | 行内元素（inline） |
| 是否独占一行 | ✅ 是 | ❌ 否 |
| 默认宽度 | 撑满父容器 | 由内容决定 |
| 典型用途 | 搭建布局结构、划分区域 | 包裹文字局部、设置行内样式 |
| 可包含内容 | 块级元素 + 行内元素 | 仅行内元素和文本 |

📝 **小结**：`div` 用来搭架子（布局），`span` 用来做细节（局部样式）。当你需要把一块内容分组但没有合适的语义标签时，才使用它们——优先选择语义化标签（如 `section`、`article`、`header` 等）。

---

## 💡 强调与重要性

在文章中，我们经常需要突出某些内容——有的表示重要，有的表示语气强调。HTML 提供了两组标签来做这件事：语义化版本和无语义版本。

### strong 与 em（推荐）

`strong` 表示`重要性`（important），`em` 表示`语气强调`（emphasis）。两者都有明确的语义，是应该优先使用的标签：

``` html title="strong 与 em 演示"
--8<-- "docs/frontend/html/text-content/demo/strong-em.html"
```

<iframe class="html-demo" loading="lazy" src="demo/strong-em.html"></iframe>

💡 `strong` 和 `em` 的本质是「重要/强调」，不是「粗体/斜体」——加粗和斜体只是默认视觉效果，可以通过 CSS 修改。

### b 与 i（不推荐）

`b`（bold）和 `i`（italic）只是视觉上的加粗和斜体，`不携带任何语义`。在现代 HTML 中应避免使用：

``` html title="b 与 i（不推荐）"
--8<-- "docs/frontend/html/text-content/demo/b-i-not-recommended.html"
```

<iframe class="html-demo" loading="lazy" src="demo/b-i-not-recommended.html"></iframe>

| 场景 | ✅ 推荐 | ❌ 不推荐 |
|------|---------|----------|
| 重要内容 | `strong` | `b` |
| 语气强调 | `em` | `i` |
| 语义 | "这是重点内容" | "这里是粗体字" |

📝 **小结**：始终优先使用 `strong` 和 `em`。只有当你确实需要加粗/斜体效果但没有任何语义需求时（比如排版中的装饰性文字），才考虑 `b` 和 `i`——但这种情况非常少见。

---

## ✏️ 删除与插入

文档修改时经常需要标记删除了什么、新增了什么。和强调标签一样，HTML 也提供了语义化版本和无语义版本。

### del 与 ins

`del`（deleted）标记被删除的内容，`ins`（inserted）标记新增的内容——它们清晰地表达了「这段文字经历过修改」的语义：

``` html title="del 与 ins 演示"
--8<-- "docs/frontend/html/text-content/demo/del-ins.html"
```

<iframe class="html-demo" loading="lazy" src="demo/del-ins.html"></iframe>

💡 `ins` 还可以配合 `cite` 属性（指向说明修改原因的 URL）和 `datetime` 属性（标注修改时间）使用。

### s 与 u（不推荐）

`s`（strikethrough）是单纯的删除线，`u`（underline）是单纯的下划线，`不携带语义`：

``` html title="s 与 u（不推荐）"
--8<-- "docs/frontend/html/text-content/demo/s-u-not-recommended.html"
```

<iframe class="html-demo" loading="lazy" src="demo/s-u-not-recommended.html"></iframe>

⚠️ 注意：`u` 标签容易被用户误认为是超链接（因为链接默认带下划线），所以更要避免使用。

| 场景 | ✅ 推荐 | ❌ 不推荐 |
|------|---------|----------|
| 删除内容 | `del` | `s` |
| 新增内容 | `ins` | `u` |

---

## 💬 引用与代码

技术文章中，引用别人的话和展示代码是最常见的两种内容形式。HTML 提供了专门的标签来处理这两种场景。

### blockquote 与 q

!!! note "MDN"
    `blockquote` 用于块级长引用，`cite` 属性可指定来源 URL；`q` 用于行内短引用，浏览器会自动添加引号。

``` html title="引用演示"
--8<-- "docs/frontend/html/text-content/demo/blockquote-q.html"
```

<iframe class="html-demo" loading="lazy" src="demo/blockquote-q.html"></iframe>

💡 如果需要标注引用来源，可以配合 `<cite>` 标签（注意与 `cite` 属性区分）：

``` html title="cite 标签标注来源"
--8<-- "docs/frontend/html/text-content/demo/cite-tag.html"
```

<iframe class="html-demo" loading="lazy" src="demo/cite-tag.html"></iframe>

### code 与 pre

!!! note "MDN"
    `code` 用于行内代码片段，默认使用等宽字体；`pre` 用于预格式化文本，会保留源代码中的空格、换行和缩进。

``` html title="code 行内代码"
--8<-- "docs/frontend/html/text-content/demo/code-inline.html"
```

<iframe class="html-demo" loading="lazy" src="demo/code-inline.html"></iframe>

``` html title="pre 预格式化文本"
--8<-- "docs/frontend/html/text-content/demo/pre-formatted.html"
```

<iframe class="html-demo" loading="lazy" src="demo/pre-formatted.html"></iframe>

💡 实际开发中，`code` 和 `pre` 经常组合使用——`pre` 保持格式，`code` 标记语义：

``` html title="pre + code 组合"
--8<-- "docs/frontend/html/text-content/demo/pre-code.html"
```

<iframe class="html-demo" loading="lazy" src="demo/pre-code.html"></iframe>

⚠️ 注意：`<pre>` 内部的 HTML 实体仍然会被解析，所以如果代码中包含 `<`、`>`、`&` 等字符，需要转义为 `&lt;`、`&gt;`、`&amp;`。

### kbd 与 samp

!!! note "MDN"
    `kbd` 表示键盘输入，`samp` 表示程序输出示例——它们都是行内语义标签。

``` html title="kbd 与 samp 演示"
--8<-- "docs/frontend/html/text-content/demo/kbd-samp.html"
```

<iframe class="html-demo" loading="lazy" src="demo/kbd-samp.html"></iframe>

📝 **小结**：`blockquote` 用于大段引用，`q` 用于句中短引用；`code` 标记行内代码，`pre` 保留代码格式；`kbd` 表示按键，`samp` 表示输出——每个标签都有自己的语义场景。

---

## 🏷️ 其他行内语义标签

HTML 还提供了一系列精细的行内语义标签，用于标记文本的特定含义。它们不影响页面外观（除非加了 CSS），但能让浏览器和搜索引擎更好地理解内容。

### mark / small / abbr / time

!!! note "MDN"
    `mark` 用于高亮标记（类似荧光笔效果）；`small` 表示附注或小字；`abbr` 表示缩写；`time` 表示时间，`datetime` 属性提供机器可读格式。

``` html title="mark 与 small 演示"
--8<-- "docs/frontend/html/text-content/demo/mark-small.html"
```

<iframe class="html-demo" loading="lazy" src="demo/mark-small.html"></iframe>

💡 `small` 不是用来缩小字号的——它的语义是「旁注、免责声明、版权声明」等附属信息。

``` html title="abbr 缩写演示"
--8<-- "docs/frontend/html/text-content/demo/abbr.html"
```

<iframe class="html-demo" loading="lazy" src="demo/abbr.html"></iframe>

💡 鼠标悬停在 `abbr` 上时，浏览器会显示 `title` 属性的值作为提示，这对缩写术语非常实用。

``` html title="time 时间演示"
--8<-- "docs/frontend/html/text-content/demo/time.html"
```

<iframe class="html-demo" loading="lazy" src="demo/time.html"></iframe>

💡 `time` 的 `datetime` 属性使用 ISO 8601 格式，方便搜索引擎和日历应用解析。页面显示的文字可以是人类友好的格式（如"明天下午"），而 `datetime` 提供机器精确的值。

### sub / sup / wbr / data / var

!!! note "MDN"
    `sub` 和 `sup` 分别表示下标和上标；`wbr`（word break opportunity）建议浏览器在此处换行；`data` 关联机器可读数据；`var` 表示变量（数学或编程中的变量名）。

``` html title="sub 与 sup 演示"
--8<-- "docs/frontend/html/text-content/demo/sub-sup.html"
```

<iframe class="html-demo" loading="lazy" src="demo/sub-sup.html"></iframe>

``` html title="wbr 与 data 演示"
--8<-- "docs/frontend/html/text-content/demo/wbr-data.html"
```

<iframe class="html-demo" loading="lazy" src="demo/wbr-data.html"></iframe>

``` html title="var 变量演示"
--8<-- "docs/frontend/html/text-content/demo/var.html"
```

<iframe class="html-demo" loading="lazy" src="demo/var.html"></iframe>

📝 **小结**：这些行内语义标签的核心理念是「给文本赋予含义」。虽然不加它们页面也能正常显示，但加上之后，搜索引擎能更好地索引内容，屏幕阅读器能更准确地朗读，整个文档的语义质量也会提升。

---

## 📝 小结

本节学习了 HTML 文本内容相关的全部标签，下面做一个快速回顾：

| 分类 | 语义化标签（推荐） | 无语义标签（不推荐） | 说明 |
|------|-------------------|---------------------|------|
| 加粗 | `strong` | `b` | 表示重要性 vs 纯视觉加粗 |
| 斜体 | `em` | `i` | 表示强调 vs 纯视觉斜体 |
| 删除 | `del` | `s` | 表示删除 vs 纯视觉删除线 |
| 下划线 | `ins` | `u` | 表示插入 vs 纯视觉下划线 |
| 容器 | `section` / `header` / `footer` 等 | `div` | 语义化布局 vs 纯容器 |
| 行内容器 | `mark` / `abbr` / `time` 等 | `span` | 语义化标记 vs 纯包裹 |

核心原则只有一个：**优先使用有语义的标签，让 HTML 不仅是给人看的，也是给机器读的。**

→ 更语义化的布局方式将在「语义化结构」中介绍。
