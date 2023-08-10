---
layout: note
title: 浏览器中的js
nav_order: 20
parent: JavaScript
create_time: 2023/8/3
---

# 可以做什么

1. 在网页中添加新的 HTML，修改网页已有内容和网页的样式。
2. 在网页中添加新的 HTML，修改网页已有内容和网页的样式。
3. 向远程服务器发送网络请求，下载和上传文件（所谓的 AJAX 和 COMET 技术）。
4. 获取或设置 cookie，向访问者提出问题或发送消息。
5. 记住客户端的数据（“本地存储”）。

# 不可以做什么

1. 不提供对内存或 CPU 的底层访问
2. 不能读、写、复制和执行硬盘上的任意文件。它没有直接访问操作系统的功能。
3. 不同的标签页/窗口之间通常互不了解。
4. 从其他网站/域的服务器中接收数据的能力被削弱了。尽管可以，但是需要来自远程服务器的明确协议（在 HTTP header 中）。

# DOM简介

{: .note-title}
> DOM
>
> Document Object Model-文档对象模型
>
> 用于操作网页文档，开发网页特效和实现用户交互

DOM的核心思想是`将网页当作对象`来处理，通过`对象的属性和方法`对网页内容进行操作。

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202308062153600-DOM-%E6%A0%87%E7%AD%BE%E8%A2%AB%E8%BD%AC%E6%8D%A2%E4%B8%BA%E5%AF%B9%E8%B1%A1.png)

# document对象

{: .note-title}
> document对象
>
> 在JavaScript中，document对象是HTML页面的根节点，它代表整个HTML文档。
>
> document对象是Window对象的一部分，可以通过window.document属性对其进行访问。
>
> document对象提供了许多方法，可以用来获取或操作HTML页面中的元素。例如，可以使用getElementById()方法来获取页面中具有指定ID的元素。

# DOM-获取元素

{% highlight html %}
{% include_relative get_element.html %}
{% endhighlight %}

# DOM-修改元素内容

{% highlight html %}
{% include_relative edit_element.html %}
{% endhighlight %}

# DOM-事件

{% highlight html %}
{% include_relative event.html %}
{% endhighlight %}

<iframe src="event.html"></iframe>

# DOM-事件流

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202308071659967-js%E4%BA%8B%E4%BB%B6%E6%B5%81.png)

1. 事件捕获阶段：当一个事件触发时，会从DOM的根元素开始依次调用`同名事件`（从外到里）
2. 事件冒泡阶段：当一个元素的事件触发时，会在该元素所有祖先元素中依次触发`同名事件`（从里到外）（默认为冒泡阶段）

{% highlight html %}
{% include_relative event_stream.html %}
{% endhighlight %}

<iframe src="event_stream.html"></iframe>

阻止事件冒泡：

{% highlight html %}
{% include_relative stopping_events_from_bubbling.html %}
{% endhighlight %}

<iframe src="stopping_events_from_bubbling.html"></iframe>

# DOM-事件委托

{: .note-title}
> 事件委托
>
> 将子元素事件委托给父元素，通过事件冒泡机制，可以在父元素上监听到子元素的事件，从而触发父元素的事件处理函数。

{% highlight html %}
{% include_relative event_delegation.html %}
{% endhighlight %}

<iframe src="event_delegation.html"></iframe>

# DOM-阻止默认行为

{% highlight html %}
{% include_relative prevent_default_behavior.html %}
{% endhighlight %}

<iframe src="prevent_default_behavior.html"></iframe>

# DOM-移除事件监听

{% highlight html %}
{% include_relative remove_event_listener.html %}
{% endhighlight %}

<iframe src="remove_event_listener.html"></iframe>

# DOM-常用事件

- `DOMContentLoaded`: DOM(HTML文档)加载完成后触发
- `load`: 页面加载完成后触发，包含图片，CSS等外部资源
- `scroll`: 滚动条滚动时触发
    - `document.documentElement.scrollTop`: 页面滚动条距离顶部的距离
- `resize`: 窗口大小改变时触发
- touch事件
    - `touchstart`: 手指触摸屏幕时触发
    - `touchmove`: 手指在屏幕上滑动时连续触发
    - `touchend`: 手指离开屏幕时触发
    - `touchcancel`: 系统取消touch时触发，例如：来电、弹窗

# DOM-获取元素的尺寸和位置

{% highlight html %}
{% include_relative size_and_location.html %}
{% endhighlight %}

<iframe src="size_and_location.html"></iframe>

# DOM节点

## DOM树

{: .note-title}
> DOM树
>
> 将HTML文档以树形结构的形式表示出来，称为DOM树。

节点分类：

1. 元素节点：HTML标签
2. 文本节点：HTML文本
3. 属性节点：HTML标签的属性

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202308081725115-DOM%E6%A0%91.png)

{% highlight html %}
{% include_relative dom_tree.html %}
{% endhighlight %}

# BOM简介

{: .note-title}
> BOM
>
> Browser Object Model-浏览器对象模型
>
> 用于操作浏览器窗口和浏览器本身，开发浏览器特效和实现浏览器功能

`window对象`一般可以省略，比如`alert()`，可以写成`window.alert()`，也可以直接写成`alert()`。

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202308081822574-BOM.png)

如上图所示，BOM由多个对象组成，其中最核心的是`window`对象，它表示浏览器的一个实例，同时也是JS访问浏览器的一个接口。

{: .important-title}
> BOM和DOM的关系
>
> DOM是包含在BOM中的

{: .important-title}
> window对象可以省略
>
> `window对象`一般可以省略，比如`alert()`，可以写成`window.alert()`，也可以直接写成`alert()`。

# BOM-var和函数

`var`声明的变量会成为window对象的`属性`，而let和const声明的变量不会成为window对象的属性。

`函数`声明会成为window对象的`方法`，而函数表达式不会成为window对象的方法。

{% highlight html %}
{% include_relative bom_var.html %}
{% endhighlight %}

# 定时器-setInterval

{% highlight html %}
{% include_relative set_interval.html %}
{% endhighlight %}

<iframe src="set_interval.html"></iframe>

# 定时器-setTimeout

{% highlight html %}
{% include_relative set_timeout.html %}
{% endhighlight %}

<iframe src="set_timeout.html"></iframe>

# BOM-location对象

{: .note-title}
> location
>
> 拆分并保存了URL地址的各个部分

{% highlight html %}
{% include_relative bom_location.html %}
{% endhighlight %}

<iframe src="bom_location.html"></iframe>

# BOM-navigator对象

{: .note-title}
> navigator
>
> 记录浏览器自身相关信息

{% highlight html %}
{% include_relative bom_navigator.html %}
{% endhighlight %}

<iframe src="bom_navigator.html"></iframe>

# BOM-history对象

{: .note-title}
> history
>
> 记录浏览器访问过的URL地址

- `back()`:后退
- `forward()`:前进
- `go(参数)`:前进或后退，1前进，-1后退

# BOM-本地存储

- localStorage
    - 永久存储，除非手动删除
    - 保存的数据没有过期时间，直到手动删除
    - 保存的数据在同源的所有页面中都可以访问
- sessionStorage
    - 临时存储，关闭浏览器窗口后自动删除
    - 保存的数据在同源的同窗口（或同标签页）中都可以访问


{% highlight html %}
{% include_relative bom_localstorage.html %}
{% endhighlight %}

<iframe src="bom_localstorage.html"></iframe>
