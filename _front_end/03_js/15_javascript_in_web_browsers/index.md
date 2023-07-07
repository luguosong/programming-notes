---
layout: note
title: 浏览器中的JavaScript
nav_order: 150
parent: JavaScript
create_time: 2023/5/12
---

# Web编程基础

{: .note-title}
> 客户端JavaScript和服务端JavaScript
> 
> - `客户端JavaScript`就是在`浏览器`中运行的JavaScript代码
> - `服务端JavaScript`就是运行在服务器上的程序。

`Node`有自己唯一的实现，也有自己唯一的官方文档。相对而言，`Web API`则是通过`主要浏览器厂商的共识来定义的`。

## js代码嵌入方式

### 代码卸载script标签之间

JavaScript代码可以出现在HTML文件的`<script>`与`</script>`标签之间，也就是嵌入`HTML`中。

[代码效果](digital_clock.html)

{% highlight html %}
{% include_relative digital_clock.html %}
{% endhighlight %}

### src属性

- 更常见的方式是使用`<script>标签`的`src属性`指定javascript代码文件的url
  - 简化HTML
  - 多个网页中可以共享同一份js源码
  - 可以通过url利用互联网上的js

```html
<script src="scripts/digital_clock,js"></script>
```

### 模块




# 事件
# 操作DOM
# 操作CSS
# 文档几何与滚动
# Web组件
# 可伸缩矢量图形
# canvas与图形
# Audio API
# 位置、导航与历史
# 网络
# 存储
# 工作线程与消息传递
# 示例：曼德布洛特集合
