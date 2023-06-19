---
layout: note
title: 主页
nav_order: 10
---


{: .note}
除非准备好致力于终身学习，否则不要进入这个行业。有时，编程似乎是一份高薪、可靠的工作，
但能确保这一点的唯一方法，就是始终让自己更有价值。 ——————Bruce Eckel

# 网站构建技术选型

- 网站采用github pages搭建，主要涉及以下技术：
    - [github pages](https://docs.github.com/cn/pages)
    - [jekyll](https://jekyllrb.com/)
    - [liquid](https://github.com/Shopify/liquid/wiki)[中文文档](https://liquid.bootcss.com/)
    - jekyll主题：[just-the-docs](https://github.com/pmarsceill/just-the-docs) [更新日志](https://just-the-docs.github.io/just-the-docs/CHANGELOG/)
    - 博客评论功能：[Gitalk](https://github.com/gitalk/gitalk)
    - 页面目录：[tocbot](https://github.com/tscanlin/tocbot)
    - 网站图片：[github图床](https://github.com/luguosong/images)+[jsdelivr加速](https://www.jsdelivr.com/github)+[PicGo工具](https://github.com/Molunerfinn/PicGo)
    - 图片查看：[Viewer.js](https://github.com/fengyuanchen/viewerjs)
    - 数学公式排版：[LaTeX](https://www.latex-project.org/)[MathJax](http://docs.mathjax.org/en/latest/)
    - 流程图： [mermaid](https://mermaid-js.github.io/mermaid/#/)

# 网站性能优化

由于网站是使用github pages部署的，国内访问相对很慢，做如下优化提高访问速度：

- 网站中的静态资源（图片、js、css）使用[jsdelivr](https://www.jsdelivr.com/github)加速
- 网站使用[cloudflare](https://dash.cloudflare.com/)做CDN加速
- [图片懒加载](https://github.com/aFarkas/lazysizes)

# 网站字体

- [Source Code Pro](https://fonts.google.com/specimen/Source+Code+Pro)
    - 引用地址：`https://fonts.googleapis.com/css2?family=Source+Code+Pro:ital,wght@1,500&display=swap`
    - 加速地址：`https://fonts.loli.net/css2?family=Source+Code+Pro:ital,wght@1,500&display=swap`
- [霞鹜文楷](https://github.com/lxgw/LxgwWenKai)
    - [在网站上使用这款字体](https://github.com/chawyehsu/lxgw-wenkai-webfont)
    - 引用地址：`https://cdn.jsdelivr.net/npm/lxgw-wenkai-webfont@1.1.0/style.css`
    - 加速地址：`https://cdn.bootcdn.net/ajax/libs/lxgw-wenkai-webfont/1.6.0/style.min.css`
    - 加粗字体：`https://cdn.bootcdn.net/ajax/libs/lxgw-wenkai-webfont/1.6.0/lxgwwenkai-bold.css`
- [ZCOOL KuaiLe](https://fonts.google.com/specimen/ZCOOL+KuaiLe)
    - 引用地址：`https://fonts.googleapis.com/css2?family=ZCOOL+KuaiLe&display=swap`
    - 加速地址：`https://fonts.loli.net/css2?family=ZCOOL+KuaiLe&display=swap`
