---
layout: note
title: CSS和文档
nav_order: 10
parent: CSS
create_time: 2023/7/13
---

# 历史

- 于1994年，`哈肯·维姆·莱`和`伯特·波斯`合作设计CSS。他们在1994年首次在芝加哥的一次会议上第一次展示了CSS的建议。
- 1996年12月发表的CSS1的要求
- 1998年5月W3C发表了CSS2
- 2004年W3C发表了CSS2.1
- CSS3分成了不同类别，称为`modules`。每个模块定义了一组相关的特性。CSS3是一个巨大的规范，由许多不同的模块组成。这些模块包括选择器、盒模型、背景和边框、文字特效、2D/3D转换、动画、多列布局等等。
- W3C于2011年9月29日开始了设计CSS4。直至现时只有极少数的功能被部分网页浏览器支持

# 置换元素和非置换元素

- `置换元素（replaceable element）`：用来替换元素内容的元素，例如`<img>`、`<input>`、`<textarea>`、`<select>`、`<object>`、`<video>`、`<iframe>`等等。
- `非置换元素（non-replaceable element）`：元素的内容是由元素的内容决定的，例如`<p>`、`<span>`、`<div>`、`<a>`、`<em>`、`<strong>`等等。

# 块级元素和行内元素

- `块级元素（block-level element）`：块级元素呈现`块状`，它有自己的`宽度`和`高度`，也就是可以自定义`width`和`height`。除此之外，块级元素比较霸道，它独自占据一行高度（float浮动除外），一般可以作为其他容器使用，可容纳块级元素和行内元素。
- `行内元素（inline element）`：行内元素也称为`内联元素`。它不占有独立区域，`其大小仅仅被动的依赖于自身内容的大小`（例如文字和图片），所以一般不能随意设置其宽高、对齐等属性。每一个行内元素可以和别的行内元素共享一行，相邻的行内元素会排列在同一行里，直到一行排不下了，才会换行

{% highlight html %}
{% include_relative Block-level-and-in-line-elements.html %}
{% endhighlight %}

[效果](Block-level-and-in-line-elements.html)

{: .warning}
> 行内元素可以放在块级元素里面，但是块级元素不能放在行内元素里面

# 把CSS应用到HTML上

## link标签

```html
<link rel="stylesheet" type="text/css" href="style.css" media="all">
```

- `rel`: 定义当前文档与被链接文档之间的关系，这里是`stylesheet`，表示被链接的文档是一个样式表文件
- `type`: 定义被链接文档的MIME类型，这里是`text/css`，表示被链接的文档是一个CSS文件
- `href`: 定义被链接文档的位置，这里是`style.css`，表示被链接的文档是`style.css`文件
- `media`: 定义了被链接文档的目标媒体类型，这里是`all`，表示被链接的文档适用于所有媒体类型

 
