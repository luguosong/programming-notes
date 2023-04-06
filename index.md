---
layout: note
title: Home
nav_order: 10
---


{: .new}
除非准备好致力于终身学习，否则不要进入这个行业。有时，编程似乎是一份高薪、可靠的工作，
但能确保这一点的唯一方法，就是始终让自己更有价值。 ——————Bruce Eckel

# 网站构建技术选型

- 网站采用github pages搭建，主要涉及以下技术：
    - [github pages](https://docs.github.com/cn/pages)
    - [jekyll](https://jekyllrb.com/)
    - [liquid](https://github.com/Shopify/liquid/wiki)[中文文档](https://liquid.bootcss.com/)
    - jekyll主题：[just-the-docs](https://github.com/pmarsceill/just-the-docs)
    - 博客评论功能：[Gitalk](https://github.com/gitalk/gitalk)
    - 页面目录：[tocbot](https://github.com/tscanlin/tocbot)
    - 网站图片：[github图床](https://github.com/guosonglu/images)+[jsdelivr加速](https://www.jsdelivr.com/github)+[PicGo工具](https://github.com/Molunerfinn/PicGo)
    - 图片查看：[Viewer.js](https://github.com/fengyuanchen/viewerjs)
    - 数学公式排版：[LaTeX](https://www.latex-project.org/)[MathJax](http://docs.mathjax.org/en/latest/)
    - 流程图： [mermaid](https://mermaid-js.github.io/mermaid/#/)

# 网站性能优化

由于网站是使用github pages部署的，国内访问相对很慢，做如下优化提高访问速度：

- 网站中的静态资源（图片、js、css）使用[jsdelivr](https://www.jsdelivr.com/github)加速
- 网站使用[cloudflare](https://dash.cloudflare.com/)做CDN加速
- Html页面头部添加 `<meta http-equiv="Cache-Control" content="max-age=7200" />`，防止每次刷新页面都反复获取静态文件
