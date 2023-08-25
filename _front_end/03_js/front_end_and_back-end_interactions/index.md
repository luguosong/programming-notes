---
layout: note
title: 前后端交互
nav_order: 30
parent: JavaScript
create_time: 2023/8/11
---

# Ajax概述

{: .note-title}
> Ajax
>
> Asynchronous JavaScript and XML，异步 JavaScript 和 XML
>
> 用来实现客户端网页请求服务器数据。

# axios发送get请求

{% highlight html %}
{% include_relative axios_get.html %}
{% endhighlight %}

# axios发送get请求携带参数

{% highlight html %}
{% include_relative axios_get_params.html %}
{% endhighlight %}

# axios发送post请求

{% highlight html %}
{% include_relative axios_post.html %}
{% endhighlight %}

# 表单提交

## 概述

form标签通过action和method属性向后台提交数据。

`表单提交存在的问题`： 不管是get还是post请求页面会发生跳转

`解决方案`：表单只负责数据采集，与后台的数据交互交给Ajax

## form-serialize插件

作用：序列化表单数据

{% highlight html %}
{% include_relative form_serialize.html %}
{% endhighlight %}

## FormData构造函数

用于文件上传

{% highlight html %}
{% include_relative formdata.html %}
{% endhighlight %}

# Content-Type

1. 当请求体为对象时，axios会自动将其转为json，并设置Content-Type为`application/json`
2. 当请求体为FormData时，axios会自动将其设置Content-Type为`multipart/form-data`
3. 当请求体为URLSearchParams时，axios会自动将其设置Content-Type为`application/x-www-form-urlencoded`

# XMLHttpRequest

浏览器内置的构造函数。 用于发送Ajax请求。

axios底层就是使用XMLHttpRequest发送请求。

使用步骤：

1. 创建XMLHttpRequest对象
2. 调用open方法，设置请求方式和请求地址
3. 调用send方法，发送请求
4. 监听load事件，获取响应数据

{% highlight html %}
{% include_relative xmlhttprequest.html %}
{% endhighlight %}

# 同源和跨域

{: .note-title}
> 同源
>
> 协议、域名、端口号相同

{: .note-title}
> 同源策略
>
> 浏览器的一种安全策略，不允许非同源URL之间进行资源交互

{: .note-title}
> 跨域
>
> 协议、域名、端口号不同

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202308230941861-%E8%B7%A8%E5%9F%9F%E9%97%AE%E9%A2%98.png)

跨域问题解决方案：

- 代理服务器（反向代理）
- `CORS（Cross-Origin Resource Sharing）`跨域资源共享
    - 前提：浏览器支持CORS功能
    - 服务端设置响应头实现跨域
        - Access-Control-Allow-Origin：允许跨域的域名，*表示所有域名
