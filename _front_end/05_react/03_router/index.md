---
layout: note
title: react-router-dom5
nav_order: 30
parent: React
create_time: 2023/7/20
---

# 路由模式

- `BrowserRouter`：一个使用HTML5历史API（pushState，replaceState和popstate事件）的<Router>组件，以使用户界面与URL保持同步。
- `HashRouter`：一个使用URL的哈希部分（即window.location.hash）来将用户界面与URL保持同步的<Router>组件。

# React router 5 示例

{% highlight react %}
{% include_relative router5-example/src/Basic.js %}
{% endhighlight %}
