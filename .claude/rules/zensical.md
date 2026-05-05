# Zensical 特有语法与项目约定

> Zensical 基于 Material for MkDocs，支持 Admonitions、Content Tabs、代码块增强、文本格式化扩展（高亮/下划线/上下标/键位）、图标 Emoji、网格布局等标准功能。以下仅记录**项目特有约定**和 **AI 易错点**。

## 代码块空格规则（易错）

带 `title=` 等属性时，`` ``` `` 与语言标识符之间**必须有空格**：

- ✅ `` ``` java title="示例" ``
- ❌ `` ```java title="示例" ``

## 列表前空行规则（易错）

Markdown 中，段落/文字与无序列表之间**必须有一行空行**，否则列表无法正常渲染。典型场景：`**本文你会学到**：` 等引导语后直接跟 `- ` 列表。

- ✅ 引导语后有空行
  ```markdown
  **本文你会学到**：

  - 第一个知识点
  - 第二个知识点
  ```
- ❌ 引导语后无空行（列表不渲染）
  ```markdown
  **本文你会学到**：
  - 第一个知识点
  - 第二个知识点
  ```

## Admonition 空行 + 缩进规则（易错）

**所有** admonition（`!!!`、`???`、`???+`）的标题行与内容之间**必须有一个空行**，且内容必须**缩进 4 格**，两个条件缺一不可，否则内容不会被渲染为 admonition 内容。

- ✅ 正确写法
  ```markdown
  !!! tip "标题"

      内容正文（缩进 4 格）

  ???+ warning "标题"

      内容正文（缩进 4 格）
  ```
- ❌ 缺少空行（内容不渲染）
  ```markdown
  !!! tip "标题"
      内容正文
  ```
- ❌ 缺少缩进（内容不进入 admonition，成为普通段落）
  ```markdown
  !!! tip "标题"

  内容正文（未缩进）
  ```

## 图片统一格式

```markdown
<!-- 带图注（项目统一格式） -->
<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/xxx.png){ loading=lazy }
  <figcaption>图 1.1 图注说明文字</figcaption>
</figure>
```

图片缩放与画廊使用 zensical 原生 GLightbox 扩展（v0.0.35+），在 `zensical.toml` 中已启用 `[project.markdown_extensions.zensical.extensions.glightbox]`。文档中的图片会自动获得点击放大和画廊浏览功能。

## 图标路径写法差异

- Front matter `icon:` 字段用 `/`：`icon: lucide/database`
- 正文 Markdown 图标短码用 `-`：`:lucide-database:`

## 嵌入外部文件

```markdown
--8<-- "相对路径/文件名"
```

## Front Matter 常用字段

```markdown
---
icon: lucide/book          # 仅二级目录 index.md 设置
status: new                # 需在 zensical.toml extra.status 中定义
hide: [navigation, toc]    # 可选：隐藏左侧导航或右侧目录
---
```

## Mermaid 图表样式规范

Mermaid 在 Shadow DOM 内渲染，外部 CSS 无法穿透，只能通过 CSS 变量继承。深色模式适配已在 `docs/custom/css/custom.css` 的 `[data-md-color-scheme="slate"]` 中配置 `--md-mermaid-*` 变量，**勿删除**。

**样式生成规则**：
1. 节点一律 `fill:transparent`，通过 `stroke` 颜色和粗细区分类别
2. 使用 `classDef` 定义样式类，避免逐个节点写 `style`
3. 使用 `style` 指令时必须同时指定 `stroke`，否则深色模式下边框模糊

```
classDef regular fill:transparent,stroke:#768390,color:#adbac7,stroke-width:1px
classDef lts fill:transparent,stroke:#539bf5,color:#adbac7,stroke-width:2px
```

**禁止**：通过 `extra_javascript` 调用 `mermaid.run()` 或 `mermaid.initialize()` 试图修复**样式**——Shadow DOM 关闭后外部 CSS 无法穿透，唯一有效方式是 CSS 变量继承。

**已修复**：`custom/js/mermaid.js` 修复了 Zensical 即时导航后 Mermaid 图表无法渲染的问题。根因是 bundle 的 `$s()` 函数使用 `ko || fp().pipe(se(1))` 缓存模式，`ko` 在首次渲染后 `take(1)` 已完成，后续 SPA 导航的新订阅者收不到值。修复方式：预加载 Mermaid 库 + `document$.subscribe()` 兜底渲染失败元素。
