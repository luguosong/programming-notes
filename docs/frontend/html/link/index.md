# 超链接

超链接（Hyperlink）就像书中的脚注引用——点击就能跳转到另一个地方。它是 Web 的灵魂，正是「超链接」把一张张独立的网页编织成了我们今天看到的互联网。

本文你会学到：

- 🔗 `a` 标签的 `href` 和 `target` 属性及其用法
- 📌 四种常见链接类型：空连接、锚点链接、下载链接、邮件与电话链接
- ⚙️ `base` 标签的作用与 `rel` 属性的安全实践

## 🔗 a 标签基础

`<a>`（anchor，锚）是 HTML 中创建超链接的唯一标签。浏览器会将 `<a>` 包裹的内容渲染为可点击的蓝色文字，点击后跳转到 `href` 属性指向的地址。

### href 与 target 属性

`href`（Hypertext Reference）指定链接的目标地址，`target` 控制在哪个窗口打开。

``` html title="基础链接示例"
--8<-- "docs/frontend/html/link/demo/basic-links.html"
```

<iframe class="html-demo" loading="lazy" src="demo/basic-links.html"></iframe>

`target` 有四种取值：

| 值 | 行为 | 使用场景 |
|------|------|----------|
| `_self` | 在当前窗口打开（默认值） | 站内跳转 |
| `_blank` | 在新标签页打开 | 跳转到外部网站 |
| `_parent` | 在父框架中打开 | 嵌套框架页面 |
| `_top` | 在最顶层窗口打开，跳出所有框架 | 从 iframe 中跳出 |

💡 实际开发中，最常用的就是默认的 `_self` 和打开外部链接时的 `_blank`。`_parent` 和 `_top` 在现代开发中很少使用，因为 `<iframe>` 本身就不太常用了。

⚠️ 使用 `target="_blank"` 打开新页面时，新页面可以通过 `window.opener` 访问原页面的文档对象，存在安全风险。解决方案见下文「rel 属性与安全性」。

## 📌 常见链接类型

除了跳转到网页，`<a>` 标签还可以实现多种功能，取决于 `href` 的写法。

### 空连接

当链接还没有确定目标地址时，可以使用空连接占位：

``` html title="空连接"
--8<-- "docs/frontend/html/link/demo/empty-link.html"
```

<iframe class="html-demo" loading="lazy" src="demo/empty-link.html"></iframe>

`href="#"` 表示跳转到当前页面顶部。虽然常用来做占位符，但实际开发中更推荐用 JavaScript 来处理未完成的链接，避免点击时页面跳动到顶部。

### 锚点链接

锚点链接用于`同页面内的跳转`，点击后平滑滚动到页面指定位置。

开发步骤：

1. `定义锚点`：给目标元素设置 `id` 属性
2. `创建跳转链接`：用 `#id` 作为 `href` 的值

``` html title="锚点链接"
--8<-- "docs/frontend/html/link/demo/anchor-links.html"
```

<iframe class="html-demo" loading="lazy" src="demo/anchor-links.html"></iframe>

💡 如果想点击后平滑滚动而不是瞬间跳转，可以用 CSS 的 `scroll-behavior: smooth` 实现。

### 下载链接

给 `<a>` 标签添加 `download` 属性后，浏览器会直接下载链接指向的文件，而不是打开它：

``` html title="下载链接"
--8<-- "docs/frontend/html/link/demo/download-links.html"
```

<iframe class="html-demo" loading="lazy" src="demo/download-links.html"></iframe>

- 不带值的 `download`：使用服务器上的原始文件名
- 带值的 `download="文件名"`：下载时使用指定的文件名

💡 对于 `.exe`、`.zip` 等浏览器无法直接显示的文件，即使不加 `download` 属性，浏览器也会自动触发下载。`download` 主要用于 `.pdf`、`.html`、`.jpg` 这类浏览器默认会打开的文件。

### 邮件与电话链接

`href` 支持多种协议，不仅能跳转网页，还能唤起系统的邮件客户端或拨号界面：

``` html title="邮件与电话链接"
--8<-- "docs/frontend/html/link/demo/mailto-tel-links.html"
```

<iframe class="html-demo" loading="lazy" src="demo/mailto-tel-links.html"></iframe>

| 协议 | 效果 | 示例 |
|------|------|------|
| `mailto:` | 打开默认邮件客户端，收件人自动填入 | `mailto:hello@example.com` |
| `tel:` | 在移动端唤起拨号界面（桌面端需 VoIP 软件） | `tel:+8613800138000` |

!!! note "MDN"
    `tel:` 协议链接在移动设备上特别实用。用户点击后直接跳转到拨号界面，拨号号码已预填，只需确认拨打即可。

⚠️ `mailto:` 链接会暴露邮箱地址到 HTML 源码中，容易被爬虫抓取。公司网站或大流量站点慎用，否则会收到大量垃圾邮件。更安全的做法是使用联系表单。

## ⚙️ 高级属性

### base 标签

`<base>` 标签放在 `<head>` 中，用来`统一指定文档中所有相对 URL 的基础地址`：

``` html title="base 标签"
--8<-- "docs/frontend/html/link/demo/base-tag.html"
```

<iframe class="html-demo" loading="lazy" src="demo/base-tag.html"></iframe>

!!! note "MDN"
    `<base>` 标签最多只能出现一次，且必须包含 `href` 或 `target` 属性（或两者都有）。它会影响页面中所有使用相对 URL 的资源，包括 `<a>`、`<img>`、`<link>` 等标签。

💡 `<base>` 在单页应用（SPA）部署到子路径时会用到，但日常开发中并不常见。了解即可，遇到相对路径奇怪的问题时记得排查是否有 `<base>` 标签。

### rel 属性与安全性

`rel`（relationship）属性描述当前页面与链接目标之间的关系。最常用的场景是配合 `target="_blank"` 解决安全问题：

``` html title="安全的 external 链接"
--8<-- "docs/frontend/html/link/demo/safe-external-links.html"
```

<iframe class="html-demo" loading="lazy" src="demo/safe-external-links.html"></iframe>

| 值 | 作用 | 必要性 |
|------|------|----------|
| `noopener` | 阻止新页面通过 `window.opener` 访问原页面 | ⚠️ 强烈推荐 |
| `noreferrer` | 不向目标网站发送来源信息（`Referer` 请求头） | 推荐配合使用 |

!!! note "MDN"
    不加 `rel="noopener"` 时，通过 `target="_blank"` 打开的新页面可以使用 `window.opener` 获取原页面的 `window` 对象，进而修改原页面的 URL 甚至窃取信息。现代浏览器（Chrome 88+、Firefox 79+）已默认为新标签页设置 `noopener`，但为了兼容旧浏览器，仍然建议手动添加。

⚠️ 如果使用了 `rel="noreferrer"`，某些依赖 `Referer` 头来做统计分析的网站可能会丢失来源数据。需要根据实际场景权衡。

📝 小结：`<a>` 标签是 HTML 最重要的标签之一。日常开发中记住这几个要点就够了——用 `href` 指定目标地址，用 `target="_blank"` + `rel="noopener noreferrer"` 安全地打开外部链接，用锚点链接实现页面内跳转。
