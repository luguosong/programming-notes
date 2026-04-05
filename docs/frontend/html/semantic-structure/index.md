# 语义化结构

本文你会学到：

- 🎯 理解什么是 HTML 语义化，以及为什么它比 `div` 布局更好
- 🏗️ 掌握页面布局类语义标签（`header`、`nav`、`main`、`footer` 等）
- 🧩 了解内容分组类语义标签（`article`、`section`、`aside`）
- 📊 对比 `div` 布局与语义化标签的差异
- ✨ 能够编写结构完整、语义清晰的 HTML 页面

## 🤔 为什么需要语义化

### div 布局的局限

早期的 HTML 页面几乎全部用 `div` 来搭建结构。`div` 本身没有任何含义——它只是一个通用的容器（block-level container），浏览器看到 `<div class="header">` 和 `<div class="footer">` 时，只知道"这是一个盒子"，完全不知道"这是页眉"还是"这是页脚"。

💡 用 `div` 布局就像用纸箱分类物品——虽然能装，但箱子外面没有标签，打开才知道里面是什么。而语义标签就像用带标签的收纳盒——一眼就能看出里面装的是什么。

``` html title="纯 div 布局——结构不清晰"
<div class="header">
  <div class="nav">...</div>
</div>
<div class="content">
  <div class="article">...</div>
  <div class="sidebar">...</div>
</div>
<div class="footer">...</div>
```

这段代码能正常显示，但阅读代码的人（以及搜索引擎、屏幕阅读器）需要猜测每个 `div` 的用途。

### 语义化的三大好处

语义化标签用标签名本身来表达内容的含义，而不是依赖 `class` 属性。它带来三个核心好处：

!!! note "MDN"
    语义化 HTML（Semantic HTML）是指使用 HTML 元素来传达页面结构和内容含义的实践。语义化标签不仅让代码更易读，还能显著提升 SEO、无障碍访问和代码可维护性。

| 好处 | 说明 |
|------|------|
| 🔍 SEO（搜索引擎优化） | 搜索引擎能理解页面结构，如 `article` 表示独立文章、`nav` 表示导航，从而更准确地索引内容 |
| ♿ 无障碍访问 | 屏幕阅读器能根据语义标签快速跳转到导航、主内容、页脚等区域，大幅提升视障用户的浏览体验 |
| 🔧 可维护性 | 开发者不需要猜测 `<div class="xxx">` 的用途，标签名直接表达了意图，团队协作效率更高 |

## 🏗️ 页面布局标签

以下标签是构建页面骨架的核心元素，每个标签都有明确的语义角色。

### header 页眉

`header` 表示介绍性内容，通常是一组介绍性或导航性辅助内容。一个页面可以有多个 `header`——页面级别的 `header` 放在最顶部，`article` 或 `section` 内部也可以有自己的 `header`。

``` html title="header 示例"
<!-- 页面级页眉 -->
<header>
  <h1>我的技术博客</h1>
  <p>记录前端学习之路</p>
</header>

<!-- 文章内页眉 -->
<article>
  <header>
    <h2>HTML 语义化入门</h2>
    <p>发布于 2025-01-01</p>
  </header>
  <p>正文内容...</p>
</article>
```

⚠️ `header` 不等于"页面顶部横幅"。它的语义是"介绍性内容"，放在 `article` 中时表示文章标题和元信息，而不是页面的大横幅图片。

### nav 导航

`nav` 表示导航区域，包含一组到其他页面或页面内锚点的链接。不是所有的链接集合都需要用 `nav` 包裹，通常只用于主要导航区块。

``` html title="nav 示例"
<nav>
  <ul>
    <li><a href="/">首页</a></li>
    <li><a href="/blog">博客</a></li>
    <li><a href="/about">关于</a></li>
  </ul>
</nav>
```

💡 页面中一般有 2-3 个 `nav`：顶部主导航、侧边栏分类导航、页脚的辅助链接。面包屑导航（Breadcrumb）也常用 `nav` 包裹。

### main 主内容区

`main` 表示页面的主体内容。一个页面只能有一个 `main` 元素，它不应该包含页面中重复出现的内容（如导航栏、页脚、侧边栏）。

``` html title="main 示例"
<body>
  <header>...</header>
  <nav>...</nav>

  <main>
    <!-- 页面核心内容放这里 -->
    <h1>文章标题</h1>
    <p>文章正文...</p>
  </main>

  <footer>...</footer>
</body>
```

⚠️ `main` 是页面内容的"舞台"——重复出现的幕布（导航、页脚）不属于舞台的一部分。

### article 独立内容

`article` 表示一段独立的、完整的内容，能够脱离上下文独立存在。典型场景：博客文章、新闻条目、论坛帖子、用户评论。

``` html title="article 示例"
<article>
  <header>
    <h2>学习 HTML 语义化的 5 个理由</h2>
    <time datetime="2025-01-01">2025 年 1 月 1 日</time>
  </header>
  <p>语义化标签让你的代码更易读...</p>
  <footer>
    <p>分类：前端基础</p>
  </footer>
</article>
```

💡 判断是否应该用 `article` 的简单方法：这段内容如果单独拿出来放到 RSS 订阅中，是否仍然有意义？如果答案是"是"，那就用 `article`。

### section 章节

`section` 表示文档中的一个章节或主题分组，通常带有标题。它不像 `article` 那样要求内容独立——`section` 是对相关内容的逻辑分组。

``` html title="section 示例"
<article>
  <h1>CSS Flexbox 完全指南</h1>

  <section>
    <h2>什么是 Flexbox</h2>
    <p>Flexbox 是一种一维布局模型...</p>
  </section>

  <section>
    <h2>基本用法</h2>
    <p>通过 display: flex 启用...</p>
  </section>

  <section>
    <h2>常见布局模式</h2>
    <p>居中、等分、侧边栏...</p>
  </section>
</article>
```

⚠️ 不要把 `section` 当作通用的样式容器——如果只是出于样式目的需要包裹元素，应该用 `div` 而不是 `section`。

📌 `article` vs `section` 的区别：`article` 强调"独立性"（能单独传播），`section` 强调"分组性"（逻辑上的一个区块）。

### aside 侧边栏

`aside` 表示与主内容间接相关的内容——侧边栏、广告位、相关链接、辅助说明等。`aside` 中的内容如果移除，不应影响主内容的理解。

``` html title="aside 示例"
<main>
  <article>
    <h1>HTML 入门教程</h1>
    <p>正文内容...</p>
  </article>

  <aside>
    <h2>推荐阅读</h2>
    <ul>
      <li><a href="#">CSS 入门教程</a></li>
      <li><a href="#">JavaScript 入门教程</a></li>
    </ul>
  </aside>
</main>
```

💡 `aside` 可以放在 `main` 内部（作为文章旁边的侧栏），也可以放在 `main` 外部（作为页面级的侧边栏）。

### footer 页脚

`footer` 表示其最近的祖先内容的页脚信息。通常包含版权声明、联系方式、相关链接等。和 `header` 一样，页面和 `article`/`section` 都可以有自己的 `footer`。

``` html title="footer 示例"
<!-- 页面级页脚 -->
<footer>
  <p>&copy; 2025 我的技术博客</p>
  <nav>
    <a href="/privacy">隐私政策</a>
    <a href="/contact">联系方式</a>
  </nav>
</footer>

<!-- 文章内页脚 -->
<article>
  <h2>文章标题</h2>
  <p>正文...</p>
  <footer>
    <p>作者：张三 | 发布于 2025-01-01</p>
  </footer>
</article>
```

## 🆕 其他语义标签

除了页面布局标签，HTML5 还提供了一些表示特定内容含义的语义标签。

### address / hgroup / search

!!! note "MDN"
    `address` 元素提供其最近的 `article` 或 `body` 祖先元素的联系信息。`hgroup` 元素表示标题及其副标题或标语。`search` 元素表示包含搜索控件的部分。

**`address`**——联系信息

`address` 专门用于表示联系信息（邮箱、电话、地址、社交媒体链接等），不应用于表示邮政地址（除非确实是联系用途）。

``` html title="address 示例"
<address>
  <p>作者：张三</p>
  <a href="mailto:zhangsan@example.com">zhangsan@example.com</a>
</address>
```

⚠️ `address` 只表示"最近的 `article` 或 `body` 的联系信息"，不能用来包裹页面中任意位置的地址。

**`hgroup`**——标题组合

`hgroup` 用于将标题和副标题（或标语）组合在一起，避免副标题出现在文档大纲中。

``` html title="hgroup 示例"
<hgroup>
  <h1>HTML 语义化指南</h1>
  <p>让代码自己说话</p>
</hgroup>
```

**`search`**——搜索区域

`search` 标识页面中的搜索功能区域，帮助辅助技术快速定位搜索控件。

``` html title="search 示例"
<search>
  <form action="/search" method="get">
    <input type="search" name="q" placeholder="搜索文章...">
    <button type="submit">搜索</button>
  </form>
</search>
```

## 📊 div 布局 vs 语义化标签

用一张对比表格来总结两者的差异：

| 对比维度 | `div` 布局 | 语义化标签 |
|----------|-----------|-----------|
| 🏷️ 含义 | 无语义，需要靠 `class` 表达意图 | 标签名自带含义，一目了然 |
| 🔍 SEO | 搜索引擎难以区分内容层级 | 搜索引擎能理解页面结构 |
| ♿ 无障碍 | 屏幕阅读器无法识别区域类型 | 可通过 landmark 快速跳转 |
| 🔧 可读性 | 需要阅读 `class` 名才能理解结构 | 标签名直接表达用途 |
| 📦 可移植性 | 离开 CSS 后结构不可读 | 即使无样式，结构依然清晰 |

## ✨ 完整语义化页面示例

下面是一个完整的 HTML 页面，展示了所有语义标签如何组合使用。注意观察页面骨架——即使不看 CSS，也能从标签名理解整体结构。

``` html title="完整的语义化页面" linenums="1"
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <title>我的技术博客</title>
</head>
<body>

  <!-- 页面级页眉 -->
  <header>
    <hgroup>
      <h1>我的技术博客</h1>
      <p>记录前端学习之路</p>
    </hgroup>
    <search>
      <form action="/search" method="get">
        <input type="search" name="q" placeholder="搜索文章...">
        <button type="submit">搜索</button>
      </form>
    </search>
  </header>

  <!-- 主导航 -->
  <nav>
    <ul>
      <li><a href="/">首页</a></li>
      <li><a href="/blog">博客</a></li>
      <li><a href="/about">关于</a></li>
    </ul>
  </nav>

  <!-- 页面主体 -->
  <main>
    <article>
      <!-- 文章页眉 -->
      <header>
        <h2>HTML 语义化入门</h2>
        <time datetime="2025-01-01">2025 年 1 月 1 日</time>
      </header>

      <!-- 文章章节 -->
      <section>
        <h3>为什么需要语义化</h3>
        <p>语义化标签让代码更易读...</p>
      </section>

      <section>
        <h3>常用语义标签</h3>
        <p>header、nav、main、footer 等...</p>
      </section>

      <!-- 文章页脚 -->
      <footer>
        <address>
          作者：<a href="mailto:zhangsan@example.com">张三</a>
        </address>
        <p>分类：前端基础</p>
      </footer>
    </article>

    <!-- 侧边栏 -->
    <aside>
      <h2>推荐阅读</h2>
      <ul>
        <li><a href="#">CSS Flexbox 指南</a></li>
        <li><a href="#">JavaScript 入门</a></li>
      </ul>
    </aside>
  </main>

  <!-- 页面级页脚 -->
  <footer>
    <p>&copy; 2025 我的技术博客</p>
    <nav>
      <a href="/privacy">隐私政策</a>
      <a href="/contact">联系方式</a>
    </nav>
  </footer>

</body>
</html>
```

📝 小结：语义化标签的本质是`让 HTML 代码自己"说话"`。`header` 告诉你"这是页眉"，`nav` 告诉你"这是导航"，`article` 告诉你"这是一篇独立文章"——不需要依赖 `class` 名，标签名就是最好的注释。写代码时多问自己一句"这个区域用什么语义标签最合适"，你的代码质量会提升一个台阶。
